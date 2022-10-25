package com.cafeyvinowinebar.cafe_y_vino_client.ui.carta.item_specs

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cafeyvinowinebar.cafe_y_vino_client.KEY_ITEMS
import com.cafeyvinowinebar.cafe_y_vino_client.data.data_models.ItemMenuFirestore
import com.cafeyvinowinebar.cafe_y_vino_client.data.data_models.asItemCanasta
import com.cafeyvinowinebar.cafe_y_vino_client.data.repositories.MenuDataRepository
import com.cafeyvinowinebar.cafe_y_vino_client.data.repositories.UserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ItemSpecsViewModel @Inject constructor(
    private val userDataRepo: UserDataRepository,
    private val menuDataRepo: MenuDataRepository

) : ViewModel() {

    private val _uiState = MutableStateFlow(SpecsUiState())
    val uiState: StateFlow<SpecsUiState> = _uiState.asStateFlow()

    // listen to the presence status
    init {
        viewModelScope.launch(Dispatchers.IO) {
            userDataRepo.getUserPresenceFlow().collect { isPresent ->
                _uiState.update {
                    it.copy(
                        isPresent = isPresent
                    )
                }
            }
        }
    }

    /**
     * When first entering the screen we need to tell the view pager what collection to display (what category of the menu)
     * For that we get an ArrayList of menu items as argument (with the selected item's name, to know where in the collection to start)
     * When we get those arguments we update the UI state with them
     */
    fun setArgsOnUiState(items: ArrayList<ItemMenuFirestore>, initialName: String) = viewModelScope.launch(Dispatchers.Default) {

        // to define the initial position where the ViewPager should start, we iterate through the list, and compare the names of items to the name
        // of the item chosen by the user
        // when the name matches, we retrieve the position of the item
        var initialPosition = 0
        items.forEach {
            if (initialName == it.nombre) {
                initialPosition = items.indexOf(it)
            }
        }
        // update the UI state with the list and the position of our item
        _uiState.update {
            it.copy(
                items = items,
                initialPosition = initialPosition
            )
        }
    }

    /**
     * Called whenever a new fragment is introduced to the ViewPager
     */
    fun setThePagingFragment(
        currentPosition: Int
    ) = viewModelScope.launch {
        _uiState.update {
            it.copy(
                currentPosition = currentPosition,
                currentItem = it.items?.get(currentPosition),
                setSize = it.items?.size,
                itemImgReference = it.items?.get(currentPosition)?.image?.let { imgPath ->
                    menuDataRepo.getImgReference(imgPath)
                }
            )
        }
    }

    fun addItemToCanastaDb(itemMenu: ItemMenuFirestore) = viewModelScope.launch {

        // transform item menu to the entity class
        val item = itemMenu.asItemCanasta()
        menuDataRepo.addProductToCanasta(item)
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