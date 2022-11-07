package com.cafeyvinowinebar.cafe_y_vino_client.data.data_models

import com.google.firebase.firestore.PropertyName

/**
 * Represents a gift shop item as it's stored in the FirestoreDB
 * A customer spends his bonus points to acquire the gift in the fidelity program
 * Model for the AdapterGiftshop
 */

data class GiftFirestore(
    var nombre: String ?= null,
    var precio: String ?= null,
    var imagen: String ?= null,
    @PropertyName("isPresent")
    var isPresent: Boolean ?= null
) {
}