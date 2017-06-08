package com.github.insanusmokrassar.AndroidSQLAutoORMDriver

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.github.insanusmokrassar.AutoORM.core.*
import com.github.insanusmokrassar.AutoORM.core.drivers.tables.interfaces.ConnectionProvider
import com.github.insanusmokrassar.AutoORM.core.drivers.tables.interfaces.TableProvider
import com.github.insanusmokrassar.iobjectk.interfaces.IObject
import kotlin.reflect.KClass

val nativeTypesMap = mapOf(
        Pair(
                Int::class,
                "INTEGER"
        ),
        Pair(
                Long::class,
                "LONG"
        ),
        Pair(
                Float::class,
                "FLOAT"
        ),
        Pair(
                Double::class,
                "DOUBLE"
        ),
        Pair(
                String::class,
                "TEXT"
        ),
        Pair(
                Boolean::class,
                "BOOLEAN"
        )
)

class ASQLConnectionProvider(
        val parameters: IObject<Any>): ConnectionProvider, SQLiteOpenHelper(parameters.get<Context>("context"), parameters.get<String>("name"), null, parameters.get<Int>("version"))  {

    val roDB = readableDatabase
    val woDB = readableDatabase

    override fun close() {
        super.close()
    }
    override fun onCreate(db: SQLiteDatabase?) {
        if (parameters.keys().contains("prepare")) {
            val modelToPrepare = parameters.get<List<Any>>("prepare")
            modelToPrepare.forEach {
                val currentModelClass = Class.forName(it as String).kotlin
                createTableIfNotExist(currentModelClass, db!!)
            }
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        Log.i(this::class.simpleName, "For some of reason was called onUpgrade with old versiont $oldVersion and new version $newVersion")
    }

    override fun <M : Any, O : M> getTableProvider(modelClass: KClass<M>, operationsClass: KClass<in O>): TableProvider<M, O> {
        createTableIfNotExist(modelClass, woDB)
        return ASQLTableProvider(modelClass, operationsClass, roDB, woDB)
    }
}

fun <M : Any> createTableIfNotExist(modelClass: KClass<M>, woDB: SQLiteDatabase) {
    val fieldsBuilder = StringBuilder()
    val primaryFields = modelClass.getPrimaryFields()

    modelClass.getVariables().forEach {
        if (it.isReturnNative()) {
            fieldsBuilder.append("${it.name} ${nativeTypesMap[it.returnClass()]}")
            if (!it.isNullable()) {
                fieldsBuilder.append(" NOT NULL")
            }
            if (primaryFields.contains(it) && it.isAutoincrement()) {
                fieldsBuilder.append(" AUTOINCREMENT")
            }
        } else {
            TODO()
        }
        fieldsBuilder.append(", ")
    }
    if (primaryFields.isNotEmpty()) {
        fieldsBuilder.append("CONSTRAINT ${modelClass.simpleName}_PR_KEY PRIMARY KEY (")
        primaryFields.forEach {
            fieldsBuilder.append(it.name)
            if (!primaryFields.isLast(it)) {
                fieldsBuilder.append(", ")
            }
        }
        fieldsBuilder.append(")")
    }

    try {
        woDB.execSQL("CREATE TABLE IF NOT EXISTS ${modelClass.simpleName} ($fieldsBuilder);")
        Log.i("createTableIfNotExist", "Table ${modelClass.simpleName} was created")
    } catch (e: Exception) {
        Log.e("createTableIfNotExist", "init", e)
        throw IllegalArgumentException("Can't create table ${modelClass.simpleName}", e)
    }
}
