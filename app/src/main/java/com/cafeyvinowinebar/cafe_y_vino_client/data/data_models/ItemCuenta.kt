package com.cafeyvinowinebar.cafe_y_vino_client.data.data_models

/**
 * Represent an item the bill consists of
 * Model for the AdapterCanasta
 */

data class ItemCuenta(
    val name: String? = null,
    val price: Long? = null,
    val count: Long? = null,
    val total: Long? = null
) {
}