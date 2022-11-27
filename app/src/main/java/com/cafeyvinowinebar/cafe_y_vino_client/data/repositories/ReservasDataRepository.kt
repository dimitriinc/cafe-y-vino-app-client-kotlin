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
     * Converts it to a list of strings of reserved tables (id of the document in fStore is the table's number)
     * Returns the list so that the viewModel can store it
     */
    suspend fun getSetOfReservas(fecha: String?, part: String?): List<String> {
        val querySnapshot = fStoreSource.getReservasQuery(fecha, part)
        val listOfReservedTables = mutableListOf<String>()
        querySnapshot?.forEach {
            listOfReservedTables.add(it.id)
        }
        return listOfReservedTables
    }
}