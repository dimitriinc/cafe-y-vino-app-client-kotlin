package com.cafeyvinowinebar.cafe_y_vino_client.ui.introduction

import androidx.lifecycle.ViewModel
import com.cafeyvinowinebar.cafe_y_vino_client.data.repositories.UserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class IntroductionViewModel @Inject constructor(

    private val userDataRepo: UserDataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(IntroUiState())
    val uiState: StateFlow<IntroUiState> = _uiState.asStateFlow()



    fun authenticateUser(
        email: String,
        password: String
    ) {
        userDataRepo.authenticateUser(email, password)
    }
}