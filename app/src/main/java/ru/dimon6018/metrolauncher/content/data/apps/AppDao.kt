package ru.dimon6018.metrolauncher.content.data.apps

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
    suspend fun insertItem(app: AppEntity)
    @Delete
    suspend fun removeApp(app: AppEntity)
    @Update
    suspend fun updateApp(app: AppEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun updateAllApps(apps: MutableList<AppEntity>)
    @Query("SELECT * FROM apps")
    fun getApps(): Flow<MutableList<AppEntity>>
    @Query("SELECT * FROM apps")
    fun getJustApps(): MutableList<AppEntity>
    @Query("SELECT * FROM apps WHERE tileType = -1") //-1
    fun getJustAppsWithoutPlaceholders(): MutableList<AppEntity>
    @Query("SELECT * FROM apps WHERE appPos = :pos")
    fun getApp(pos: Int): AppEntity
    @Query("SELECT * FROM apps WHERE id = :id")
    fun getAppById(id: Int): AppEntity
    @Query("DELETE FROM apps")
    suspend fun removeAllApps()
}