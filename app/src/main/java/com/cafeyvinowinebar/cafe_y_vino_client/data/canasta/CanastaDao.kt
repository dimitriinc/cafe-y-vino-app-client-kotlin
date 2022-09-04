package com.cafeyvinowinebar.cafe_y_vino_client.data.canasta

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CanastaDao {

    @Insert
    suspend fun insert(item: ItemCanasta)

    @Delete
    suspend fun deleteItem(item: ItemCanasta)

    @Query("SELECT * FROM canasta ORDER BY name ASC")
    fun getAllItems(): Flow<List<ItemCanasta>>

    // deletes all the items of the specified name
    @Query("DELETE FROM canasta WHERE name = :name")
    fun deleteItemsByName(name: String)

    // returns a list of all the items of the specified name
    @Query("SELECT * FROM canasta WHERE name = :name")
    fun getItemsByName(name: String): List<ItemCanasta>

}