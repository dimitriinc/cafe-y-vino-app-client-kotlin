package com.cafeyvinowinebar.cafe_y_vino_client.ui.carta.carta_display.categories

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cafeyvinowinebar.cafe_y_vino_client.GMT
import com.cafeyvinowinebar.cafe_y_vino_client.data.repositories.UserDataRepository
import com.cafeyvinowinebar.cafe_y_vino_client.data.repositories.UtilsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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

        // set the happy hour value in the UI state
        viewModelScope.launch(Dispatchers.IO) {

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
                            Log.d(TAG, "HAPPY HOUR IS TRUE")
                        } else {
                            _uiState.update {
                                it.copy(
                                    isHappyHour = false
                                )
                            }
                            Log.d(TAG, "HAPPY HOUR IS FALSE")
                        }
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isHappyHour = false
                        )
                    }
                    Log.d(TAG, "HAPPY HOUR IS FALSE")
                }
            }
        }
    }
}