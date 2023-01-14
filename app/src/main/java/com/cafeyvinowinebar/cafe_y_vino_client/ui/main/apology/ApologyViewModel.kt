package com.cafeyvinowinebar.cafe_y_vino_client.ui.main.apology

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cafeyvinowinebar.cafe_y_vino_client.*
import com.cafeyvinowinebar.cafe_y_vino_client.data.repositories.UserDataRepository
import com.cafeyvinowinebar.cafe_y_vino_client.data.sources.FirebaseMessagingSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApologyViewModel @Inject constructor(
    private val userDataRepo: UserDataRepository,
    private val fMessaging: FirebaseMessagingSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(ApologyUiState())
    val uiState: StateFlow<ApologyUiState> = _uiState.asStateFlow()

    fun registerUser(nombre: String, telefono: String) = viewModelScope.launch(Dispatchers.IO) {
        val email = userDataRepo.getUserObject()!!.email
        val token = fMessaging.getToken()
        val userObject = mapOf(
            KEY_NOMBRE to nombre,
            KEY_TELEFONO to telefono,
            KEY_FECHA_NACIMIENTO to "birthdate",
            KEY_EMAIL to email!!,
            KEY_IS_PRESENT to false,
            KEY_MESA to "00",
            KEY_TOKEN to token,
            KEY_BONOS to 0
        )
        val userDocStored = userDataRepo.storeUserDoc(userObject)
        _uiState.update {
            it.copy(
                isRegistered = userDocStored
            )
        }
    }


}