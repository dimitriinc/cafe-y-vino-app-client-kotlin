package com.cafeyvinowinebar.cafe_y_vino_client.data.model_classes

/**
 * Represents a gift shop item as it's stored in the FirestoreDB
 * A customer spends his bonus points to acquire the gift in the fidelity program
 * Model for the AdapterGiftshop
 */

class Gift(
    private val nombre: String,
    private val precio: String,
    private val imagen: String,
    private val isPresent: Boolean
) {
}