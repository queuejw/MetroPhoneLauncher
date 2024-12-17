package ru.queuejw.mpl.content.data.bsod

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [BSODEntity::class], version = 2)
abstract class BSOD : RoomDatabase() {

    abstract fun getDao(): BSODDao

    companion object {
        private const val DB_BSOD: String = "bsodList.db"

        fun getData(context: Context): BSOD {
            return Room.databaseBuilder(context, BSOD::class.java, DB_BSOD)
                .fallbackToDestructiveMigration().build()
        }
    }
}