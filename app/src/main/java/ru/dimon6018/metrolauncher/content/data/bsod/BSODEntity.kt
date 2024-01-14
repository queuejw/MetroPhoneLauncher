package ru.dimon6018.metrolauncher.content.data.bsod

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity( tableName = "bsod_list" )
class BSODEntity {
    @PrimaryKey
    var pos: Int? = null
    @ColumnInfo
    var log: String = ""
    var date: String? = null
}
