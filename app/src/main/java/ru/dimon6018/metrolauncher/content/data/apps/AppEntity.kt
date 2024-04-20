package ru.dimon6018.metrolauncher.content.data.apps

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity( tableName = "apps" )
data class AppEntity (
    @PrimaryKey
    var appPos: Int? = null,
    var id: Long? = null,
    @ColumnInfo
    var tileColor: Int? = null, //-1 - theme color, ... see Prefs.kt
    var tileType: Int? = null, // -1 - placeholder, 0 - default, 1 - weather ...
    var isSelected: Boolean? = null,
    var tileSize: String = "", //big, medium, small
    var appLabel: String = "",
    var appPackage: String = ""
)