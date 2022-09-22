package com.cafeyvinowinebar.cafe_y_vino_client.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cafeyvinowinebar.cafe_y_vino_client.data.repositories.UserDataRepository
import com.cafeyvinowinebar.cafe_y_vino_client.data.repositories.UtilsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val utilsRepo: UtilsRepository,
    private val userDataRepo: UserDataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {

        if (userDataRepo.getUserObject() != null) {
            _uiState.update {
                it.copy(
                    isLoggedIn = true
                )
            }
        }

        viewModelScope.launch {
            userDataRepo.userPresenceFlow.collect { isPresent ->
                _uiState.update {
                    it.copy(
                        isUserPresent = isPresent
                    )
                }
            }
        }
    }

    fun getUtilsForEntryRequest() {}


}