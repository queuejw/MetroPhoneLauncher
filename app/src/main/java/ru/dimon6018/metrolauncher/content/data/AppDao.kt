package ru.dimon6018.metrolauncher.content.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItem(app: AppEntity)
    @Delete
    fun removeApp(app: AppEntity)
    @Update
    fun updateApp(app: AppEntity)
    @Query("SELECT * FROM apps")
    fun getApps(): Flow<List<AppEntity>>
    @Query("SELECT * FROM apps")
    fun getJustApps(): List<AppEntity>
    @Query("SELECT * FROM apps WHERE appPos = :pos")
    fun getApp(pos: Int): AppEntity
    @Query("SELECT * FROM apps WHERE id = :id")
    fun getAppById(id: Int): AppEntity
}