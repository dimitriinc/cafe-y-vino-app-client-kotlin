package com.cafeyvinowinebar.cafe_y_vino_client.data.repositories

import com.cafeyvinowinebar.cafe_y_vino_client.data.model_classes.GiftToSend
import com.cafeyvinowinebar.cafe_y_vino_client.data.sources.FirebaseFirestoreSource
import com.google.firebase.Timestamp
import java.util.*
import javax.inject.Inject

class ProductsDataRepository @Inject constructor(
    private val fStoreSource: FirebaseFirestoreSource
) {
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