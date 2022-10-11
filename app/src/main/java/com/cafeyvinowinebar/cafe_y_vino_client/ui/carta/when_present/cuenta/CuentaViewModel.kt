package com.cafeyvinowinebar.cafe_y_vino_client.ui.carta.when_present.cuenta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cafeyvinowinebar.cafe_y_vino_client.data.PreferencesManager
import com.cafeyvinowinebar.cafe_y_vino_client.data.repositories.MenuDataRepository
import com.cafeyvinowinebar.cafe_y_vino_client.data.repositories.UserDataRepository
import com.cafeyvinowinebar.cafe_y_vino_client.data.sources.FirebaseMessagingSource
import com.cafeyvinowinebar.cafe_y_vino_client.di.ApplicationScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CuentaViewModel @Inject constructor(
    private val userDataRepo: UserDataRepository,
    private val menuDataRepo: MenuDataRepository,
    private val preferencesManager: PreferencesManager,
    private val fMessaging: FirebaseMessagingSource,
    @ApplicationScope private val applicationScope: CoroutineScope
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CuentaUiState(
            userId = userDataRepo.getUserId()
        )
    )
    val uiState: StateFlow<CuentaUiState> = _uiState.asStateFlow()

    init {
        // listen to the user's presence state
        viewModelScope.launch(Dispatchers.IO) {
            userDataRepo.userPresenceFlow.collect { isPresent ->
                _uiState.update {
                    it.copy(
                        isPresent = isPresent
                    )
                }
            }
            // observe the bill's total price
            menuDataRepo.getCuentaTotalFlow(userDataRepo.getUserId()).collect { total ->
                _uiState.update { uiState ->
                    uiState.copy(
                        totalCuentaCost = total ?: 0.0
                    )
                }
            }
        }

    }

    /**
     * Functions to expand/collapse different fabs
     */
    fun collapsePedirCuentaFab() {
        _uiState.update {
            it.copy(
                isPedirCuentaFabExpanded = false
            )
        }
    }

    fun expandPedirCuentaFab() {
        _uiState.update {
            it.copy(
                isPedirCuentaFabExpanded = true
            )
        }
    }

    fun collapsePayModeFabs() {
        _uiState.update {
            it.copy(
                arePayModeFabsExpanded = false
            )
        }
    }

    fun expandPayModeFabs() {
        _uiState.update {
            it.copy(
                arePayModeFabsExpanded = true
            )
        }
    }

    /**
     * Receives a pay mode from the fragment, and sends it along with some user data to the messaging service to notify the administrators
     * Prohibits the user from sending other requests and new pedidos by updating the preference in the DataStore
     * Collapses all the fabs
     */
    fun sendCuentaRequest(payMode: String) = applicationScope.launch {
        val user = userDataRepo.getUser()
        fMessaging.sendCuentaRequest(
            payMode,
            user.nombre,
            user.mesa
        )
        preferencesManager.updateCanSendPedidos(false)
        collapsePayModeFabs()
        collapsePedirCuentaFab()
    }


}