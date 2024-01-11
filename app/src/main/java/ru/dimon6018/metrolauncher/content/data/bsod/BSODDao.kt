package ru.dimon6018.metrolauncher.content.data.bsod

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface BSODDao {
    @Insert
    fun insertLog(bsod: BSODEntity)
    @Delete
    fun removeLog(bsod: BSODEntity)
    @Query("SELECT * FROM bsod_list")
    fun getBsodList(): List<BSODEntity>
    @Query("SELECT * FROM bsod_list WHERE pos = :pos")
    fun getBSOD(pos: Int): BSODEntity
}
