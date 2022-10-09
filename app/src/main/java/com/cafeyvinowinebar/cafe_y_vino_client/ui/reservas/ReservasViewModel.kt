package com.cafeyvinowinebar.cafe_y_vino_client.ui.reservas

import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import com.cafeyvinowinebar.cafe_y_vino_client.DATE_FORMAT
import com.cafeyvinowinebar.cafe_y_vino_client.R
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

/**
 * A view model scoped to the reservas nav graph
 */
@HiltViewModel
class ReservasViewModel @Inject constructor(
    private val userDataRepo: UserDataRepository,
    utilsRepo: UtilsRepository,
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
     * Two functions to set the availability flags of the mesa and fin screens
     */
    fun setPassToMesas(isAllowed: Boolean) {
        _uiState.update { it.copy(passToMesasAllowed = isAllowed) }
    }

    fun setPassToFin(isAllowed: Boolean) {
        _uiState.update { it.copy(passToFinAllowed = isAllowed) }
    }

    /**
     * Receives a popup menu instance to block unavailable items
     * We request a query snapshot from the repository which will contain a list of document snapshots each corresponding to an existing reservation
     * The id of the document snapshot is the table numeric value, so if the id exists, we block this table in the popup's menu
     * If the collection doesn't exist, and the repo returns null, that means there is no reservation in this set, we block no items
     */
    fun blockReservedTables(popup: PopupMenu?) = appScope.launch {
        reservasRepo.getSetOfReservas(_uiState.value.fecha, _uiState.value.part)?.forEach {

            val menu = popup?.menu
            when (it.id) {
                "01" -> menu?.findItem(R.id.m01)?.isEnabled = false
                "02" -> menu?.findItem(R.id.m02)?.isEnabled = false
                "03" -> menu?.findItem(R.id.m03)?.isEnabled = false
                "04" -> menu?.findItem(R.id.m04)?.isEnabled = false
                "05" -> menu?.findItem(R.id.m05)?.isEnabled = false
                "06" -> menu?.findItem(R.id.m06)?.isEnabled = false
                "07" -> menu?.findItem(R.id.m07)?.isEnabled = false
                "08" -> menu?.findItem(R.id.m08)?.isEnabled = false
                "09" -> menu?.findItem(R.id.m09)?.isEnabled = false
                "10" -> menu?.findItem(R.id.m10)?.isEnabled = false
                "11" -> menu?.findItem(R.id.m11)?.isEnabled = false
                "12" -> menu?.findItem(R.id.m12)?.isEnabled = false
            }
            _uiState.update { uiState ->
                uiState.copy(mesasPopup = popup)
            }
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


}