package com.cafeyvinowinebar.cafe_y_vino_client.ui.main.home

data class HomeUiState(
    val isLoggedIn: Boolean = true,
    val isUserPresent: Boolean = false,
    val canUserSendEntryRequest: Boolean? = null
)