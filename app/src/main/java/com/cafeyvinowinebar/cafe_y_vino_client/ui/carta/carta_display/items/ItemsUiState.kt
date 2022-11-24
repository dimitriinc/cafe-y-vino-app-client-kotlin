package com.cafeyvinowinebar.cafe_y_vino_client.ui.carta.carta_display.items

import com.cafeyvinowinebar.cafe_y_vino_client.data.data_models.ItemMenuFirestore

data class ItemsUiState(
    val isPresent: Boolean = false,
    val areItemsFabsExpanded: Boolean = false,
    val items: ArrayList<ItemMenuFirestore>? = null,
    val initialPosition: Int = 0,
    val itemsSize: Int = 0,
    val areSpecsFabsExpanded: Boolean = false
) {
}