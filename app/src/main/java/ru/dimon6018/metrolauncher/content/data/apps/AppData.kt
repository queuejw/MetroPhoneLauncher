package ru.dimon6018.metrolauncher.content.data.apps

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [AppEntity::class], version = 4)
abstract class AppData: RoomDatabase() {

    abstract fun getAppDao(): AppDao

    companion object {
        private const val DB: String = "AppData.db"

        fun getAppData(context: Context): AppData {
            return Room.databaseBuilder(context, AppData::class.java, DB).fallbackToDestructiveMigration().build()
        }
    }
}