package com.cafeyvinowinebar.cafe_y_vino_client.data.data_models

/**
 * Represents a building block of orders as seen by the administration
 * ItemCanasta becomes ItemPedido when stored to the FirestoreDB
 * Multiple ItemCanasta with the same name are merged in one ItemPedido with the appropriate 'count' value
 *
 * The objects of this class will be used in a Set to prevent duplication, that's why we override equals() and hashCode()
 */

class ItemPedido(
    val name: String,
    val category: String,
    val price: Long,
    val count: Long
) {
}