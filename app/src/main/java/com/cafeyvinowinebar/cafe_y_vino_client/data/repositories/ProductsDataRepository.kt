package com.cafeyvinowinebar.cafe_y_vino_client.data.repositories

import com.cafeyvinowinebar.cafe_y_vino_client.data.data_models.GiftToSend
import com.cafeyvinowinebar.cafe_y_vino_client.data.sources.FirebaseFirestoreSource
import com.google.firebase.Timestamp
import java.util.*
import javax.inject.Inject

/**
 * Repository for the operations on the menu items, stored in the "menu" collection, and also on the gifts, stored in the "regalos" collection
 */
class ProductsDataRepository @Inject constructor(
    private val fStoreSource: FirebaseFirestoreSource
) {

    /**
     * Gather the data to build a gift object that will be stored in the "pedidos" collection of the Firestore DB
     * Build an instance, and pass it to the Firestore source
     */
    fun storeGift(
        nombre: String,
        precio: Long,
        mesa: String,
        userId: String,
        user: String
    ) {
        val gift = GiftToSend(
            nombre = nombre,
            precio = precio,
            mesa = mesa,
            userId = userId,
            user = user,
            servido = false,
            timestamp = Timestamp(Date())
        )

        fStoreSource.storeGift(gift)
    }
}