package com.cafeyvinowinebar.cafe_y_vino_client.ui.carta.when_present

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.cafeyvinowinebar.cafe_y_vino_client.KEY_MESA
import com.cafeyvinowinebar.cafe_y_vino_client.KEY_NOMBRE
import com.cafeyvinowinebar.cafe_y_vino_client.data.PreferencesManager
import com.cafeyvinowinebar.cafe_y_vino_client.data.canasta.CanastaDao
import com.cafeyvinowinebar.cafe_y_vino_client.data.canasta.ItemCanasta
import com.cafeyvinowinebar.cafe_y_vino_client.data.model_classes.ItemPedido
import com.cafeyvinowinebar.cafe_y_vino_client.data.model_classes.PedidoMetaDoc
import com.cafeyvinowinebar.cafe_y_vino_client.data.sources.FirebaseAuthSource
import com.cafeyvinowinebar.cafe_y_vino_client.data.sources.FirebaseFirestoreSource
import com.cafeyvinowinebar.cafe_y_vino_client.data.sources.FirebaseMessagingSource
import com.cafeyvinowinebar.cafe_y_vino_client.di.ApplicationScope
import com.cafeyvinowinebar.cafe_y_vino_client.getCurrentDate
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class CanastaCuentaViewModel @Inject constructor(
    private val fStore: FirebaseFirestoreSource,
    private val fAuth: FirebaseAuthSource,
    private val canastaDao: CanastaDao,
    private val preferencesManager: PreferencesManager,
    private val fMessaging: FirebaseMessagingSource,
    @ApplicationScope private val applicationScope: CoroutineScope
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CanastaCuentaUiState(
            items = canastaDao.getAllItems().asLiveData(),
            userId = fAuth.getUserId()
        )
    )
    val uiState: StateFlow<CanastaCuentaUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            fStore.userPresence.collect { isPresent ->
                _uiState.update {
                    it.copy(isPresent = isPresent ?: false)
                }
            }
        }
    }

    fun addItemToCanasta(item: ItemCanasta) = viewModelScope.launch {
        canastaDao.insert(item)
    }

    fun removeItemFromCanasta(item: ItemCanasta) = viewModelScope.launch {
        canastaDao.deleteItem(item)
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

    fun observeCuentaTotalCost() = viewModelScope.launch {

    }

    fun sendPedido() = applicationScope.launch {

        val userId = fAuth.getUserId()
        val user = fStore.getUserDocById(userId)
        val userMesa = user.getString(KEY_MESA)!!
        val userNombre = user.getString(KEY_NOMBRE)!!
        val currentDate = getCurrentDate()
        val metaDocId = Random.nextLong().toString()
        val canasta = canastaDao.getAllItems().asLiveData().value!!
        val pedido = mutableSetOf<ItemPedido>()
        var servidoBarra = true
        var servidoCocina = true

        canasta.forEach { canastaItem ->
            val itemCount = canastaDao.getItemsByName(canastaItem.name).size
            canastaDao.deleteItemsByName(canastaItem.name)
            pedido.add(
                ItemPedido(
                    name = canastaItem.name,
                    count = itemCount.toLong(),
                    price = canastaItem.price,
                    category = canastaItem.category
                )
            )
        }

        pedido.forEach { pedidoItem ->
            if (pedidoItem.category == "barra") {
                servidoBarra = false
            } else {
                servidoCocina = false
            }
        }

        val metaDoc = PedidoMetaDoc(
            mesa = userMesa,
            servidoBarra = servidoBarra,
            servidoCocina = servidoCocina,
            user = userNombre,
            userId = userId,
            timestamp = Timestamp(Date())
        )

        fStore.setPedido(
            metaDocId,
            pedido,
            metaDoc,
            currentDate
        )

        fMessaging.sendPedidoMessage(
            metaDocId,
            currentDate,
            userMesa,
            userNombre
        )



    }

    fun updateCanSendPedido(canSendPedidos: Boolean) = viewModelScope.launch {
        preferencesManager.updateCanSendPedidos(canSendPedidos)
        _uiState.update {
            it.copy(
                canSendPedidos = canSendPedidos
            )
        }
    }

    fun sendCuentaRequest(payMode: String) = applicationScope.launch {
        if (_uiState.value.totalCuentaCost > 0) {
            val userId = fAuth.getUserId()
            val user = fStore.getUserDocById(userId)

            fMessaging.sendCuentaMessage(
                payMode,
                user.getString(KEY_NOMBRE)!!,
                user.getString(KEY_MESA)!!
            )
        } else {
            _uiState.update {
                it.copy(
                    isCuentaEmpty = true
                )
            }
        }
    }


}