package com.github.insanusmokrassar.androidsqlautoormdriver

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.github.insanusmokrassar.AutoORM.core.DatabaseConnect
import com.github.insanusmokrassar.AutoORM.core.drivers.databases.interfaces.DatabaseProvider
import com.github.insanusmokrassar.iobjectk.interfaces.IObject
import kotlin.reflect.KClass

class ASQLDatabaseProvider(val parameters: IObject<Any>) : DatabaseProvider, SQLiteOpenHelper(parameters.get<Context>("context"), parameters.get<String>("name"), null, parameters.get<Int>("version")) {
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

    override fun getDatabaseConnect(params: IObject<Any>, onFreeCallback: (DatabaseConnect) -> Unit, onCloseCallback: (DatabaseConnect) -> Unit): DatabaseConnect {
        return DatabaseConnect(
                ASQLConnectionProvider(readableDatabase, writableDatabase),
                ASQLTransactable(writableDatabase),
                onFreeCallback,
                onCloseCallback
        )
    }

    override fun supportTable(modelClass: KClass<*>): Boolean {
        return true
    }
}