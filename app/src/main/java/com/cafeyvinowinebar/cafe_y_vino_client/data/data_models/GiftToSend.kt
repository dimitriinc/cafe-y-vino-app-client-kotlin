package com.cafeyvinowinebar.cafe_y_vino_client.data.data_models

import com.google.firebase.Timestamp

data class GiftToSend(
    val nombre: String,
    val user: String,
    val userId: String,
    val mesa: String,
    val precio: Long,
    val servido: Boolean,
    val timestamp: Timestamp
)
