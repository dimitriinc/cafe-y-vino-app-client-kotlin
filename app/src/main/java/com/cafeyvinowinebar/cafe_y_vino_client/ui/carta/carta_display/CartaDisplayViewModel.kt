package com.cafeyvinowinebar.cafe_y_vino_client.ui.carta.carta_display

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cafeyvinowinebar.cafe_y_vino_client.*
import com.cafeyvinowinebar.cafe_y_vino_client.data.canasta.ItemCanasta
import com.cafeyvinowinebar.cafe_y_vino_client.data.repositories.MenuDataRepository
import com.cafeyvinowinebar.cafe_y_vino_client.data.repositories.UserDataRepository
import com.cafeyvinowinebar.cafe_y_vino_client.data.repositories.UtilsRepository
import com.google.firebase.firestore.DocumentSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CartaDisplayViewModel @Inject constructor(
    private val userDataRepo: UserDataRepository,
    private val menuDataRepo: MenuDataRepository,
    private val utilsRepo: UtilsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartaDisplayUiState())
    val uiState: StateFlow<CartaDisplayUiState> = _uiState.asStateFlow()


    init {

        // set the presence status in the UI state
        viewModelScope.launch {
            userDataRepo.userFlow.collect {
                _uiState.update {
                    it.copy(isPresent = it.isPresent)
                }
            }
        }

        // set the happy hour value in the UI state
        viewModelScope.launch(Dispatchers.Default) {

            val utilsHappyHour = utilsRepo.getUtilsForHappyHour()

            val day = LocalDate.now().dayOfWeek.name

            // iterate through the list of happy days and check if the current day is one of them
            // if not, set 'false'
            utilsHappyHour.happyDays.forEach { happyDay ->
                if (day == happyDay) {
                    // the day is happy, now we have to find out if the current hour is happy
                    // if it is, set 'true'
                    val calendar = Calendar.getInstance()
                    calendar.timeZone = TimeZone.getTimeZone(GMT)
                    val hour = calendar.get(Calendar.HOUR_OF_DAY)
                    utilsHappyHour.happyHours.forEach { happyHour ->
                        if (hour == happyHour.toInt()) {
                            _uiState.update {
                                it.copy(
                                    isHappyHour = true
                                )
                            }
                        } else {
                            _uiState.update {
                                it.copy(
                                    isHappyHour = false
                                )
                            }
                        }
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isHappyHour = false
                        )
                    }
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