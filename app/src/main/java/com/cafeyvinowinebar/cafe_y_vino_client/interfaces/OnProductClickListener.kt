package com.cafeyvinowinebar.cafe_y_vino_client.interfaces

import com.cafeyvinowinebar.cafe_y_vino_client.data.model_classes.ItemMenu
import com.google.firebase.firestore.DocumentSnapshot

interface OnProductClickListener {
    fun onClick(document: DocumentSnapshot, items: ArrayList<ItemMenu>)
}