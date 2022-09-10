package com.cafeyvinowinebar.cafe_y_vino_client.data.sources

import android.content.res.Resources
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthSource @Inject constructor(
    private val fAuth: FirebaseAuth
) {

    private val _errorMessageFlow: MutableStateFlow<String?> = MutableStateFlow(null)
    val errorMessageFlow: StateFlow<String?> = _errorMessageFlow.asStateFlow()

    fun getUserId(): String {
        return fAuth.currentUser!!.uid
    }

    suspend fun authenticateUser(
        email: String,
        password: String
    ): Boolean {
        return try {
            fAuth.createUserWithEmailAndPassword(email, password).await()
            true
        } catch (e: Exception) {
            if (e is FirebaseAuthUserCollisionException) {
                _errorMessageFlow.update {
                    Resources.getSystem().getString(R.string.email_collision)
                }
            } else {
                _errorMessageFlow.update {
                    Resources.getSystem().getString(R.string.error)
                }
            }
            false
        }

    }


}