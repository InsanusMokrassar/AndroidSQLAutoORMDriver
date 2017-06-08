package com.github.insanusmokrassar.AndroidSQLAutoORMDriver

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.github.insanusmokrassar.AutoORM.configField
import com.github.insanusmokrassar.AutoORM.core.DatabaseConnect
import com.github.insanusmokrassar.AutoORM.core.drivers.databases.abstracts.AbstractDatabaseProvider
import com.github.insanusmokrassar.AutoORM.core.drivers.databases.interfaces.DatabaseProvider
import com.github.insanusmokrassar.AutoORM.core.drivers.tables.interfaces.ConnectionProvider
import com.github.insanusmokrassar.AutoORM.core.drivers.tables.interfaces.Transactable
import com.github.insanusmokrassar.AutoORM.databasesField
import com.github.insanusmokrassar.IObjectKRealisations.JSONIObject
import com.github.insanusmokrassar.iobjectk.interfaces.IObject
import java.util.*
import kotlin.reflect.KClass

class ASQLDatabaseProvider(val parameters: IObject<Any>) : AbstractDatabaseProvider() {
    override fun makeDriverAndTransactable(params: IObject<Any>): Pair<ConnectionProvider, Transactable> {
        val connectionProvider = ASQLConnectionProvider(params)
        return Pair(
                connectionProvider,
                ASQLTransactable(connectionProvider.woDB)
        )
    }

    override fun supportTable(modelClass: KClass<*>): Boolean {
        return true
    }
}

fun prepareConfig(assetPath: String, context: Context): IObject<Any> {
    val configScanner = Scanner(context.assets.open(assetPath))
    val configBuilder = StringBuilder()
    while (configScanner.hasNext()) {
        configBuilder.append("${configScanner.nextLine()}\n")
    }
    val params = JSONIObject(configBuilder.toString())
    val databases = params.get<List<Any>>(databasesField)
    databases.forEach {
        if (it is IObject<*>) {
            val config = it as IObject<Any>
            if (config.keys().contains("context") && config.get("context")) {
                val databaseProviderParams: IObject<Any> = config.get(configField)
                databaseProviderParams.put("context", context)
            }
        }
    }
    return params
}
