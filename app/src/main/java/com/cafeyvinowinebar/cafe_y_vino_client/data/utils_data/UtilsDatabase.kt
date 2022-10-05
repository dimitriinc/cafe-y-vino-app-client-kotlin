package com.cafeyvinowinebar.cafe_y_vino_client.data.utils_data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [UtilsEntity::class], version = 1)
abstract class UtilsDatabase : RoomDatabase() {
    abstract fun utilsDao(): UtilsDao
}