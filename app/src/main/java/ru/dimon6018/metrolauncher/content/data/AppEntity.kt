package ru.dimon6018.metrolauncher.content.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity( tableName = "apps" )
data class AppEntity (
    @PrimaryKey
    var appPos: Int? = null,
    var id: Int? = null,
    var tileColor: Int? = null,
    @ColumnInfo
    var appSize: String = "",
    var appLabel: String = "",
    var appPackage: String = ""
)