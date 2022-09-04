package com.cafeyvinowinebar.cafe_y_vino_client.data.sources

import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import javax.inject.Inject

class FirebaseStorageSource @Inject constructor(
    private val fStorage: FirebaseStorage
) {

    fun getImgReference(imgPath: String): StorageReference {
        val storageReference = fStorage.reference
        return storageReference.child(imgPath)
    }
}