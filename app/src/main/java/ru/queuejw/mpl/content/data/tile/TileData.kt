package ru.queuejw.mpl.content.data.tile

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Tile::class], version = 3, autoMigrations = [AutoMigration(from = 2, to = 3)], exportSchema = true)
abstract class TileData : RoomDatabase() {

    abstract fun getTileDao(): TileDao

    companion object {
        private const val DB: String = "tileData.db"

        fun getTileData(context: Context): TileData {
            return Room.databaseBuilder(context, TileData::class.java, DB).build()
        }
    }
}