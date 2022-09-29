package com.cafeyvinowinebar.cafe_y_vino_client.data.model_classes

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Parcelize
@Keep
data class User(
    val nombre: String,
    val telefono: String,
    val email: String,
    val token: String,
    val mesa: String,
    val isPresent: Boolean,
    val bonos: Long,
    val fechaDeNacimiento: String) : Parcelable{
}