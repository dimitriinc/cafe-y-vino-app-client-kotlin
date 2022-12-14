package com.cafeyvinowinebar.cafe_y_vino_client.ui.introduction

import android.content.Context
import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.cafeyvinowinebar.cafe_y_vino_client.data.repositories.UserDataRepository
import com.cafeyvinowinebar.cafe_y_vino_client.di.ApplicationScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * A view model scoped to the intro nav graph
 */
@HiltViewModel
class IntroductionViewModel @Inject constructor(

    private val userDataRepo: UserDataRepository,
    @ApplicationScope private val applicationScope: CoroutineScope

) : ViewModel() {

    /**
     * Starts to listen to error messages that may come if some of the operations fail co complete
     */
    init {
        viewModelScope.launch {
            userDataRepo.errorMessageFlow.collect {
                _uiState.update { state ->
                    state.copy(
                        errorMessageId = it
                    )
                }
            }
        }
    }

    private val _uiState = MutableStateFlow(IntroUiState())
    val uiState: StateFlow<IntroUiState> = _uiState.asStateFlow()

    /**
     * Receives the data from the edit texts
     * Calls the registration operations from the repository and suspends until their completion
     * If the registration process returns 'true', lets the fragment know by updating the UI state
     */
    fun registerUser() = applicationScope.launch {
        _uiState.update {
            it.copy(
                progressBarVisible = true
            )
        }
        val uiState = _uiState.value
        val registered = userDataRepo.registerUser(
            uiState.email,
            uiState.password,
            uiState.name,
            uiState.phone,
            uiState.birthdate
        )
        if (registered) {
            _uiState.update {
                it.copy(
                    isRegistered = true,
                    progressBarVisible = false
                )
            }
        }
    }

    /**
     * Take the values from the form and store them in the UI state
     */
    fun storeFormData(
        email: String,
        password: String,
        name: String,
        phone: String,
        birthdate: String
    ) {
        _uiState.update {
            it.copy(
                email = email,
                password = password,
                name = name,
                phone = phone,
                birthdate = birthdate
            )
        }
    }

    /**
     * Start the email sending operation, wait for the result
     * If successful, let the fragment know by updating the UI state with a message
     */
    fun resetPassword(email: String) = viewModelScope.launch {
        val formSent = userDataRepo.resetPassword(email)
        if (formSent) {
            _uiState.update {
                it.copy(
                    messageId = R.string.sent_email_toast
                )
            }
        }
    }

    /**
     * Starts the logging in inside the application scope, and waits for the result
     * If successful, lets the fragment know that the job is done by updating the UI state
     */
    fun loginUser(email: String, password: String) = applicationScope.launch {
        val loggedIn = userDataRepo.loginUser(email, password)
        if (loggedIn) {
            _uiState.update {
                it.copy(
                    isLoggedIn = true
                )
            }
        }
    }

    /**
     * When returning from the privacy policy fragment we want to recreate the dialog
     */
    fun setPrivacyFlag(flag: Boolean) {
        _uiState.update {
            it.copy(
                privacyPolicyVisited = flag
            )
        }
    }

    fun nullifyErrorMessage() {
        _uiState.update {
            it.copy(
                errorMessageId = null,
                progressBarVisible = false
            )
        }
    }

    fun setProgressBarVisibility(isVisible: Boolean) {
        _uiState.update {
            it.copy(
                progressBarVisible = isVisible
            )
        }
    }

    fun nullifyMessage() {
        _uiState.update {
            it.copy(
                messageId = null,
                progressBarVisible = false
            )
        }
    }

    fun updateToken() = applicationScope.launch {
        userDataRepo.updateToken()
    }
}