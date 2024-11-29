package ru.dimon6018.metrolauncher.content.data.tile

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface TileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addTile(app: Tile)

    @Delete
    suspend fun deleteTile(app: Tile)

    @Update
    suspend fun updateTile(app: Tile)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun updateAllTiles(apps: MutableList<Tile>)

    @Query("SELECT * FROM tiles")
    fun getTilesLiveData(): LiveData<MutableList<Tile>>

    @Query("SELECT * FROM tiles")
    suspend fun getTilesList(): MutableList<Tile>

    @Query("SELECT * FROM tiles WHERE tileType != -1") //-1
    suspend fun getUserTiles(): MutableList<Tile>

    @Query("SELECT * FROM tiles WHERE tilePosition = :pos")
    fun getTileFromPosition(pos: Int): Tile

    @Query("SELECT * FROM tiles WHERE id = :id")
    fun getTileByID(id: Int): Tile

    @Query("DELETE FROM tiles")
    fun deleteAllTiles()
}