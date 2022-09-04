package com.cafeyvinowinebar.cafe_y_vino_client.ui.carta.item_specs

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cafeyvinowinebar.cafe_y_vino_client.KEY_ITEMS
import com.cafeyvinowinebar.cafe_y_vino_client.data.canasta.CanastaDao
import com.cafeyvinowinebar.cafe_y_vino_client.data.canasta.ItemCanasta
import com.cafeyvinowinebar.cafe_y_vino_client.data.model_classes.ItemMenu
import com.cafeyvinowinebar.cafe_y_vino_client.data.sources.FirebaseFirestoreSource
import com.cafeyvinowinebar.cafe_y_vino_client.data.sources.FirebaseStorageSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ItemSpecsViewModel @Inject constructor(
    private val fStore: FirebaseFirestoreSource,
    private val fStorage: FirebaseStorageSource,
    private val canastaDao: CanastaDao

) : ViewModel() {

    private val _uiState = MutableStateFlow(SpecsUiState())
    val uiState: StateFlow<SpecsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            fStore.userPresence.collect { isPresent ->
                _uiState.update {
                    it.copy(
                        isPresent = isPresent ?: false
                    )
                }
            }
        }
    }

    fun setArgsOnUiState(bundle: Bundle, initialName: String) = viewModelScope.launch {
        val items = bundle.getSerializable(KEY_ITEMS) as ArrayList<ItemMenu>
        var initialPosition = 0
        items.forEach {
            if (initialName == it.nombre) {
                initialPosition = items.indexOf(it)
            }
        }
        _uiState.update {
            it.copy(
                items = items,
                initialPosition = initialPosition
            )
        }
    }

    fun setThePagingFragment(
        currentPosition: Int
    ) = viewModelScope.launch {
        _uiState.update {
            it.copy(
                currentPosition = currentPosition,
                currentItem = it.items?.get(currentPosition),
                setSize = it.items?.size,
                itemImgReference = it.items?.get(currentPosition)?.image?.let { imgPath ->
                    fStorage.getImgReference(
                        imgPath
                    )
                }
            )
        }
    }

    fun addItemToCanastaDb(itemMenu: ItemMenu) = viewModelScope.launch {
        val item = ItemCanasta(
            name = itemMenu.nombre,
            category = itemMenu.categoria,
            price = itemMenu.precio.toLong(),
            icon = itemMenu.icon
        )
        canastaDao.insert(item)
    }

    fun collapseFabs() {
        _uiState.update {
            it.copy(
                areFabsExpended = false
            )
        }
    }

    fun expandFabs() {
        _uiState.update {
            it.copy(
                areFabsExpended = true
            )
        }
    }
}