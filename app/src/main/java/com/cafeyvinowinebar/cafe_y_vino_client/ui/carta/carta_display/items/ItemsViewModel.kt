package com.cafeyvinowinebar.cafe_y_vino_client.ui.carta.carta_display.items

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cafeyvinowinebar.cafe_y_vino_client.KEY_CATEGORIA
import com.cafeyvinowinebar.cafe_y_vino_client.KEY_ICON
import com.cafeyvinowinebar.cafe_y_vino_client.KEY_NOMBRE
import com.cafeyvinowinebar.cafe_y_vino_client.KEY_PRECIO
import com.cafeyvinowinebar.cafe_y_vino_client.data.canasta.ItemCanasta
import com.cafeyvinowinebar.cafe_y_vino_client.data.data_models.ItemMenuFirestore
import com.cafeyvinowinebar.cafe_y_vino_client.data.data_models.asItemCanasta
import com.cafeyvinowinebar.cafe_y_vino_client.data.repositories.MenuDataRepository
import com.cafeyvinowinebar.cafe_y_vino_client.data.repositories.UserDataRepository
import com.google.firebase.firestore.DocumentSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ItemsViewModel @Inject constructor(
    private val userDataRepo: UserDataRepository,
    private val menuDataRepo: MenuDataRepository,
) : ViewModel(){

    private val _uiState = MutableStateFlow(ItemsUiState())
    val uiState: StateFlow<ItemsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userDataRepo.getUserPresenceFlow().collect { isUserPresent ->
                _uiState.update {
                    it.copy(isPresent = isUserPresent)
                }
            }
        }
    }

    /**
     * Transform the received snapshot to a room entity
     * Pass it to repo for storage
     */
    fun addProductToCanasta(document: DocumentSnapshot) = viewModelScope.launch(Dispatchers.IO) {
        val item = ItemCanasta(
            name = document.getString(KEY_NOMBRE)!!,
            category = document.getString(KEY_CATEGORIA)!!,
            icon = document.getString(KEY_ICON),
            price = document.getString(KEY_PRECIO)!!.toLong()
        )
        menuDataRepo.addProductToCanasta(item)
    }

    fun addProductToCanasta(item: ItemMenuFirestore) = viewModelScope.launch(Dispatchers.IO) {
        menuDataRepo.addProductToCanasta(item.asItemCanasta())
    }

    fun setItems(items: ArrayList<ItemMenuFirestore>) {
        _uiState.update {
            it.copy(
                items = items
            )
        }
    }
    fun setItemsFabs(expanded: Boolean) {
        _uiState.update {
            it.copy(
                areItemsFabsExpanded = expanded
            )
        }
    }

    fun setSpecsFabs(expanded: Boolean) {
        _uiState.update {
            it.copy(
                areSpecsFabsExpanded = expanded
            )
        }
    }

    fun setInitPosition(initialName: String) {
        var initialPosition = 0
        _uiState.value.items?.forEach {
            if (initialName == it.nombre) {
                initialPosition = _uiState.value.items!!.indexOf(it)
            }
        }
        _uiState.update {
            it.copy(
                initialPosition = initialPosition
            )
        }
    }

}