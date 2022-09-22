package com.cafeyvinowinebar.cafe_y_vino_client.data.model_classes

import kotlinx.android.parcel.Parcelize

@Parcelize
data class UtilsHoras(
    val diasDeDescanso: List<String>,
    val diasDeHappyHour: List<String>,
    val horasDeAtencion: List<Long>,
    val horasDeHappyHour: List<Long>,
    val horasDeReservaDia: List<String>,
    val horasDeReservaNoche: List<String>,
    val horasDiaNoche: List<String>
)
