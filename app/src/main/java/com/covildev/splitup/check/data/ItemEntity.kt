package com.covildev.splitup.check.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "item_table",
    foreignKeys = [
        ForeignKey(
            entity = CheckEntity::class,
            parentColumns = ["check_id"],
            childColumns = ["check_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["item_id", "check_id"], unique = true)]
)
data class ItemEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "item_id")
    val id: Int = 0,

    @ColumnInfo(name = "check_id", index = true)
    val checkId: Int,

    @ColumnInfo(name = "item_name")
    val name: String,

    @ColumnInfo(name = "item_quantity")
    val quantity: Int,

    @ColumnInfo(name = "item_value")
    val valueInCents: Long,
)

