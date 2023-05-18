package com.cafeyvinowinebar.cafe_y_vino_client.ui.reservas

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import com.cafeyvinowinebar.cafe_y_vino_client.DATE_FORMAT
import com.cafeyvinowinebar.cafe_y_vino_client.data.sources.FirebaseMessagingSource
import com.cafeyvinowinebar.cafe_y_vino_client.data.data_models.ReservaFirestore
import com.cafeyvinowinebar.cafe_y_vino_client.data.repositories.ReservasDataRepository
import com.cafeyvinowinebar.cafe_y_vino_client.data.repositories.UserDataRepository
import com.cafeyvinowinebar.cafe_y_vino_client.data.repositories.UtilsRepository
import com.cafeyvinowinebar.cafe_y_vino_client.di.ApplicationScope
import com.google.firebase.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMultipart

/**
 * A view model scoped to the reservas nav graph
 */
@HiltViewModel
class ReservasViewModel @Inject constructor(
    private val userDataRepo: UserDataRepository,
    private val utilsRepo: UtilsRepository,
    private val fMessaging: FirebaseMessagingSource,
    private val reservasRepo: ReservasDataRepository,
    @ApplicationScope
    private val appScope: CoroutineScope
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReservasUiState())
    val uiState: StateFlow<ReservasUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val utils = utilsRepo.getUtilsForReservas()
            val firstName = userDataRepo.getUserFirstName()
            _uiState.update {
                it.copy(
                    horas = utils.setsOfHours,
                    horasDeDia = utils.availableHoursDia,
                    horasDeNoche = utils.availableHoursNoche,
                    firstName = firstName
                )
            }
        }

    }

    /**
     * Six methods to set the values, that will be used to create a Reserva instance, to the UI state
     */
    fun setPartOfDay(part: String) {
        _uiState.update {
            it.copy(part = part)
        }
    }

    fun setDate(calendar: Calendar) {
        val sdf = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        val date = sdf.format(calendar.time)
        _uiState.update {
            it.copy(fecha = date)
        }
    }

    fun setMesa(mesa: String) {
        _uiState.update {
            it.copy(mesa = mesa)
        }
    }

    fun setHora(hora: String) {
        _uiState.update {
            it.copy(hora = hora)
        }
    }

    fun setPax(pax: String) {
        _uiState.update {
            it.copy(pax = pax)
        }
    }

    fun setComentario(comentario: String) {
        _uiState.update {
            it.copy(comentario = comentario)
        }
    }

    /**
     * We request the repository to query the existing reservations for the chosen period of time and return a list of tables
     * And we store the returned value in the UI state, so that the view layer can disable the menu items based on the list
     */
    fun getReservedTables() = appScope.launch(Dispatchers.IO) {
        _uiState.update {
            it.copy(
                listOfReservedTables = emptyList()
            )
        }
        val listOfTables = reservasRepo.getSetOfReservas(_uiState.value.fecha, _uiState.value.part)
        _uiState.update { uiState ->
            uiState.copy(listOfReservedTables = listOfTables)
        }
    }

    /**
     * Starts the process of storing a reserva
     */
    fun makeReservation() = appScope.launch {

        // gather all the necessary data
        val userUid = userDataRepo.getUserId()
        val user = userDataRepo.getUser()
        val uiState = _uiState.value

        // create an instance of reserva
        val reserva = ReservaFirestore(
            nombre = user.nombre,
            telefono = user.telefono,
            fecha = uiState.fecha,
            mesa = uiState.mesa,
            hora = uiState.hora,
            pax = uiState.pax,
            parte = uiState.part,
            userId = userUid,
            comentario = uiState.comentario,
            timestamp = Timestamp(Date())
        )

        sendEmail(reserva)

        // send it for storing into the Firestore DB, suspend until the result arrives
        val reservaSent = reservasRepo.setReservaDoc(reserva)

        // if true, send a message to admins
        if (reservaSent) {
            fMessaging.sendReservaMessage(reserva)
        }

        _uiState.update {
            it.copy(
                isReservaSent = reservaSent
            )
        }

    }

    fun nullifyReservaSent() {
        _uiState.update {
            it.copy(
                isReservaSent = null
            )
        }
    }

    /**
     * Every time fecha or part of day gets changed we want to set mesa, hora, and pax to null, because we deal with a new set of reservation
     * The chosen mesa might be reserved already, and the hora and pax will not correspond either
     */
    fun nullifyData() {
        _uiState.update {
            it.copy(
                mesa = null,
                hora = null,
                pax = null
            )
        }
    }

    private suspend fun sendEmail(reserva: ReservaFirestore) {
        val utils = utilsRepo.getEmailUtils()
        val username = "cafeyvinobot@gmail.com"
        val password = utils.password
        val recipient = utils.recipient

        val props = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", "smtp.gmail.com")
            put("mail.smtp.port", "587")
        }

        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(username, password)
            }
        })

        try {
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(username))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient))
                subject = "Nueva solicitud de reserva desde la app"

                val multipart = MimeMultipart()

                val htmlBodyPart = MimeBodyPart().apply {
                    setContent("<html><body>" +
                            "                <div style='padding:2rem;background-color:#fcfaeb;color:#160b17;border:1px solid;border-radius:50px;margin-left:auto;margin-right:auto;margin-bottom:1rem;width:fit-content;'>" +
                            "                    <p>Nombre:  <em>${reserva.nombre}</em></p>" +
                            "                    <p>Fecha:  <em>${reserva.fecha}</em></p>" +
                            "                    <p>Hora:  <em>${reserva.hora}</em></p>" +
                            "                    <p>Pax:  <em>${reserva.pax}</em></p>" +
                            "                    <p>Comentario:  <em>${reserva.comentario}</em></p>" +
                            "                    <p>Teléfono:  <em>${reserva.telefono}</em></p>" +
                            "                </div>" +
                            "</body></html>",
                        "text/html")
                }

                multipart.addBodyPart(htmlBodyPart)
                setContent(multipart)
            }

            Transport.send(message)
        } catch (_: MessagingException) {}
    }


}