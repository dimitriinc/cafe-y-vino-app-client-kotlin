package com.cafeyvinowinebar.cafe_y_vino_client.data.utils_data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UtilsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(utilsSet: UtilsEntity)

    @Query("SELECT * FROM utils")
    fun getUtils(): List<UtilsEntity>

    @Query("SELECT * FROM utils")
    fun getUtilsFlow(): Flow<List<UtilsEntity>>

}