package com.github.insanusmokrassar.AndroidSQLAutoORMDriverExample

interface Operations {
    fun insert(what: Model)
    fun getById(id: Int): Model
}