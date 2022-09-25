package com.cafeyvinowinebar.cafe_y_vino_client.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cafeyvinowinebar.cafe_y_vino_client.GMT
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
    private val fMessaging: FirebaseMessagingSource
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
            userDataRepo.userPresenceFlow.collect { isPresent ->
                _uiState.update {
                    it.copy(
                        isUserPresent = isPresent
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

    fun updateEmail(email: String) {
        TODO("Not yet implemented")
    }


}