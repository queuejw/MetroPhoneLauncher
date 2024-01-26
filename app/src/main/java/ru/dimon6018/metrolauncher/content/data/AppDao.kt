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
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun updateAllApps(apps: MutableList<AppEntity>)
    @Query("SELECT * FROM apps")
    fun getApps(): Flow<MutableList<AppEntity>>
    @Query("SELECT * FROM apps WHERE isPlaceholder = :bool")
    fun getAppsWithoutPlaceholders(bool: Boolean): Flow<MutableList<AppEntity>>
    @Query("SELECT * FROM apps")
    fun getJustApps(): MutableList<AppEntity>
    @Query("SELECT * FROM apps WHERE isPlaceholder = :bool")
    fun getJustAppsWithoutPlaceholders(bool: Boolean): MutableList<AppEntity>
    @Query("SELECT * FROM apps WHERE appPos = :pos")
    fun getApp(pos: Int): AppEntity
    @Query("SELECT * FROM apps WHERE id = :id")
    fun getAppById(id: Int): AppEntity
    @Query("DELETE FROM apps")
    fun removeAllApps()
}