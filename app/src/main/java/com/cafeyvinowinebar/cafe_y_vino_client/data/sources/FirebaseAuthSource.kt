package com.cafeyvinowinebar.cafe_y_vino_client.data.sources

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Writes and reads from the Firebase Authentication system
 */
class FirebaseAuthSource @Inject constructor(
    private val fAuth: FirebaseAuth
) {

    /**
     * When an operation fails, it populates the error flow with its exception
     * Which down the stream transforms into a message that the user sees on the screen
     */
    private val _errorFlow: MutableStateFlow<Throwable?> = MutableStateFlow(null)
    val errorFlow: StateFlow<Throwable?> = _errorFlow.asStateFlow()

    /**
     * Simple read of the user object
     */
    fun getUserObject(): FirebaseUser? =
        fAuth.currentUser

    /**
     * We user email + password authentication
     * The caller gets response from the suspending await() in form of a boolean
     * In addition, if the registration fails, the error gets transmitted via the errorFlow
     */
    suspend fun registerUser(
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

    /**
     * Send a new password form to the email provided
     * Return the result of the operation
     * If false, update the error flow
     */
    suspend fun resetPassword(email: String): Boolean {
        return try {
            fAuth.sendPasswordResetEmail(email).await()
            true
        } catch (e: Throwable) {
            _errorFlow.update {
                e
            }
            false
        }
    }

    /**
     * Try to log in the user using the provided email and password
     * Return the result
     * If false, update the error flow
     */
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

    fun logout() {
        fAuth.signOut()
    }

    /**
     * Update the email in the fAuth system, return the result
     */
    suspend fun updateEmail(email: String): Boolean {
        return try {
            val user = getUserObject()
            user?.updateEmail(email)?.await()
            true
        } catch (e: Throwable) {
            false
        }
    }
}