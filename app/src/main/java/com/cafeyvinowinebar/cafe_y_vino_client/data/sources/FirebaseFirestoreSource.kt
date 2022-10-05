package com.cafeyvinowinebar.cafe_y_vino_client.data.sources

import com.cafeyvinowinebar.cafe_y_vino_client.*
import com.cafeyvinowinebar.cafe_y_vino_client.data.data_models.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import java.lang.Exception as LangException

/**
 * Reads and writes from the Firestore DB
 */
class FirebaseFirestoreSource @Inject constructor(
    private val fStore: FirebaseFirestore,
    private val fAuth: FirebaseAuthSource
) {

    /**
     * Transmits the user's document snapshot as a flow
     */
    val userFlow: Flow<DocumentSnapshot?> = fStore.collection("usuarios")
        .document(fAuth.getUserId())
        .snapshotAsFlow()


    /**
     * Gets a list of admin tokens as strings for the messaging source to send them messages
     */
    suspend fun getAdminTokens(): List<String> {
        val admins = fStore.collection("administradores").get().await()
        return admins.map {
            it.getString("token")!!
        }
    }

    /**
     * One-off operation to get the utils doc, so that the utils repo can store it in a Room table at the beginning of the app's process
     */
    suspend fun getUtilsDoc(): DocumentSnapshot? {
        return try {
            fStore.collection("utils")
                .document("horas")
                .get()
                .await()
        } catch (error: Throwable) {
            null
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
        user: UserFirestore,
        userId: String
    ): Boolean {
        return try {
            fStore.collection("usuarios").document(userId).set(user).await()
            true
        } catch (e: Throwable) {
            false
        }
    }

    suspend fun updateNombre(nombre: String): Boolean {
        return try {
            fStore.collection("usuarios")
                .document(fAuth.getUserId())
                .update("nombre", nombre)
                .await()
            true
        } catch (e: Throwable) {
            false
        }
    }

    suspend fun updateTelefono(telefono: String): Boolean {
        return try {
            fStore.collection("usuarios")
                .document(fAuth.getUserId())
                .update("telefono", telefono)
                .await()
            true
        } catch (e: Throwable) {
            false
        }
    }

    fun updateBonos(newBonos: Long) {
        fStore.collection("usuarios")
            .document(fAuth.getUserId())
            .update("bonos", newBonos)
    }

    /**
     * Receives an instance of a Gift class and stores to the "pedidos" collection
     */
    fun storeGift(gift: GiftToSend) {
        fStore.collection("pedidos")
            .document(getCurrentDate())
            .collection("regalos")
            .add(gift)
    }

}