package com.cafeyvinowinebar.cafe_y_vino_client.data.canasta

import androidx.room.Entity
import androidx.room.PrimaryKey


/**
 * Represents a building block of orders as seen by customers
 * Stored in a SQLite database
 * Model for the AdapterCanasta
 */

@Entity(tableName = "canasta")
data class ItemCanasta(
    val name: String,
    val category: String,
    val icon: String?,
    val price: Long,
    @PrimaryKey(autoGenerate = true) val id: Int = 0
) {
}