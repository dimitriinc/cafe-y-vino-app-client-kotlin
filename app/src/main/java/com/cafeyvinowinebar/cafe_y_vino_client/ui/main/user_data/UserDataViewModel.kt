package com.cafeyvinowinebar.cafe_y_vino_client.ui.main.user_data

import android.content.Context
import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.cafeyvinowinebar.cafe_y_vino_client.data.repositories.UserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserDataViewModel @Inject constructor(
    private val userDataRepo: UserDataRepository,
    @ApplicationContext val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserDataUiState())
    val uiState: StateFlow<UserDataUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            userDataRepo.getUserFlow().collect {
                _uiState.update { uiState ->
                    uiState.copy(
                        userName = it.nombre,
                        userTelefono = it.telefono,
                        userEmail = it.email
                    )
                }
            }
        }
    }


    fun logout() {
        userDataRepo.logout()
    }

    /**
     * Next three functions update different user properties
     * Nombre and telefono in the Firestore DB, and email in the Authentication system
     * A call to the data layer returns a boolean that represents a success or a failure of the update
     * According to the result we update the message property of the UI state
     */
    fun updateEmail(email: String) = viewModelScope.launch {
        val emailUpdated = userDataRepo.updateEmail(email)
        if (emailUpdated) {
            _uiState.update {
                it.copy(
                    message = context.getString(R.string.email_update_success)
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    message = context.getString(R.string.email_update_failure)
                )
            }
        }
    }

    fun updateNombre(nombre: String) = viewModelScope.launch {
        val nombreUpdated = userDataRepo.updateNombre(nombre)
        if (nombreUpdated) {
            _uiState.update {
                it.copy(
                    message = context.getString(R.string.nombre_update_success)
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    message = context.getString(R.string.nombre_update_failure)
                )
            }
        }
    }

    fun updateTelefono(telefono: String) = viewModelScope.launch {
        val telefonoUpdated = userDataRepo.updateTelefono(telefono)
        if (telefonoUpdated) {
            _uiState.update {
                it.copy(
                    message = context.getString(R.string.telefono_update_success)
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    message = context.getString(R.string.telefono_update_failure)
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
}