package com.cafeyvinowinebar.cafe_y_vino_client.data.sources

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FirebaseAuthSource @Inject constructor(
    private val fAuth: FirebaseAuth
) {

    fun getUserId(): String {
        return fAuth.currentUser!!.uid
    }


}