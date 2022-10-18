package com.cafeyvinowinebar.cafe_y_vino_client.ui.carta.when_present.canasta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cafeyvinowinebar.cafe_y_vino_client.KEY_MESA
import com.cafeyvinowinebar.cafe_y_vino_client.KEY_NOMBRE
import com.cafeyvinowinebar.cafe_y_vino_client.data.PreferencesManager
import com.cafeyvinowinebar.cafe_y_vino_client.data.canasta.ItemCanasta
import com.cafeyvinowinebar.cafe_y_vino_client.data.data_models.ItemPedido
import com.cafeyvinowinebar.cafe_y_vino_client.data.data_models.PedidoMetaDoc
import com.cafeyvinowinebar.cafe_y_vino_client.data.repositories.MenuDataRepository
import com.cafeyvinowinebar.cafe_y_vino_client.data.repositories.UserDataRepository
import com.cafeyvinowinebar.cafe_y_vino_client.data.sources.FirebaseMessagingSource
import com.cafeyvinowinebar.cafe_y_vino_client.di.ApplicationScope
import com.cafeyvinowinebar.cafe_y_vino_client.getCurrentDate
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class CanastaViewModel @Inject constructor(
    private val menuDataRepo: MenuDataRepository,
    val userDataRepo: UserDataRepository,
    @ApplicationScope val applicationScope: CoroutineScope,
    private val preferencesManager: PreferencesManager
) : ViewModel(){
    private val _uiState = MutableStateFlow(
        CanastaUiState(
            items = menuDataRepo.getCanastaItems()
        )
    )
    val uiState: StateFlow<CanastaUiState> = _uiState.asStateFlow()

    init {
        // listen to the user's presence state, and if they can make orders
        viewModelScope.launch(Dispatchers.IO) {
            userDataRepo.userPresenceFlow.collect { isPresent ->
                _uiState.update {
                    it.copy(
                        isPresent = isPresent
                    )
                }
            }
            preferencesManager.preferencesFlow.collect { preferences ->
                _uiState.update {
                    it.copy(
                        canSendPedidos = preferences.canSendPedidos
                    )
                }
            }

        }
    }

    fun addItemToCanasta(item: ItemCanasta) = viewModelScope.launch {
        menuDataRepo.addProductToCanasta(item)
    }

    fun removeItemFromCanasta(item: ItemCanasta) = viewModelScope.launch {
        menuDataRepo.removeProductFromCanasta(item)
    }

    fun collapseCanastaFabs() {
        _uiState.update {
            it.copy(
                areCanastaFabsExpanded = false
            )
        }
    }
    fun expandCanastaFabs() {
        _uiState.update {
            it.copy(
                areCanastaFabsExpanded = true
            )
        }
    }

    /**
     * Tells the menu repository to start the logic of creating a new pedido in the system
     */
    fun sendPedido() = applicationScope.launch {

        val userId = userDataRepo.getUserId()
        val user = userDataRepo.getUser()

        menuDataRepo.sendPedido(user, userId)

    }

}