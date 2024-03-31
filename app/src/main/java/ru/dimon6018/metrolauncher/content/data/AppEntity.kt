package ru.dimon6018.metrolauncher.content.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity( tableName = "apps" )
data class AppEntity (
    @PrimaryKey
    var appPos: Int? = null,
    var id: Long? = null,
    @ColumnInfo
    var tileColor: Int? = null,
    var tileType: Int? = null,
    var isPlaceholder: Boolean? = null,
    var isSelected: Boolean? = null,
    var appSize: String = "",
    var appLabel: String = "",
    var appPackage: String = ""
)