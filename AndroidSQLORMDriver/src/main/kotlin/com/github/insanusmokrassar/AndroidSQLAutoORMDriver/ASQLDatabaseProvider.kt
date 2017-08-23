package com.github.insanusmokrassar.AndroidSQLAutoORMDriver

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.github.insanusmokrassar.AutoORM.classesCompilerField
import com.github.insanusmokrassar.AutoORM.configField
import com.github.insanusmokrassar.AutoORM.core.DatabaseConnect
import com.github.insanusmokrassar.AutoORM.core.drivers.databases.abstracts.AbstractDatabaseProvider
import com.github.insanusmokrassar.AutoORM.core.drivers.databases.interfaces.DatabaseProvider
import com.github.insanusmokrassar.AutoORM.core.drivers.tables.interfaces.ConnectionProvider
import com.github.insanusmokrassar.AutoORM.core.drivers.tables.interfaces.Transactable
import com.github.insanusmokrassar.AutoORM.databasesField
import com.github.insanusmokrassar.IObjectKRealisations.JSONIObject
import com.github.insanusmokrassar.IObjectKRealisations.openFile
import com.github.insanusmokrassar.iobjectk.exceptions.ReadException
import com.github.insanusmokrassar.iobjectk.interfaces.IObject
import java.util.*
import kotlin.collections.ArrayList
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

    replaceAllContexts(params, context)

//    val databases = params.get<List<Any>>(databasesField)
//    databases.forEach {
//        if (it is IObject<*>) {
//            val config = it as IObject<Any>
//            if (config.keys().contains(contextField) && config.get(contextField)) {
//                val databaseProviderParams: IObject<Any> = config.get(configField)
//                databaseProviderParams.put(contextField, context)
//            }
//        }
//    }
//    try {
//        val compilerFullConfig = params.get<IObject<Any>>(classesCompilerField)
//        try {
//            val config = compilerFullConfig.get<Any>(configField)
//            when (config) {
//                is String -> if (config == contextField) { compilerFullConfig.put(configField, context) }
//                is List<*> -> {
//                    val resultConfig = ArrayList(config)
//                    resultConfig.forEach {
//                        if (it == contextField) {
//                            resultConfig[resultConfig.indexOf(it)] = context
//                        }
//                    }
//                    compilerFullConfig.put(configField, resultConfig)
//                }
//                is IObject<*> -> {
//                    (config as? IObject<Any>)?.let {
//                        if (it.keys().contains(contextField)) {
//                            it.put(contextField, context)
//                        }
//                    }
//                }
//            }
//        } catch (e: ReadException) {
//            Log.e("Prepare config", "Can't load config section, create new", e)
//        }
//    } catch (e: ReadException) {
//        Log.e("Prepare config", "Can't load compiler section", e)
//    }
    return params
}

fun replaceAllContexts(iobject: IObject<Any>, context: Context) {
    iobject.keys().forEach {
        val value = iobject.get<Any>(it)
        when (value) {
            is String -> if (value == contextField) { iobject.put(it, context) }
            is List<*> -> {
                val resultConfig = ArrayList(value)
                resultConfig.forEach {
                    when (it) {
                        is String -> (it == contextField).let { resultConfig[resultConfig.indexOf(it)] = context }
                        is IObject<*> -> {
                            (it as? IObject<Any>)?.let {
                                replaceAllContexts(it, context)
                            }
                        }
                    }
                }
                iobject.put(it, resultConfig)
            }
            is IObject<*> -> {
                (value as? IObject<Any>)?.let { replaceAllContexts(it, context) }
            }
        }
    }
}
