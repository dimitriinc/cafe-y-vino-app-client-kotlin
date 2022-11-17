package com.cafeyvinowinebar.cafe_y_vino_client.services

import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.cafeyvinowinebar.cafe_y_vino_client.*
import com.cafeyvinowinebar.cafe_y_vino_client.data.PreferencesManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

private const val TAG = "MESSAGING_SERVICE"
@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var fAuth: FirebaseAuth
    @Inject
    lateinit var fStore: FirebaseFirestore
    @Inject
    lateinit var preferencesManager: PreferencesManager

    lateinit var manager: NotificationManagerCompat

    private val serviceJob = Job()

    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onCreate() {
        super.onCreate()
        manager = NotificationManagerCompat.from(this)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val user = fAuth.currentUser
        if (user != null) {
            fStore.collection("usuarios").document(user.uid).update(KEY_TOKEN, token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d(TAG, "onMessageReceived: the action of the message: ${message.data["action"]}")

        when (message.data[KEY_ACTION]) {

            ACTION_PUERTA_ADMIN -> processPuertaAck(message)
            ACTION_RESERVA_ACK -> processReservaAck(message)
            ACTION_RESERVA_NACK -> processReservaNack(message)
            ACTION_PEDIDO_ADMIN -> processPedidoAck()
            ACTION_CUENTA_ACK -> processCuentaAck()
            ACTION_CUENTA_ADMIN -> processCuentaMessage(message)
            ACTION_REGALO_ADMIN -> processRegaloAck()
            ACTION_MSG -> processMsg(message)
        }
    }

    private fun processPuertaAck(message: RemoteMessage) {

        val firstName =
            message.data[KEY_NOMBRE]!!.split(" ")[0].lowercase().replaceFirstChar { it.uppercase() }

        val builder = NotificationCompat.Builder(this, PUERTA)
            .setContentTitle(getString(R.string.noti_puerta_title))
            .setSmallIcon(R.drawable.logo_mini)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(getString(R.string.noti_puerta_text, firstName))
            )
            .setColor(getColor(R.color.disco))

        with(NotificationManagerCompat.from(this)) {
            notify(Random.nextInt(), builder.build())
        }

        serviceScope.launch { preferencesManager.updateCanSendPedidos(true) }
    }

    private fun processReservaAck(message: RemoteMessage) {

        val fecha = message.data[KEY_FECHA]
        val hora = message.data[KEY_HORA]
        val pax = message.data[KEY_PAX]

        val builder = NotificationCompat.Builder(this, RESERVA)
            .setContentTitle(getString(R.string.noti_reserva_ack_title))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(getString(R.string.noti_reserva_ack_text, fecha, hora, pax))
            )
            .setSmallIcon(R.drawable.logo_mini)
            .setColor(getColor(R.color.disco))

        with(NotificationManagerCompat.from(this)) {
            notify(Random.nextInt(), builder.build())
        }
    }

    private fun processReservaNack(message: RemoteMessage) {

        val fecha = message.data[KEY_FECHA]
        val hora = message.data[KEY_HORA]
        val comentario = message.data[KEY_COMENTARIO]

        val builder = NotificationCompat.Builder(this, RESERVA)
            .setContentTitle(getString(R.string.noti_reserva_nack_title))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(getString(R.string.noti_reserva_nack_text, fecha, hora, comentario))
            )
            .setSmallIcon(R.drawable.logo_mini)
            .setColor(getColor(R.color.disco))

        with(NotificationManagerCompat.from(this)) {
            notify(Random.nextInt(), builder.build())
        }
    }

    private fun processPedidoAck() {

        val builder = NotificationCompat.Builder(this, PEDIDO)
            .setContentTitle(getString(R.string.noti_pedido_title))
            .setContentText(getString(R.string.noti_pedido_text))
            .setSmallIcon(R.drawable.logo_mini)
            .setColor(getColor(R.color.disco))

        with(NotificationManagerCompat.from(this)) {
            notify(Random.nextInt(), builder.build())
        }
    }

    private fun processCuentaAck() {

        val builder = NotificationCompat.Builder(this, CUENTA)
            .setContentTitle(getString(R.string.noti_cuenta_title))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(getString(R.string.noti_cuenta_text))
            )
            .setSmallIcon(R.drawable.logo_mini)
            .setColor(getColor(R.color.disco))

        with(NotificationManagerCompat.from(this)) {
            notify(Random.nextInt(), builder.build())
        }
    }

    private fun processCuentaMessage(message: RemoteMessage) {

        val firstName =
            message.data[KEY_NOMBRE]!!.split(" ")[0].lowercase().replaceFirstChar { it.uppercase() }
        val bono = message.data[KEY_BONO]

        val builder = NotificationCompat.Builder(this, CUENTA)
            .setContentTitle(getString(R.string.noti_farewell_title))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(getString(R.string.noti_farewell_text, firstName, bono))
            )
            .setSmallIcon(R.drawable.logo_mini)
            .setColor(getColor(R.color.disco))

        with(NotificationManagerCompat.from(this)) {
            notify(Random.nextInt(), builder.build())
        }

        serviceScope.launch { preferencesManager.updateCanSendPedidos(true) }
    }

    private fun processRegaloAck() {

        val builder = NotificationCompat.Builder(this, PEDIDO)
            .setContentTitle(getString(R.string.noti_pedido_title))
            .setContentText(getString(R.string.noti_pedido_text))
            .setSmallIcon(R.drawable.logo_mini)
            .setColor(getColor(R.color.disco))

        with(NotificationManagerCompat.from(this)) {
            notify(Random.nextInt(), builder.build())
        }
    }

    private fun processMsg(message: RemoteMessage) {

        Log.d(TAG, "processMsg: the builder is about to start")

        val builder = NotificationCompat.Builder(this, DATOS)
            .setContentTitle(getString(R.string.noti_msg_title))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(message.data[ACTION_MSG])
            )
            .setSmallIcon(R.drawable.logo_mini)
            .setColor(getColor(R.color.disco))

        Log.d(TAG, "processMsg: the builder has been initialized")

        try {
            with(NotificationManagerCompat.from(this)) {
                notify(Random.nextInt(), builder.build())
            }
        } catch (e: Throwable) {
            Log.d(TAG, "processMsg: the notifying has failed, \nexception: ${e.printStackTrace()}")
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}