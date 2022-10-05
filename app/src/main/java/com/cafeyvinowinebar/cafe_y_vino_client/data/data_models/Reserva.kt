package com.cafeyvinowinebar.cafe_y_vino_client.data.data_models

import com.google.firebase.Timestamp

class Reserva(
    val nombre: String? = null,
    val telefono: String? = null,
    val fecha: String? = null,
    val mesa: String? = null,
    val hora: String? = null,
    val pax: String? = null,
    val parte: String? = null,
    val comentario: String? = null,
    val llegado: Boolean = false,
    val userId: String? = null,
    val timestamp: Timestamp? = null,
    val confirmado: Boolean = false
) {
}