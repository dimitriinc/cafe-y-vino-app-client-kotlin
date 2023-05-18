package com.cafeyvinowinebar.cafe_y_vino_client.data.repositories

import android.util.Log
import com.cafeyvinowinebar.cafe_y_vino_client.data.data_models.EmailUtils
import com.cafeyvinowinebar.cafe_y_vino_client.ui.data_models.UtilsEntryRequest
import com.cafeyvinowinebar.cafe_y_vino_client.ui.data_models.UtilsHappyHour
import com.cafeyvinowinebar.cafe_y_vino_client.ui.data_models.UtilsReservas
import com.cafeyvinowinebar.cafe_y_vino_client.data.sources.FirebaseFirestoreSource
import com.cafeyvinowinebar.cafe_y_vino_client.data.utils_data.UtilsDao
import com.cafeyvinowinebar.cafe_y_vino_client.data.utils_data.UtilsEntity
import com.cafeyvinowinebar.cafe_y_vino_client.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "UtilsRepository"
/**
 * A repository for data related to access to some functionalities, like sending an entry request and adding happy hour products to canasta
 * And also the data concerning the available hours for reservations
 */
class UtilsRepository @Inject constructor(
    private val fStoreSource: FirebaseFirestoreSource,
    private val utilsDao: UtilsDao,
    @ApplicationScope private val appScope: CoroutineScope
) {

    /**
     * We want to automatically update utils by listening to a document in the Firestore w/o specifically calling any function
     */
    init {
        appScope.launch {
            fStoreSource.getUtilsFlow()
                .catch {
                    currentCoroutineContext().cancel(null)
                }
                .collect { utilsDoc ->
                    if (utilsDoc != null) {
                        utilsDao.insert(
                            UtilsEntity(
                                diasDeDescanso = utilsDoc.get("dias de descanso") as List<String>,
                                diasDeHappyHour = utilsDoc.get("dias de happy hour") as List<String>,
                                horasDeAtencion = utilsDoc.get("horas de atencion") as List<Long>,
                                horasDeHappyHour = utilsDoc.get("horas de happy hour") as List<Long>,
                                horasDeReservaDia = utilsDoc.get("horas de reserva (dia)") as List<String>,
                                horasDeReservaNoche = utilsDoc.get("horas de reserva (noche)") as List<String>,
                                horasDiaNoche = utilsDoc.get("horas dia_noche") as List<String>
                            )
                        )
                    }
                }
        }
    }

    /**
     * Three functions to get utils specific for each purpose
     */
    fun getUtilsForEntryRequest(): UtilsEntryRequest {
        val utils = utilsDao.getUtils()[0]
        return UtilsEntryRequest(
            offDays = utils.diasDeDescanso,
            attendanceHours = utils.horasDeAtencion
        )
    }

    fun getUtilsForHappyHour(): Flow<UtilsHappyHour> = utilsDao.getUtilsFlow()
        .catch {
            cause: Throwable ->  throw cause
        }
        .map {
            Log.d(TAG, "getUtilsForHappyHour: WE ARE AT THE MAP BLOCK")
            val utils = it[0]
            UtilsHappyHour(
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

    suspend fun getEmailUtils(): EmailUtils {
        val snapshot = fStoreSource.getEmailUtils()
        return EmailUtils(
            recipient = snapshot.getString("recipiente") ?: "elliotponsic@hotmail.fr",
            password = snapshot.getString("pass") ?: "1234"
        )
    }

}