package com.cafeyvinowinebar.cafe_y_vino_client.ui.introduction

data class IntroUiState(
    val isRegistered: Boolean = false,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null,
    val message: String? = null
)
