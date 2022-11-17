package com.cafeyvinowinebar.cafe_y_vino_client.data.data_models

/**
 * Represents a category of the menu
 * Model for the AdapterMainMenu
 */

data class MenuCategoryFirestore(
    val name: String? = null,
    val image: String? = null,
    val catPath: String? = null
) {}