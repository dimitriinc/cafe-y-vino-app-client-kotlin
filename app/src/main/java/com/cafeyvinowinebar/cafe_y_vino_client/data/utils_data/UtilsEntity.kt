package com.cafeyvinowinebar.cafe_y_vino_client.data.utils_data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "utils")
data class UtilsEntity(
    @ColumnInfo(name = "days_off") val diasDeDescanso: List<String>,
    @ColumnInfo(name = "days_happy") val diasDeHappyHour: List<String>,
    @ColumnInfo(name = "horas_attendance") val horasDeAtencion: List<Long>,
    @ColumnInfo(name = "horas_happy") val horasDeHappyHour: List<Long>,
    @ColumnInfo(name = "horas_reserva_dia") val horasDeReservaDia: List<String>,
    @ColumnInfo(name = "horas_reserva_noche") val horasDeReservaNoche: List<String>,
    @ColumnInfo(name = "horas_sets")val horasDiaNoche: List<String>,
    @PrimaryKey val id: Int = 0
)