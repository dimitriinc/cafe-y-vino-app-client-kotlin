package com.cafeyvinowinebar.cafe_y_vino_client.ui.main

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cafeyvinowinebar.cafe_y_vino_client.GMT
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.cafeyvinowinebar.cafe_y_vino_client.data.repositories.MenuDataRepository
import com.cafeyvinowinebar.cafe_y_vino_client.data.repositories.UserDataRepository
import com.cafeyvinowinebar.cafe_y_vino_client.data.repositories.UtilsRepository
import com.cafeyvinowinebar.cafe_y_vino_client.data.sources.FirebaseMessagingSource
import com.cafeyvinowinebar.cafe_y_vino_client.ui.data_models.Gift
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.*
import javax.inject.Inject

/**
 * A view model scoped to the main nav graph
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val utilsRepo: UtilsRepository,
    private val userDataRepo: UserDataRepository,
    private val fMessaging: FirebaseMessagingSource,
    private val productsRepo: MenuDataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        // first of all we set the logged in value to true if the user is logged in
        // otherwise the user will be sent to the intro nav graph
        if (userDataRepo.getUserObject() == null) {
            _uiState.update {
                it.copy(
                    isLoggedIn = false
                )
            }
        }


        // get the user data stored in the UI state
        viewModelScope.launch(Dispatchers.IO) {

            val firstName = userDataRepo.getUserFirstName()

            userDataRepo.userFlow.collect { user ->
                _uiState.update { uiState ->
                    uiState.copy(
                        isUserPresent = user.isPresent,
                        userEmail = user.email,
                        userTelefono = user.telefono,
                        userFirstName = firstName,
                        bonos = user.bonos
                    )

                }

            }
        }
    }

    /**
     * After each time we check if the user can send an entry request, we set the value to null
     * So that the fragment reacts appropriately to when the value is not null
     */
    fun nullifyEntryRequestStatus() {
        _uiState.update {
            it.copy(
                canUserSendEntryRequest = null
            )
        }
    }

    /**
     * We get related to entry requests values from the utils Room table
     * and check the current time against them to set the entry status
     */
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

    /**
     * Gathers necessary data for an entry request message
     * Calls the appropriate messaging source function
     */
    fun sendEntryRequest() = viewModelScope.launch {
        val user = userDataRepo.getUser()
        val userId = userDataRepo.getUserId()
        fMessaging.sendEntryRequest(
            userId,
            user.nombre
        )
    }

    /**
     * Closes the current session from the user
     */
    fun logout() {
        userDataRepo.logout()
    }

    /**
     * Next three functions update different user properties
     * Nombre and telefono in the Firestore DB, and email in the Authentication system
     * A call to the data layer returns a boolean that represents a success or a failure of the update
     * According to the result we update the message property of the UI state
     */
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

    /**
     * After the message is displayed to the user, we set the value of the property back to null
     * So that the fragment can keep reacting to when the value is not null
     */
    fun nullifyMessage() {
        _uiState.update {
            it.copy(
                message = null
            )
        }
    }

    /**
     * Gathers necessary data to send a message to admin to notify them about a new gift to serve, and to store the gift in the Firestore DB
     * Calls the products repo's function to store the gift
     * Calls the appropriate messaging source function
     * Subtracts the gift's price from the total user bonos, and passes the value to the repo's fun to update the user's bonos
     */
    fun sendGiftRequest(gift: Gift) = viewModelScope.launch {
        val userMesa = userDataRepo.getUser().mesa
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