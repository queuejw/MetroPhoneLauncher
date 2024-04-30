package ru.dimon6018.metrolauncher.content.data.bsod

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity( tableName = "bsod_list" )
data class BSODEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    @ColumnInfo
    var pos: Int? = null,
    var log: String = "",
    var date: String? = null
)
