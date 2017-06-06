package com.github.insanusmokrassar.androidsqlautoormdriver

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.github.insanusmokrassar.AutoORM.core.DatabaseConnect
import com.github.insanusmokrassar.AutoORM.core.drivers.databases.interfaces.DatabaseDriver
import com.github.insanusmokrassar.iobjectk.interfaces.IObject
import kotlin.reflect.KClass

class ASQLDatabaseDriver(parameters: IObject<Any>) : DatabaseDriver, SQLiteOpenHelper(Context::class.objectInstance, parameters.get<String>("name"), null, parameters.get<Int>("version")) {
    override fun onCreate(db: SQLiteDatabase?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getDatabaseConnect(params: IObject<Any>, onFreeCallback: (DatabaseConnect) -> Unit, onCloseCallback: (DatabaseConnect) -> Unit): DatabaseConnect {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun supportTable(modelClass: KClass<*>): Boolean {
        return true
    }
}