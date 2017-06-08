package com.github.insanusmokrassar.AndroidSQLAutoORMDriver

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.github.insanusmokrassar.AutoORM.core.asSQLString
import com.github.insanusmokrassar.AutoORM.core.drivers.tables.SearchQuery
import com.github.insanusmokrassar.AutoORM.core.drivers.tables.abstracts.AbstractTableProvider
import com.github.insanusmokrassar.AutoORM.core.drivers.tables.filters.Filter
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

private val operations = mapOf(
        Pair(
                "eq",
                {
                    it: Filter ->
                    if (it.args[0] is String) {
                        it.args[0] = (it.args[0] as String).asSQLString()
                    }
                    if (it.isNot) {
                        "${it.field}!=${it.args[0]}"
                    } else {
                        "${it.field}=${it.args[0]}"
                    }
                }
        ),
        Pair(
                "is",
                {
                    it: Filter ->
                    if (it.args[0] is String) {
                        it.args[0] = (it.args[0] as String).asSQLString()
                    }
                    if (it.isNot) {
                        "${it.field}!=${it.args[0]}"
                    } else {
                        "${it.field}=${it.args[0]}"
                    }
                }
        ),
        Pair(
                "gt",
                {
                    it: Filter ->
                    if (it.isNot) {
                        "${it.field}<=${it.args[0]}"
                    } else {
                        "${it.field}>${it.args[0]}"
                    }
                }
        ),
        Pair(
                "gte",
                {
                    it: Filter ->
                    if (it.isNot) {
                        "${it.field}<${it.args[0]}"
                    } else {
                        "${it.field}>=${it.args[0]}"
                    }
                }
        ),
        Pair(
                "lt",
                {
                    it: Filter ->
                    if (it.isNot) {
                        "${it.field}>=${it.args[0]}"
                    } else {
                        "${it.field}<${it.args[0]}"
                    }
                }
        ),
        Pair(
                "lte",
                {
                    it: Filter ->
                    if (it.isNot) {
                        "${it.field}>${it.args[0]}"
                    } else {
                        "${it.field}<=${it.args[0]}"
                    }
                }
        ),
        Pair(
                "in",
                {
                    it: Filter ->
                    if (it.isNot) {
                        "${it.field}<${it.args[0]} OR ${it.field}>${it.args[1]}"
                    } else {
                        "${it.field}>=${it.args[0]} AND ${it.field}<=${it.args[1]}"
                    }
                }
        ),
        Pair(
                "oneof",
                {
                    filter: Filter ->
                    val localBuilder = StringBuilder()
                    val operator: String
                    if (filter.isNot) {
                        operator = "!="
                    } else {
                        operator = "="
                    }
                    localBuilder.append("(")
                    filter.args.forEach {
                        localBuilder.append("${filter.field}$operator$it")
                        if (filter.args.indexOf(it) < filter.args.size - 1) {
                            localBuilder.append(" OR ")
                        }
                    }
                    localBuilder.append(")")
                }
        )
)

class ASQLTableProvider<M : Any, O : M>(
        modelClass: KClass<M>,
        operationsClass: KClass<in O>,
        val roDB: SQLiteDatabase,
        val woDB: SQLiteDatabase) :
        AbstractTableProvider<M, O>(
                modelClass,
                operationsClass) {

    override fun find(where: SearchQuery): Collection<O> {
        val cursor = roDB.query(modelClass.simpleName, where.fields.toTypedArray(), compileSearchQuery(where), null, null, null, null, compilePaging(where))

        val result = ArrayList<O>()
        if (cursor.moveToFirst()) {
            val properties = variablesMap.filter {
                where.fields.contains(it.key) || where.fields.isEmpty()
            }
            do {
                val values = HashMap<KProperty<*>, Any>()
                val extras = cursor.extras
                properties.values.forEach {
                    values.put(
                            it, extras[it.name]
                    )
                }
                result.add(createModelFromValuesMap(values))
            } while (cursor.moveToNext())
        }

        cursor.close()

        return result
    }

    override fun remove(where: SearchQuery): Boolean {
        return woDB.delete(modelClass.simpleName, compileSearchQuery(where), null) > 0
    }

    override fun insert(values: Map<KProperty<*>, Any>): Boolean {
        return woDB.insert(modelClass.simpleName, null, valuesToContentValues(values)) > 0
    }

    override fun update(values: Map<KProperty<*>, Any>, where: SearchQuery): Boolean {
        return woDB.update(modelClass.simpleName, valuesToContentValues(values), compileSearchQuery(where), null) > 0
    }
}

fun compileSearchQuery(query: SearchQuery): String {
    val queryBuilder = StringBuilder()
    query.filters.forEach {
        queryBuilder.append(operations[it.filterName]!!(it))
        if (it.logicalLink != null) {
            queryBuilder.append(" ${it.logicalLink} ")
        }
    }
    return queryBuilder.toString()
}

fun compilePaging(query: SearchQuery): String? {
    if (query.pageFilter == null) {
        return null
    } else {
        return "limit ${query.pageFilter!!.size} offset ${query.pageFilter!!.page * query.pageFilter!!.size}"
    }
}

fun valuesToContentValues(values: Map<KProperty<*>, Any>): ContentValues {
    val cv = ContentValues()
    values.forEach {
        prop, value ->
        when(value::class) {
            Boolean::class -> cv.put(prop.name, value as Boolean)
            Int::class -> cv.put(prop.name, value as Int)
            Long::class -> cv.put(prop.name, value as Long)
            Float::class -> cv.put(prop.name, value as Float)
            Double::class -> cv.put(prop.name, value as Double)
            Byte::class -> cv.put(prop.name, value as Byte)
            ByteArray::class -> cv.put(prop.name, value as ByteArray)
            String::class -> cv.put(prop.name, value as String)
            Short::class -> cv.put(prop.name, value as Short)
        }
    }
    return cv
}
