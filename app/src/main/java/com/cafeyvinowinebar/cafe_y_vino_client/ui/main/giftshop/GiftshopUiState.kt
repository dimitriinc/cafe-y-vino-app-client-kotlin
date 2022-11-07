package com.cafeyvinowinebar.cafe_y_vino_client.ui.main.giftshop

data class GiftshopUiState(
    val isUserPresent: Boolean = false,
    val bonos: Long = 0,
    val userFirstName: String = "",
    val userName: String = ""
) {
}