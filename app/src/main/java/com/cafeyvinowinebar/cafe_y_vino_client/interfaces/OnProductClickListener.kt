package com.cafeyvinowinebar.cafe_y_vino_client.interfaces

import com.google.firebase.firestore.DocumentSnapshot

interface OnProductClickListener {
    fun onClick(document: DocumentSnapshot)
}