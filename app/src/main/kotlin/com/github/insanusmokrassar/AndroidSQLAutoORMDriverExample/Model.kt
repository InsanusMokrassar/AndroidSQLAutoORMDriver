package com.github.insanusmokrassar.AndroidSQLAutoORMDriverExample

import com.github.insanusmokrassar.AutoORM.core.PrimaryKey

interface Model {
    @PrimaryKey
    val id: Int?
    val name: String
}