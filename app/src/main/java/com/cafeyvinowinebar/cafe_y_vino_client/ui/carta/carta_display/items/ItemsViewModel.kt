package com.cafeyvinowinebar.cafe_y_vino_client.ui.carta.carta_display.items

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cafeyvinowinebar.cafe_y_vino_client.KEY_CATEGORIA
import com.cafeyvinowinebar.cafe_y_vino_client.KEY_ICON
import com.cafeyvinowinebar.cafe_y_vino_client.KEY_NOMBRE
import com.cafeyvinowinebar.cafe_y_vino_client.KEY_PRECIO
import com.cafeyvinowinebar.cafe_y_vino_client.data.canasta.ItemCanasta
import com.cafeyvinowinebar.cafe_y_vino_client.data.repositories.MenuDataRepository
import com.cafeyvinowinebar.cafe_y_vino_client.data.repositories.UserDataRepository
import com.google.firebase.firestore.DocumentSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
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
            userDataRepo.getUserPresenceFlow().collect {
                _uiState.update {
                    it.copy(isPresent = it.isPresent)
                }
            }
        }
    }

    /**
     * Transform the received snapshot to a room entity
     * Pass it to repo for storage
     */
    fun addProductToCanasta(document: DocumentSnapshot) = viewModelScope.launch {
        val item = ItemCanasta(
            name = document.getString(KEY_NOMBRE)!!,
            category = document.getString(KEY_CATEGORIA)!!,
            icon = document.getString(KEY_ICON),
            price = document.getLong(KEY_PRECIO)!!
        )
        menuDataRepo.addProductToCanasta(item)
    }


    fun collapseFabs() {
        _uiState.update {
            it.copy(
                areFabsExpanded = false
            )
        }
    }

    fun expandFabs() {
        _uiState.update {
            it.copy(
                areFabsExpanded = true
            )
        }
    }
}