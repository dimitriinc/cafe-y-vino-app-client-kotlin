package com.cafeyvinowinebar.cafe_y_vino_client.ui.reservas

import androidx.appcompat.widget.PopupMenu

data class ReservasUiState(
    val fecha: String? = null,
    val part: String? = null,
    val mesa: String? = null,
    val hora: String? = null,
    val pax: String? = null,
    val comentario: String? = null,
    val horas: List<String> = emptyList(),
    val horasDeDia: List<String> = emptyList(),
    val horasDeNoche: List<String> = emptyList(),
    val userMessage: String? = null,
    val mesasPopup: PopupMenu? = null,
    val passToMesasAllowed: Boolean = false,
    val passToFinAllowed: Boolean = false,
    val isReservaSent: Boolean? = null,
    val firstName: String = ""
) {
}