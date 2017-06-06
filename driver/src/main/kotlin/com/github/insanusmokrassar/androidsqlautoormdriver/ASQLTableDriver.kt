package com.github.insanusmokrassar.androidsqlautoormdriver

import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.github.insanusmokrassar.AutoORM.core.*
import com.github.insanusmokrassar.AutoORM.core.drivers.tables.interfaces.TableDriver
import com.github.insanusmokrassar.AutoORM.core.drivers.tables.interfaces.TableProvider
import java.util.logging.Logger
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

class ASQLTableDriver(
        val roDB: SQLiteDatabase,
        val woDB: SQLiteDatabase): TableDriver {
    override fun close() {
        roDB.close()
        woDB.close()
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
                fieldsBuilder.append(" AUTO_INCREMENT")
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
