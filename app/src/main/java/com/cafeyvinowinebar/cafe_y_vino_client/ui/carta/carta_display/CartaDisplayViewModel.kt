package com.cafeyvinowinebar.cafe_y_vino_client.ui.carta.carta_display

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cafeyvinowinebar.cafe_y_vino_client.*
import com.cafeyvinowinebar.cafe_y_vino_client.data.canasta.CanastaDao
import com.cafeyvinowinebar.cafe_y_vino_client.data.canasta.ItemCanasta
import com.cafeyvinowinebar.cafe_y_vino_client.data.sources.FirebaseFirestoreSource
import com.google.firebase.firestore.DocumentSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CartaDisplayViewModel @Inject constructor(
    private val fStore: FirebaseFirestoreSource,
    private val canastaDao: CanastaDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartaDisplayUiState())
    val uiState: StateFlow<CartaDisplayUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            fStore.userPresence.collect { isPresent ->
                _uiState.update {
                    it.copy(isPresent = isPresent ?: false)
                }
            }
        }
    }

    fun addItemToCanastaDb(document: DocumentSnapshot) = viewModelScope.launch {
        val item = ItemCanasta(
            name = document.getString(KEY_NOMBRE)!!,
            category = document.getString(KEY_CATEGORIA)!!,
            icon = document.getString(KEY_ICON),
            price = document.getLong(KEY_PRECIO)!!
        )
        canastaDao.insert(item)
    }

    fun isHourHappy() = viewModelScope.launch {
        val day = LocalDate.now().dayOfWeek.name
        val bundle = fStore.getHappyHourData()
        val happyDays = bundle.getCharArray(KEY_HAPPY_DAYS)

        happyDays?.forEach { happyDay ->
            if (day == happyDay.toString()) {
                val happyHours = bundle.getLongArray(KEY_HAPPY_HOURS)
                val calendar = Calendar.getInstance()
                calendar.timeZone = TimeZone.getTimeZone(GMT)
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                happyHours?.forEach { happyHour ->
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