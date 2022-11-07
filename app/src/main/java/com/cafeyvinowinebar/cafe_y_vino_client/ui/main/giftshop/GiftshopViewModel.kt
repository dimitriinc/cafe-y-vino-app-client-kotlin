package com.cafeyvinowinebar.cafe_y_vino_client.ui.main.giftshop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cafeyvinowinebar.cafe_y_vino_client.data.repositories.MenuDataRepository
import com.cafeyvinowinebar.cafe_y_vino_client.data.repositories.UserDataRepository
import com.cafeyvinowinebar.cafe_y_vino_client.data.sources.FirebaseMessagingSource
import com.cafeyvinowinebar.cafe_y_vino_client.ui.data_models.Gift
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GiftshopViewModel @Inject constructor(
    private val userDataRepo: UserDataRepository,
    private val fMessaging: FirebaseMessagingSource,
    private val productsRepo: MenuDataRepository
) : ViewModel(){

    private val _uiState = MutableStateFlow(GiftshopUiState())
    val uiState: StateFlow<GiftshopUiState> = _uiState.asStateFlow()

    init {

        /**
         * get some of the user data and store it in the UI state
         * listen to the presence and bonus points
         */
        viewModelScope.launch(Dispatchers.IO) {
            val firstName = userDataRepo.getUserFirstName()
            userDataRepo.getUserFlow().collect { user ->
                _uiState.update { uiState ->
                    uiState.copy(
                        isUserPresent = user.isPresent,
                        userFirstName = firstName,
                        bonos = user.bonos,
                        userName = user.nombre
                    )
                }
            }
        }
    }


    /**
     * Gathers necessary data to send a message to admin to notify them about a new gift to serve, and to store the gift in the Firestore DB
     * Calls the products repo's function to store the gift
     * Calls the appropriate messaging source function
     * Subtracts the gift's price from the total user bonos, and passes the value to the repo's fun to update the user's bonos
     */
    fun sendGiftRequest(gift: Gift) = viewModelScope.launch {
        val userMesa = userDataRepo.getUser().mesa
        val userId = userDataRepo.getUserId()
        productsRepo.storeGift(
            gift.nombre,
            gift.precio.toLong(),
            userMesa,
            userId,
            _uiState.value.userName
        )
        fMessaging.sendGiftMessage(
            gift.nombre,
            userMesa,
            _uiState.value.userName
        )
        userDataRepo.updateBonos(_uiState.value.bonos - gift.precio.toLong())
    }
}