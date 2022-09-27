package com.cafeyvinowinebar.cafe_y_vino_client.ui.main

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cafeyvinowinebar.cafe_y_vino_client.GMT
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.cafeyvinowinebar.cafe_y_vino_client.data.model_classes.Gift
import com.cafeyvinowinebar.cafe_y_vino_client.data.repositories.ProductsDataRepository
import com.cafeyvinowinebar.cafe_y_vino_client.data.repositories.UserDataRepository
import com.cafeyvinowinebar.cafe_y_vino_client.data.repositories.UtilsRepository
import com.cafeyvinowinebar.cafe_y_vino_client.data.sources.FirebaseMessagingSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val utilsRepo: UtilsRepository,
    private val userDataRepo: UserDataRepository,
    private val fMessaging: FirebaseMessagingSource,
    private val productsRepo: ProductsDataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {

        if (userDataRepo.getUserObject() != null) {
            _uiState.update {
                it.copy(
                    isLoggedIn = true
                )
            }
        }

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    userName = userDataRepo.getUser()?.nombre!!
                )
            }
            userDataRepo.userPresenceFlow.collect { isPresent ->
                _uiState.update {
                    it.copy(
                        isUserPresent = isPresent
                    )
                }
            }
            userDataRepo.userBonosFlow.collect { bonos ->
                _uiState.update {
                    it.copy(
                        bonos = bonos
                    )
                }
            }
        }
    }

    fun nullifyEntryRequest() {
        _uiState.update {
            it.copy(
                canUserSendEntryRequest = null
            )
        }
    }

    fun setEntryRequestStatus() = viewModelScope.launch {
        val utils = utilsRepo.getUtilsForEntryRequest()
        val day = LocalDate.now().dayOfWeek.name
        utils.offDays.forEach { offDay ->
            if (offDay == day) {
                _uiState.update { uiState ->
                    uiState.copy(canUserSendEntryRequest = false)
                }
            } else {
                val calendar = Calendar.getInstance()
                calendar.timeZone = TimeZone.getTimeZone(GMT)
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                utils.attendanceHours.forEach { attendanceHour ->
                    if (hour == attendanceHour.toInt()) {
                        _uiState.update {
                            it.copy(
                                canUserSendEntryRequest = true
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                canUserSendEntryRequest = false
                            )
                        }
                    }
                }
            }
        }
    }

    fun sendEntryRequest() = viewModelScope.launch {
        val user = userDataRepo.getUser()!!
        val userId = userDataRepo.getUserId()
        fMessaging.sendEntryRequest(
            userId,
            user.token,
            user.nombre
        )
    }

    fun logout() {
        userDataRepo.logout()
    }

    fun updateEmail(email: String)  = viewModelScope.launch {
        val emailUpdated = userDataRepo.updateEmail(email)
        if (emailUpdated) {
            _uiState.update {
                it.copy(
                    message = Resources.getSystem().getString(R.string.email_update_success)
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    message = Resources.getSystem().getString(R.string.email_update_failure)
                )
            }
        }
    }

    fun updateNombre(nombre: String) = viewModelScope.launch {
        val nombreUpdated = userDataRepo.updateNombre(nombre)
        if (nombreUpdated) {
            _uiState.update {
                it.copy(
                    message = Resources.getSystem().getString(R.string.nombre_update_success)
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    message = Resources.getSystem().getString(R.string.nombre_update_failure)
                )
            }
        }
    }

    fun updateTelefono(telefono: String) = viewModelScope.launch {
        val telefonoUpdated = userDataRepo.updateTelefono(telefono)
        if (telefonoUpdated) {
            _uiState.update {
                it.copy(
                    message = Resources.getSystem().getString(R.string.telefono_update_success)
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    message = Resources.getSystem().getString(R.string.telefono_update_failure)
                )
            }
        }
    }

    fun nullifyMessage() {
        _uiState.update {
            it.copy(
                message = null
            )
        }
    }

    fun sendGiftRequest(gift: Gift) = viewModelScope.launch {
        val userMesa = userDataRepo.getUser()?.mesa!!
        val userId = userDataRepo.getUserId()
        productsRepo.storeGift(
            gift.nombre,
            gift.precio.toLong(),
            userMesa,
            userId,
            _uiState.value.userName
        )
        fMessaging.sendGiftMessage(
            gift.nombre,
            userMesa,
            _uiState.value.userName
        )
        userDataRepo.updateBonos(_uiState.value.bonos - gift.precio.toLong())
    }

}