package com.thaicrew.splitup.check.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "check_table")
data class CheckEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "check_id")
    val id: Int = 0,

    @ColumnInfo(name = "check_name")
    val name: String,

    @ColumnInfo(name = "check_cration_date")
    val creationDate: Long,

    @ColumnInfo(name = "check_closing_date")
    val closingDate: Long? = null,

    @ColumnInfo(name = "check_status")
    val status: String
)