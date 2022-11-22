package com.cafeyvinowinebar.cafe_y_vino_client.ui.carta.carta_display.categories

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cafeyvinowinebar.cafe_y_vino_client.GMT
import com.cafeyvinowinebar.cafe_y_vino_client.data.repositories.UserDataRepository
import com.cafeyvinowinebar.cafe_y_vino_client.data.repositories.UtilsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.*
import javax.inject.Inject

private const val TAG = "CategoriesViewModel"
@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val utilsRepo: UtilsRepository,
    private val userDataRepo: UserDataRepository,
) : ViewModel(){

    private val _uiState = MutableStateFlow(CategoriesUiState())
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()

    init {
        // set the presence status in the UI state
        viewModelScope.launch {
            userDataRepo.getUserPresenceFlow().collect { isUserPresent ->
                _uiState.update {
                    it.copy(isPresent = isUserPresent)
                }
            }
        }
    }

    fun setHappyHour() = viewModelScope.launch(Dispatchers.IO) {

        utilsRepo.getUtilsForHappyHour().collect { utilsHappyHour ->
            val day = LocalDate.now().dayOfWeek.name

            // iterate through the list of happy days and check if the current day is one of them
            // if not, set 'false'
            var happyHourIsSetToTrue = false
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
                            happyHourIsSetToTrue = true
                        }
                    }
                }
            }
            if (!happyHourIsSetToTrue) {
                _uiState.update {
                    it.copy(
                        isHappyHour = false
                    )
                }
            }
        }
    }
}