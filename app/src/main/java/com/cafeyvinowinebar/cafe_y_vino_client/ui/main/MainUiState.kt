package com.cafeyvinowinebar.cafe_y_vino_client.ui.main

import com.cafeyvinowinebar.cafe_y_vino_client.data.model_classes.UtilsEntryRequest

data class MainUiState(
    val isLoggedIn: Boolean = false,
    val isUserPresent: Boolean = false,
    val canUserSendEntryRequest: Boolean? = null
) {
}