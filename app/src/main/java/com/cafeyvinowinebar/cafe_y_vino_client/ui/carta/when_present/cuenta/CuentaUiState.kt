package com.cafeyvinowinebar.cafe_y_vino_client.ui.carta.when_present.cuenta

data class CuentaUiState(
    val isPresent: Boolean = true,
    val isPedirCuentaFabExpanded: Boolean = false,
    val arePayModeFabsExpanded: Boolean = false,
    val canSendPedidos: Boolean = true,
    val totalCuentaCost: Double = 0.0,
    val userId: String
)
