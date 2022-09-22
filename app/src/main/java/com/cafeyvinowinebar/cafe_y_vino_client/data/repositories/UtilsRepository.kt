package com.cafeyvinowinebar.cafe_y_vino_client.data.repositories

import com.cafeyvinowinebar.cafe_y_vino_client.data.model_classes.UtilsEntryRequest
import com.cafeyvinowinebar.cafe_y_vino_client.data.sources.FirebaseAuthSource
import com.cafeyvinowinebar.cafe_y_vino_client.data.sources.FirebaseFirestoreSource
import com.cafeyvinowinebar.cafe_y_vino_client.data.sources.FirebaseMessagingSource
import javax.inject.Inject

class UtilsRepository @Inject constructor(
    private val fAuthSource: FirebaseAuthSource,
    private val fMessagingSource: FirebaseMessagingSource,
    private val fStoreSource: FirebaseFirestoreSource
) {
    suspend fun getUtilsForEntryRequest(): UtilsEntryRequest {
        val utilsDoc = fStoreSource.getUtilsDoc()
        return UtilsEntryRequest(
            offDays = utilsDoc.diasDeDescanso,
            attendanceHours = utilsDoc.horasDeAtencion
        )
    }



}