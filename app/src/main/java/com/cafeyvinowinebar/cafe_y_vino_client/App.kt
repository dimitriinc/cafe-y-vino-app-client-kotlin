package com.cafeyvinowinebar.cafe_y_vino_client

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

const val PUERTA = "channelPuerta"
const val RESERVA = "channelReserva"
const val PEDIDO = "channelPedido"
const val CUENTA = "channelCuenta"
const val DATOS = "channelDatos"

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channelPuerta = NotificationChannel(
                PUERTA,
                "Puerta",
                NotificationManager.IMPORTANCE_HIGH
            )
            channelPuerta.description = "Solicitudes y confirmaciones de entrada al restaurante"

            val channelReserva = NotificationChannel(
                RESERVA,
                "Reserva",
                NotificationManager.IMPORTANCE_HIGH
            )
            channelReserva.description = "Solicitudes y confirmaciones de las reservaciones"

            val channelPedido = NotificationChannel(
                PEDIDO,
                "Pedido",
                NotificationManager.IMPORTANCE_HIGH
            )
            channelPedido.description = "Enviar y recibir confirmaciones de los pedidos"

            val channelCuenta = NotificationChannel(
                CUENTA,
                "Cuenta",
                NotificationManager.IMPORTANCE_HIGH
            )
            channelCuenta.description = "Solicitudes y confirmaciones de las cuentas"

            val channelDatos = NotificationChannel(
                DATOS,
                "Datos",
                NotificationManager.IMPORTANCE_HIGH
            )
            channelDatos.description = "Recibir promociones y otros mensajes de la administración del restaurante"

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

//            notificationManager.createNotificationChannel(channelPuerta)
//            notificationManager.createNotificationChannel(channelReserva)
//            notificationManager.createNotificationChannel(channelPuerta)
//            notificationManager.createNotificationChannel(channelCuenta)
//            notificationManager.createNotificationChannel(channelDatos)

            notificationManager.createNotificationChannels(listOf(
                channelPuerta,
                channelPedido,
                channelCuenta,
                channelReserva,
                channelDatos
            ))

        }


    }
}