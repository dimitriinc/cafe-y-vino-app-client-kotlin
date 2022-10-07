package com.cafeyvinowinebar.cafe_y_vino_client.data.repositories

import com.cafeyvinowinebar.cafe_y_vino_client.data.data_models.ReservaFirestore
import com.cafeyvinowinebar.cafe_y_vino_client.data.sources.FirebaseFirestoreSource
import com.google.firebase.firestore.QuerySnapshot
import javax.inject.Inject

class ReservasDataRepository @Inject constructor(
    private val fStoreSource: FirebaseFirestoreSource,
) {
    /**
     * Passes the reserva further to store it into the Firebase DB
     */
    suspend fun setReservaDoc(reserva: ReservaFirestore): Boolean =
        fStoreSource.setReservaDoc(reserva)

    /**
     * Requests a set of reservas from the fStore source
     */
    suspend fun getSetOfReservas(fecha: String?, part: String?): QuerySnapshot? {
        return fStoreSource.getReservasQuery(fecha, part)
    }
}