package com.cafeyvinowinebar.cafe_y_vino_client.data.model_classes

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class UtilsHoras(
    val diasDeDescanso: List<String>,
    val diasDeHappyHour: List<String>,
    val horasDeAtencion: List<Long>,
    val horasDeHappyHour: List<Long>,
    val horasDeReservaDia: List<String>,
    val horasDeReservaNoche: List<String>,
    val horasDiaNoche: List<String>
) : Parcelable

fun UtilsHoras.asUtilsEntryRequest() = UtilsEntryRequest(
    offDays = diasDeDescanso,
    attendanceHours = horasDeAtencion
)

fun UtilsHoras.asUtilsHappyHour() = UtilsHappyHour(
    happyDays = diasDeHappyHour,
    happyHours = horasDeHappyHour
)

fun UtilsHoras.asUtilsReservas() = UtilsReservas(
    availableHoursDia = horasDeReservaDia,
    availableHoursNoche = horasDeReservaNoche,
    setsOfHours = horasDiaNoche
)
