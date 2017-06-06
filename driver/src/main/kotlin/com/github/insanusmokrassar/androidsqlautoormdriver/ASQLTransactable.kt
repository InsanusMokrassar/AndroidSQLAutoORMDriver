package com.github.insanusmokrassar.androidsqlautoormdriver

import android.database.sqlite.SQLiteDatabase
import com.github.insanusmokrassar.AutoORM.core.drivers.tables.interfaces.Transactable

class ASQLTransactable(val database: SQLiteDatabase): Transactable {
    override fun start() {
        database.beginTransaction()
    }

    override fun abort() {
        database.endTransaction()
    }

    override fun submit() {
        database.setTransactionSuccessful()
        database.endTransaction()
    }
}