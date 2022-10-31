package com.cafeyvinowinebar.cafe_y_vino_client.data.data_models

import com.google.firebase.firestore.PropertyName

data class UserFirestore(
    val nombre: String,
    val telefono: String,
    val email: String,
    val token: String,
    val mesa: String,
    @PropertyName("isPresent") val isPresent: Boolean,
    val bonos: Long,
    val fechaDeNacimiento: String
)
