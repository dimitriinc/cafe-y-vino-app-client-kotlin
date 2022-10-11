package com.cafeyvinowinebar.cafe_y_vino_client.data.sources

import com.cafeyvinowinebar.cafe_y_vino_client.*
import com.cafeyvinowinebar.cafe_y_vino_client.data.data_models.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
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
        .document(fAuth.getUserObject()!!.uid)
        .snapshotAsFlow()

    /**
     * Transmits the utils data as flow
     */
    val utilsFlow: Flow<DocumentSnapshot?> = fStore.collection("utils")
        .document("horas")
        .snapshotAsFlow()

    /**
     * Creates a flow with calculated total cost of the user's cuenta
     */
    fun getCuentaTotalFlow(userId: String): Flow<Long?> {
        val currentDate = getCurrentDate()
        return fStore.collection("cuentas")
            .document(currentDate)
            .collection("cuentas corrientes")
            .document(userId)
            .collection("cuenta")
            .asCuentaTotalFlow()
    }


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
     * Stores a reservation document according to its date and part of day, the id of the document is its table's number
     */
    suspend fun setReservaDoc(
        reserva: ReservaFirestore
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

    /**
     * Stores a new pedido in the DB
     * First a document with its metadata
     * Then, iterating through the pedido set, each of its items
     */
    fun storePedido(
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

    /**
     * After a new user is registered, we save a document with some personal and necessary for the functioning of the app data
     */
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

    // TODO: those three should be performed by a work manager, updates are stored to the Room first
    suspend fun updateNombre(nombre: String): Boolean {
        return try {
            fStore.collection("usuarios")
                .document(fAuth.getUserObject()!!.uid)
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
                .document(fAuth.getUserObject()!!.uid)
                .update("telefono", telefono)
                .await()
            true
        } catch (e: Throwable) {
            false
        }
    }
    fun updateBonos(newBonos: Long) {
        fStore.collection("usuarios")
            .document(fAuth.getUserObject()!!.uid)
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

    /**
     * Gets the collection of reservations for the defined date and part of day
     * If there is no reservations for that set, the collection doesn't exist, and we return a null
     */
    suspend fun getReservasQuery(fecha: String?, part: String?): QuerySnapshot? {
        return try {
            fStore.collection("reservas").document(fecha!!).collection(part!!).get().await()
        } catch (e: Throwable) {
            null
        }
    }
}