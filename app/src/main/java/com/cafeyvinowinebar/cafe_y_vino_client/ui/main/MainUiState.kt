package com.cafeyvinowinebar.cafe_y_vino_client.ui.main

data class MainUiState(
    val isLoggedIn: Boolean = false,
    val isUserPresent: Boolean = false,
    val canUserSendEntryRequest: Boolean? = null,
    val bonos: Long = 0,
    val userName: String = "",
    val message: String? = null
) {
}