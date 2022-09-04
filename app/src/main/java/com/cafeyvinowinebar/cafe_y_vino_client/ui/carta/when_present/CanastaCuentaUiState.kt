package com.cafeyvinowinebar.cafe_y_vino_client.ui.carta.when_present

import androidx.lifecycle.LiveData
import com.cafeyvinowinebar.cafe_y_vino_client.data.canasta.ItemCanasta

data class CanastaCuentaUiState(
    val isPresent: Boolean = false,
    val areCanastaFabsExpanded: Boolean = false,
    val isPedirCuentaFabExpanded: Boolean = false,
    val arePayModeFabsExpanded: Boolean = false,
    val items: LiveData<List<ItemCanasta>>,
    val canSendPedidos: Boolean = true,
    val totalCuentaCost: Double = 0.0,
    val userId: String,
    val isCuentaEmpty: Boolean = false
)
