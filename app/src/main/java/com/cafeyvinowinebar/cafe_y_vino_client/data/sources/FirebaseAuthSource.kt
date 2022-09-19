package com.cafeyvinowinebar.cafe_y_vino_client.data.sources

import android.content.res.Resources
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.cafeyvinowinebar.cafe_y_vino_client.data.model_classes.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthSource @Inject constructor(
    private val fAuth: FirebaseAuth
) {

    private val _errorFlow: MutableStateFlow<Throwable?> = MutableStateFlow(null)
    val errorFlow: StateFlow<Throwable?> = _errorFlow.asStateFlow()

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
        } catch (e: Throwable) {
            _errorFlow.update {
                e
            }
            false
        }

    }

    suspend fun resetPassword(email: String) {
        try {
            fAuth.sendPasswordResetEmail(email).await()
            // TODO: get a success message to the UI
        } catch (e: Throwable) {
            _errorFlow.update {
                e
            }
        }
    }

    suspend fun loginUser(email: String, password: String): Boolean {
        return try {
            fAuth.signInWithEmailAndPassword(email, password).await()
            true
        } catch (e: Throwable) {
            _errorFlow.update {
                e
            }
            false
        }
    }
}