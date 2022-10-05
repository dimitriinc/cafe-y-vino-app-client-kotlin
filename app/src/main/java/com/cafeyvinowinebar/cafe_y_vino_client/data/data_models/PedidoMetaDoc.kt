package com.cafeyvinowinebar.cafe_y_vino_client.data.data_models

import com.google.firebase.Timestamp

data class PedidoMetaDoc(
    val isExpended: Boolean = false,
    val mesa: String,
    val servido: Boolean = false,
    val servidoBarra: Boolean,
    val servidoCocina: Boolean,
    val user: String,
    val userId: String,
    val timestamp: Timestamp
) {
}