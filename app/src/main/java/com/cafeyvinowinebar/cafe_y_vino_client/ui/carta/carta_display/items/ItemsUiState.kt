package com.cafeyvinowinebar.cafe_y_vino_client.ui.carta.carta_display.items

import com.cafeyvinowinebar.cafe_y_vino_client.data.data_models.ItemMenuFirestore
import com.google.firebase.storage.StorageReference

data class ItemsUiState(
    val isPresent: Boolean = false,
    val areItemsFabsExpanded: Boolean = false,
    val items: ArrayList<ItemMenuFirestore>? = null,
    val initialPosition: Int = 0,
    val areSpecsFabsExpanded: Boolean = false,
) {
}