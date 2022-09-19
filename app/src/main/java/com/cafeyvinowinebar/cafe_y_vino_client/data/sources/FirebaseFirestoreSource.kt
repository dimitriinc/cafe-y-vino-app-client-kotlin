package com.cafeyvinowinebar.cafe_y_vino_client.data.sources

import android.os.Bundle
import com.cafeyvinowinebar.cafe_y_vino_client.KEY_HAPPY_DAYS
import com.cafeyvinowinebar.cafe_y_vino_client.KEY_HAPPY_HOURS
import com.cafeyvinowinebar.cafe_y_vino_client.asFlow
import com.cafeyvinowinebar.cafe_y_vino_client.data.model_classes.ItemPedido
import com.cafeyvinowinebar.cafe_y_vino_client.data.model_classes.PedidoMetaDoc
import com.cafeyvinowinebar.cafe_y_vino_client.data.model_classes.Reserva
import com.cafeyvinowinebar.cafe_y_vino_client.data.model_classes.User
import com.cafeyvinowinebar.cafe_y_vino_client.di.ApplicationScope
import com.cafeyvinowinebar.cafe_y_vino_client.getCurrentDate
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import java.lang.Exception as LangException

class FirebaseFirestoreSource @Inject constructor(
    private val fStore: FirebaseFirestore,
    private val fAuth: FirebaseAuthSource,
    @ApplicationScope private val applicationScope: CoroutineScope
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    val userPresence: Flow<Boolean?> = fStore.collection("usuarios")
        .document(fAuth.getUserId())
        .asFlow()

    private suspend fun getUtilsDocument(): DocumentSnapshot = fStore.collection("utils")
        .document("horas")
        .get()
        .await()

    suspend fun getPartHoras(): List<String> {
        return try {
            val snapshot = getUtilsDocument()
            snapshot.get("horas dia noche") as List<String>
        } catch (e: Exception) {
            listOf("[14:00 - 18:00]", "[18:00 - 22:00]")
        }
    }

    suspend fun getSetOfReservas(date: String?, part: String?): QuerySnapshot? {

        return when (part) {
            "dia" -> fStore.collection("reservas")
                .document(date!!)
                .collection("dia")
                .get()
                .await()
            "noche" -> fStore.collection("reservas")
                .document(date!!)
                .collection("noche")
                .get()
                .await()
            else -> null

        }
    }

    suspend fun getHappyHourData(): Bundle {
        val snapshot = getUtilsDocument()
        val happyDays = snapshot.get("dias de happy hour") as CharArray
        val happyHours = snapshot.get("horas de happy hour") as LongArray
        val bundle = Bundle()
        bundle.putCharArray(KEY_HAPPY_DAYS, happyDays)
        bundle.putLongArray(KEY_HAPPY_HOURS, happyHours)
        return bundle
    }

    suspend fun getSetOfAvailableReservaHours(part: String?): List<String> {
        val horasSnapshot = getUtilsDocument()
        return horasSnapshot.get("horas de reserva ($part)") as List<String>
    }

    suspend fun getUserDocById(uid: String): DocumentSnapshot =
        fStore.collection("usuarios").document(uid).get().await()

    suspend fun getAdminTokens(): List<String> {
        val admins = fStore.collection("administradores").get().await()
        return admins.map {
            it.getString("token")!!
        }
    }

    suspend fun setReservaDoc(
        reserva: Reserva
    ): Boolean {
        return try {
            fStore.collection("reservas")
                .document(reserva.fecha!!)
                .collection(reserva.parte!!)
                .document(reserva.mesa!!)
                .set(reserva)
                .await()
            true
        } catch (e: LangException) {
            false
        }

    }

    fun setPedido(
        metaDocId: String,
        pedidoSet: Set<ItemPedido>,
        metaDoc: PedidoMetaDoc,
        currentDate: String
    ) {

        fStore.collection("pedidos")
            .document(currentDate)
            .collection("pedidos enviados")
            .document(metaDocId)
            .set(metaDoc)

        pedidoSet.forEach {
            fStore.collection("pedidos")
                .document(currentDate)
                .collection("pedidos enviados")
                .document(metaDocId)
                .collection("pedido")
                .add(it)
        }
    }

    suspend fun storeUserDoc(
        user: User,
        userId: String
    ): Boolean {
        return try {
            fStore.collection("usuarios").document(userId).set(user).await()
            true
        } catch (e: Exception) {
            false
        }
    }

}