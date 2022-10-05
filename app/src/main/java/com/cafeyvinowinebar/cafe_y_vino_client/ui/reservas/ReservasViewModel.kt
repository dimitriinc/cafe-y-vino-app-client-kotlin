package com.cafeyvinowinebar.cafe_y_vino_client.ui.reservas

import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cafeyvinowinebar.cafe_y_vino_client.data.sources.FirebaseFirestoreSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import com.cafeyvinowinebar.cafe_y_vino_client.DATE_FORMAT
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.cafeyvinowinebar.cafe_y_vino_client.data.sources.FirebaseAuthSource
import com.cafeyvinowinebar.cafe_y_vino_client.data.sources.FirebaseMessagingSource
import com.cafeyvinowinebar.cafe_y_vino_client.data.data_models.Reserva
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.*


@HiltViewModel
class ReservasViewModel @Inject constructor(
    private val fStore: FirebaseFirestoreSource,
    private val fAuth: FirebaseAuthSource,
    private val fMessaging: FirebaseMessagingSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReservasUiState())
    val uiState: StateFlow<ReservasUiState> = _uiState.asStateFlow()


    fun getPartHoras() = viewModelScope.launch {
        val horas = fStore.getPartHoras()
        _uiState.update {
            it.copy(horas = horas)
        }
    }

    fun setPartOfDay(part: String) {
        _uiState.update {
            it.copy(part = part)
        }
    }

    fun setDate(calendar: Calendar) {
        val sdf = SimpleDateFormat(DATE_FORMAT, Locale("ES"))
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

    fun setPassToMesas(isAllowed: Boolean) {
        _uiState.update { it.copy(passToMesasAllowed = isAllowed) }
    }

    fun setPassToFin(isAllowed: Boolean) {
        _uiState.update { it.copy(passToFinAllowed = isAllowed) }
    }

    fun blockReservedTables(popup: PopupMenu?) = viewModelScope.launch {
        fStore.getSetOfReservas(_uiState.value.fecha, _uiState.value.part)?.forEach {

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

    fun populateClockPopup(clockPopup: PopupMenu?) = viewModelScope.launch {
        val availableHours = fStore.getSetOfAvailableReservaHours(_uiState.value.part)
        availableHours.forEach { hourString ->
            clockPopup?.menu?.add(hourString)
        }
        _uiState.update {
            it.copy(clockPopup = clockPopup)
        }
    }

    fun makeReservation(comentario: String) = viewModelScope.launch {

        val userUid = fAuth.getUserId()
        val userDocSnapshot = fStore.getUserDocById(userUid)
        val userNombre = userDocSnapshot.getString("nombre")
        val userTelefono = userDocSnapshot.getString("telefono")

        val reserva = Reserva(
            nombre = userNombre,
            telefono = userTelefono,
            fecha = _uiState.value.fecha,
            mesa = _uiState.value.mesa,
            hora = _uiState.value.hora,
            pax = _uiState.value.pax,
            parte = _uiState.value.part,
            userId = userUid,
            comentario = comentario,
            timestamp = Timestamp(Date())
        )

        val isReservaSent = fStore.setReservaDoc(reserva)

        fMessaging.sendReservaMessage(reserva)

        _uiState.update {
            it.copy(
                isReservaSent = isReservaSent
            )
        }

    }



}