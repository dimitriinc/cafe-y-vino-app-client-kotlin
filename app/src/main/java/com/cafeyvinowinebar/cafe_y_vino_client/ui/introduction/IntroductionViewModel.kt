package com.cafeyvinowinebar.cafe_y_vino_client.ui.introduction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cafeyvinowinebar.cafe_y_vino_client.data.repositories.UserDataRepository
import com.cafeyvinowinebar.cafe_y_vino_client.di.ApplicationScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IntroductionViewModel @Inject constructor(

    private val userDataRepo: UserDataRepository,
    @ApplicationScope private val applicationScope: CoroutineScope

) : ViewModel() {

    init {
        viewModelScope.launch {
            userDataRepo.errorMessageFlow.collect {
                _uiState.update { state ->
                    state.copy(
                        errorMessage = it
                    )
                }
            }
        }
    }

    private val _uiState = MutableStateFlow(IntroUiState())
    val uiState: StateFlow<IntroUiState> = _uiState.asStateFlow()

    fun authenticateUser(
        email: String,
        password: String,
        name: String,
        phone: String,
        birthdate: String
    ) = applicationScope.launch {
        val authenticated = userDataRepo.authenticateUser(email, password, name, phone, birthdate)
        if (authenticated) {
            _uiState.update {
                it.copy(
                    isRegistered = true
                )
            }
        }
    }

    fun resetPassword(email: String) = viewModelScope.launch {
        userDataRepo.resetPassword(email)
    }

    fun loginUser(email: String, password: String) = viewModelScope.launch {
        val loggedIn = userDataRepo.loginUser(email, password)
        if (loggedIn) {
            _uiState.update {
                it.copy(
                    isLoggedIn = true
                )
            }
        }
    }


}