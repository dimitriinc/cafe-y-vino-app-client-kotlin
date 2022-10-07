package com.cafeyvinowinebar.cafe_y_vino_client.data.sources

import com.cafeyvinowinebar.cafe_y_vino_client.*
import com.cafeyvinowinebar.cafe_y_vino_client.data.data_models.ReservaFirestore
import com.cafeyvinowinebar.cafe_y_vino_client.getFirebaseMessageId
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.remoteMessage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseMessagingSource @Inject constructor(
    private val fMessaging: FirebaseMessaging,
    private val fStore: FirebaseFirestoreSource
) {

    suspend fun getToken(): String = fMessaging.token.await()

    suspend fun sendReservaMessage(reserva: ReservaFirestore) {
        val userToken = fMessaging.token.await()
        val adminTokens = fStore.getAdminTokens()
        adminTokens.forEach {
            fMessaging.send(remoteMessage("$SENDER_ID@fcm.googleapis.com") {
                messageId = getFirebaseMessageId()
                addData(KEY_ACTION, ACTION_RESERVA)
                addData(KEY_TOKEN, userToken)
                addData(KEY_NOMBRE, reserva.nombre)
                addData(KEY_FECHA, reserva.fecha)
                addData(KEY_MESA, reserva.mesa)
                addData(KEY_HORA, reserva.hora)
                addData(KEY_PAX, reserva.pax)
                addData(KEY_ADMIN_TOKEN, it)
                addData(KEY_PARTE, reserva.parte)
                addData(KEY_TYPE, TO_ADMIN_NEW)
                addData(KEY_COMENTARIO, reserva.comentario)
            })
        }

    }

    suspend fun sendPedidoMessage(
        metaDocId: String,
        currentDate: String,
        mesa: String,
        userNombre: String,
    ) {
        val userToken = fMessaging.token.await()
        val adminTokens = fStore.getAdminTokens()
        adminTokens.forEach { adminToken ->
            fMessaging.send(remoteMessage("$SENDER_ID@fcm.googleapis.com") {
                messageId = getFirebaseMessageId()
                addData(KEY_MESA, mesa)
                addData(KEY_TOKEN, userToken)
                addData(KEY_ADMIN_TOKEN, adminToken)
                addData(KEY_NOMBRE, userNombre)
                addData(KEY_FECHA, currentDate)
                addData(KEY_META_ID, metaDocId)
                addData(KEY_ACTION, ACTION_PEDIDO)
                addData(KEY_TYPE, TO_ADMIN_NEW)
            })
        }
    }

    suspend fun sendCuentaMessage(
        payMode: String,
        userNombre: String,
        mesa: String
    ) {
        val userToken = fMessaging.token.await()
        val adminTokens = fStore.getAdminTokens()
        adminTokens.forEach { adminToken ->
            fMessaging.send(remoteMessage("$SENDER_ID@fcm.googleapis.com") {
                messageId = getFirebaseMessageId()
                addData(KEY_TOKEN, userToken)
                addData(KEY_ADMIN_TOKEN, adminToken)
                addData(KEY_ACTION, ACTION_CUENTA)
                addData(KEY_TYPE, TO_ADMIN_NEW)
                addData(KEY_MODO, payMode)
                addData(KEY_MESA, mesa)
                addData(KEY_NOMBRE, userNombre)
            })
        }
    }

    suspend fun sendEntryRequest(
        userId: String,
        userToken: String,
        userName: String
    ) {
        val adminTokens = fStore.getAdminTokens()
        adminTokens.forEach { adminToken ->
            fMessaging.send(remoteMessage("$SENDER_ID@fcm.googleapis.com") {
                messageId = getFirebaseMessageId()
                addData(KEY_TOKEN, userToken)
                addData(KEY_ADMIN_TOKEN, adminToken)
                addData(KEY_ACTION, ACTION_PUERTA)
                addData(KEY_TYPE, TO_ADMIN_NEW)
                addData(KEY_NOMBRE, userName)
                addData(KEY_USER_ID, userId)
            })
        }
    }

    suspend fun sendGiftMessage(
        regalo: String,
        mesa: String,
        nombre: String
    ) {
        val adminTokens = fStore.getAdminTokens()
        adminTokens.forEach { adminToken ->
            fMessaging.send(remoteMessage("$SENDER_ID@fcm.googleapis.com") {
                messageId = getFirebaseMessageId()
                addData(KEY_REGALO, regalo)
                addData(KEY_ACTION, ACTION_REGALO)
                addData(KEY_MESA, mesa)
                addData(KEY_ADMIN_TOKEN, adminToken)
                addData(KEY_TYPE, TO_ADMIN_NEW)
                addData(KEY_NOMBRE, nombre)
            })
        }
    }
}