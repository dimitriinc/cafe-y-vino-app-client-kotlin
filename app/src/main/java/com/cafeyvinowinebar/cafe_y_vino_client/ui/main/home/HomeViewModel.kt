package com.cafeyvinowinebar.cafe_y_vino_client.ui.main.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cafeyvinowinebar.cafe_y_vino_client.GMT
import com.cafeyvinowinebar.cafe_y_vino_client.data.repositories.UserDataRepository
import com.cafeyvinowinebar.cafe_y_vino_client.data.repositories.UtilsRepository
import com.cafeyvinowinebar.cafe_y_vino_client.data.sources.FirebaseMessagingSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val utilsRepo: UtilsRepository,
    private val userDataRepo: UserDataRepository,
    private val fMessaging: FirebaseMessagingSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        /**
         * first of all we set the logged in value to true if the user is logged in
         * otherwise the user will be sent to the intro nav graph
         */
        if (userDataRepo.getUserObject() == null) {
            _uiState.update {
                it.copy(
                    isLoggedIn = false
                )
            }
        }

        // track the user's presence status
        viewModelScope.launch(Dispatchers.IO) {
            userDataRepo.getUserPresenceFlow().collect { isUserPresent ->
                _uiState.update {
                    it.copy(
                        isUserPresent = isUserPresent
                    )
                }
            }
        }
    }

    /**
     * We get related to entry requests values from the utils Room table
     * and check the current time against them to set the entry status
     */
    fun setEntryRequestStatus() = viewModelScope.launch(Dispatchers.IO) {
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
}