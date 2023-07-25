package com.example.mysql.entity

import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar

object EntityAdmin : Table<Nothing>(tableName = "admin") {
    val id = int(name = "id").primaryKey()
    val nama = varchar(name = "nama")
    val email = varchar(name = "email")
    val password = varchar(name = "password")
}