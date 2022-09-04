package com.cafeyvinowinebar.cafe_y_vino_client.data.canasta

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ItemCanasta::class], version = 1)
abstract class CanastaDatabase : RoomDatabase() {
    abstract fun canastaDao(): CanastaDao
}