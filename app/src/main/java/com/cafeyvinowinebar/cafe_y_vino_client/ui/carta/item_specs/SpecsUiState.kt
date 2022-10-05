package com.cafeyvinowinebar.cafe_y_vino_client.ui.carta.item_specs

import com.cafeyvinowinebar.cafe_y_vino_client.data.data_models.ItemMenu
import com.google.firebase.storage.StorageReference

data class SpecsUiState(
    val isPresent: Boolean = false,
    val items: ArrayList<ItemMenu>? = null,
    val initialPosition: Int = 0,
    val currentPosition: Int? = null,
    val currentItem: ItemMenu? = null,
    val setSize: Int? = null,
    val itemImgReference: StorageReference? = null,
    val areFabsExpended: Boolean = false
)