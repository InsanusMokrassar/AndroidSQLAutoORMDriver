package com.github.insanusmokrassar.androidsqlautoormdriver

import android.database.sqlite.SQLiteDatabase
import com.github.insanusmokrassar.AutoORM.core.drivers.tables.interfaces.TableDriver
import com.github.insanusmokrassar.AutoORM.core.drivers.tables.interfaces.TableProvider
import kotlin.reflect.KClass

class ASQLTableDriver(
        val roDB: SQLiteDatabase,
        val woDB: SQLiteDatabase): TableDriver {
    override fun close() {
        roDB.close()
        woDB.close()
    }

    override fun <M : Any, O : M> getTableProvider(modelClass: KClass<M>, operationsClass: KClass<in O>): TableProvider<M, O> {
        return ASQLTableProvider(modelClass, operationsClass, roDB, woDB)
    }
}


