package com.cafeyvinowinebar.cafe_y_vino_client.data.utils_data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UtilsDao {

    @Insert
    suspend fun insert(utilsSet: UtilsEntity)

    @Query("SELECT * FROM utils")
    fun getUtils(): List<UtilsEntity>

}