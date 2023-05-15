package com.cafeyvinowinebar.cafe_y_vino_client

import android.app.UiModeManager
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

const val KEY_NOMBRE = "nombre"
const val KEY_IS_PRESENT = "isPresent"
const val KEY_MESA = "mesa"
const val KEY_TOKEN = "token"
const val KEY_ACTION = "action"
const val KEY_FECHA = "fecha"
const val KEY_HORA = "hora"
const val KEY_PAX = "pax"
const val KEY_COMENTARIO = "comentario"
const val KEY_BONO = "bono"
const val KEY_BONOS = "bonos"
const val KEY_ADMIN_TOKEN = "adminToken"
const val KEY_PARTE = "parte"
const val KEY_TYPE = "type"
const val KEY_CATEGORIA = "categoria"
const val KEY_PRECIO = "precio"
const val KEY_ICON = "icon"
const val KEY_META_ID = "metaDocId"
const val KEY_MODO = "modo"
const val KEY_USER_ID = "userId"
const val KEY_REGALO = "regalo"
const val KEY_EMAIL = "email"
const val KEY_TELEFONO = "telefono"
const val KEY_FECHA_NACIMIENTO = "fechaDeNacimiento"

const val TO_ADMIN_NEW = "toAdminNew"
const val DATE_FORMAT = "dd-MM-yyyy"
const val GMT = "GMT-5"

const val ACTION_CUENTA_ADMIN = "cuenta_admin"
const val ACTION_REGALO_ADMIN = "regalo_admin"
const val ACTION_PUERTA_ADMIN = "puerta_admin"
const val ACTION_RESERVA_ACK = "reserva_ack"
const val ACTION_RESERVA_NACK = "reserva_nack"
const val ACTION_PEDIDO_ADMIN = "pedido_admin"
const val ACTION_MSG = "msg"
const val ACTION_CUENTA_ACK = "cuenta_ack"
const val ACTION_RESERVA = "reserva"
const val ACTION_PEDIDO = "pedido"
const val ACTION_CUENTA = "cuenta"
const val ACTION_PUERTA = "puerta"
const val ACTION_REGALO = "regalo"

const val SENDER_ID = "1096226926741"


fun getFirebaseMessageId() = "m-${Random.nextLong()}"

fun getCurrentDate(): String {
    val sdf = SimpleDateFormat(DATE_FORMAT, Locale("ES"))
    sdf.timeZone = TimeZone.getTimeZone(GMT)
    return sdf.format(Date())
}

fun isOnline(context: Context): Boolean {
    val connectivityManager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    if (capabilities != null) {
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            return true
        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            return true
        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
            return true
        }
    }
    return false
}


