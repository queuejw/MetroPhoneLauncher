package ru.dimon6018.metrolauncher.content.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [AppEntity::class], version = 1)
abstract class AppData: RoomDatabase() {

    abstract fun getAppDao(): AppDao

    companion object {
        private const val DB: String = "AppData.db"

        fun getAppData(context: Context): AppData {
            return Room.databaseBuilder(context.applicationContext, AppData::class.java, DB).build()
        }
    }
}