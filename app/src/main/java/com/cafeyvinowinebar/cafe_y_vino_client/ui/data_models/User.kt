package com.cafeyvinowinebar.cafe_y_vino_client.ui.data_models

data class User(
    val nombre: String,
    val telefono: String,
    val email: String,
    val token: String,
    val mesa: String,
    val isPresent: Boolean,
    val bonos: Long
    )