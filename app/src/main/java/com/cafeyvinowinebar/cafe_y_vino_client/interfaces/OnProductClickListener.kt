package com.cafeyvinowinebar.cafe_y_vino_client.interfaces

import com.cafeyvinowinebar.cafe_y_vino_client.data.data_models.ItemMenuFirestore
import com.google.firebase.firestore.DocumentSnapshot

interface OnProductClickListener {
    fun onClick(document: DocumentSnapshot, items: ArrayList<ItemMenuFirestore>)
}