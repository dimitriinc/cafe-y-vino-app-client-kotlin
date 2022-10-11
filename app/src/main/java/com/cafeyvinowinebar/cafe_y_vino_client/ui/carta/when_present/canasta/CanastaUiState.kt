package com.cafeyvinowinebar.cafe_y_vino_client.ui.carta.when_present.canasta

import androidx.lifecycle.LiveData
import com.cafeyvinowinebar.cafe_y_vino_client.data.canasta.ItemCanasta

data class CanastaUiState(
    val isPresent: Boolean = true,
    val items: LiveData<List<ItemCanasta>>,
    val areCanastaFabsExpanded: Boolean = false,
    val canSendPedidos: Boolean = true
)
