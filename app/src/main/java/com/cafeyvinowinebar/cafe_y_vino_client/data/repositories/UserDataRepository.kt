package com.cafeyvinowinebar.cafe_y_vino_client.data.repositories

import android.content.res.Resources
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.cafeyvinowinebar.cafe_y_vino_client.data.model_classes.User
import com.cafeyvinowinebar.cafe_y_vino_client.data.sources.FirebaseAuthSource
import com.cafeyvinowinebar.cafe_y_vino_client.data.sources.FirebaseFirestoreSource
import com.cafeyvinowinebar.cafe_y_vino_client.data.sources.FirebaseMessagingSource
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserDataRepository @Inject constructor(
    private val fAuthSource: FirebaseAuthSource,
    private val fMessagingSource: FirebaseMessagingSource,
    private val fStoreSource: FirebaseFirestoreSource
) {


    val errorMessageFlow: Flow<String?> = fAuthSource.errorFlow.map {
        when (it) {
            null -> null
            is FirebaseAuthUserCollisionException -> Resources.getSystem()
                .getString(R.string.email_collision)
            is FirebaseAuthInvalidUserException -> Resources.getSystem()
                .getString(R.string.wrong_email)
            else -> Resources.getSystem().getString(R.string.error)
        }

    }

    val userPresenceFlow: Flow<Boolean> = fStoreSource.userPresence.map { it!! }

    fun getUserObject(): FirebaseUser? =
        fAuthSource.getUserObject()

    fun getUserId(): String =
        getUserObject()!!.uid


    suspend fun getUser(): User? {
        val userDocSnapshot = getUserObject()?.let { fStoreSource.getUserDocById(it.uid) }
        return if (userDocSnapshot != null) {
            User(
                nombre = userDocSnapshot.getString("nombre")!!,
                telefono = userDocSnapshot.getString("telefono")!!,
                email = userDocSnapshot.getString("email")!!,
                token = userDocSnapshot.getString("token")!!,
                mesa = userDocSnapshot.getString("mesa")!!,
                isPresent = userDocSnapshot.getBoolean("isPresent")!!,
                bonos = userDocSnapshot.getLong("bonos")!!,
                fechaDeNacimiento = userDocSnapshot.getString("fecha de nacimiento")!!
            )
        } else {
            null
        }
    }

    suspend fun authenticateUser(
        email: String,
        password: String,
        name: String,
        phone: String,
        birthdate: String
    ): Boolean {
        val authenticated = fAuthSource.authenticateUser(email, password)
        if (authenticated) {
            val token = fMessagingSource.getToken()
            val user = User(
                nombre = name,
                telefono = phone,
                fechaDeNacimiento = birthdate,
                email = email,
                isPresent = false,
                mesa = "00",
                token = token,
                bonos = 0
            )
            return storeUserDoc(user)
        } else {
            return false
        }
    }


    private suspend fun storeUserDoc(user: User): Boolean {
        val userId = fAuthSource.getUserId()
        return fStoreSource.storeUserDoc(user, userId)
    }

    suspend fun resetPassword(email: String) {
        fAuthSource.resetPassword(email)
    }

    suspend fun loginUser(email: String, password: String): Boolean {
        return fAuthSource.loginUser(email, password)
    }

    fun logout() {
        fAuthSource.logout()
    }
}