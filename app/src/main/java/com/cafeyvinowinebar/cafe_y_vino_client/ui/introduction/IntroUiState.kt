package com.cafeyvinowinebar.cafe_y_vino_client.ui.introduction

data class IntroUiState(
    val isRegistered: Boolean = false,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null,
    val message: String? = null,
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val birthdate: String = "",
    val password: String = "",
    val privacyPolicyVisited: Boolean = false,
    val progressBarVisible: Boolean = false
)
