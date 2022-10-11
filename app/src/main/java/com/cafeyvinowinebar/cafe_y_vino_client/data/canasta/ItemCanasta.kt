package com.cafeyvinowinebar.cafe_y_vino_client.data.canasta

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cafeyvinowinebar.cafe_y_vino_client.data.data_models.ItemPedido


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

fun ItemCanasta.asItemPedido(itemCount: Long): ItemPedido =
    ItemPedido(
        name = name,
        count = itemCount,
        price = price,
        category = category
    )