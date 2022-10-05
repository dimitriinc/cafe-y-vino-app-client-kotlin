package com.cafeyvinowinebar.cafe_y_vino_client.data.repositories

import com.cafeyvinowinebar.cafe_y_vino_client.ui.data_models.UtilsEntryRequest
import com.cafeyvinowinebar.cafe_y_vino_client.ui.data_models.UtilsHappyHour
import com.cafeyvinowinebar.cafe_y_vino_client.ui.data_models.UtilsReservas
import com.cafeyvinowinebar.cafe_y_vino_client.data.sources.FirebaseFirestoreSource
import com.cafeyvinowinebar.cafe_y_vino_client.data.utils_data.UtilsDao
import com.cafeyvinowinebar.cafe_y_vino_client.data.utils_data.UtilsEntity
import com.cafeyvinowinebar.cafe_y_vino_client.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class UtilsRepository @Inject constructor(
    private val fStoreSource: FirebaseFirestoreSource,
    private val utilsDao: UtilsDao,
    @ApplicationScope private val appScope: CoroutineScope
) {

    init {
        appScope.launch {
            val utilsDoc = fStoreSource.getUtilsDoc()
            if (utilsDoc == null) {
                return@launch
            } else {
                utilsDao.insert(UtilsEntity(
                    diasDeDescanso = utilsDoc.get("dias de descanso") as List<String>,
                    diasDeHappyHour = utilsDoc.get("dias de happy hour") as List<String>,
                    horasDeAtencion = utilsDoc.get("horas de atencion") as List<Long>,
                    horasDeHappyHour = utilsDoc.get("horas de happy hour") as List<Long>,
                    horasDeReservaDia = utilsDoc.get("horas de reserva (dia)") as List<String>,
                    horasDeReservaNoche = utilsDoc.get("horas de reserva (noche)") as List<String>,
                    horasDiaNoche = utilsDoc.get("horas dia_noche") as List<String>
                ))
            }

        }
    }

    fun getUtilsForEntryRequest(): UtilsEntryRequest {
        val utils = utilsDao.getUtils()[0]
        return UtilsEntryRequest(
            offDays = utils.diasDeDescanso,
            attendanceHours = utils.horasDeAtencion
        )
    }

    fun getUtilsForHappyHour(): UtilsHappyHour {
        val utils = utilsDao.getUtils()[0]
        return UtilsHappyHour(
            happyDays = utils.diasDeHappyHour,
            happyHours = utils.horasDeHappyHour
        )
    }

    fun getUtilsForReservas(): UtilsReservas {
        val utils = utilsDao.getUtils()[0]
        return UtilsReservas(
            availableHoursDia = utils.horasDeReservaDia,
            availableHoursNoche = utils.horasDeReservaNoche,
            setsOfHours = utils.horasDiaNoche
        )
    }

}