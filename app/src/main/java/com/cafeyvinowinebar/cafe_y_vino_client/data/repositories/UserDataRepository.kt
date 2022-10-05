package com.cafeyvinowinebar.cafe_y_vino_client.data.repositories

import android.content.res.Resources
import androidx.datastore.core.DataStore
import com.cafeyvinowinebar.cafe_y_vino_client.*
import com.cafeyvinowinebar.cafe_y_vino_client.data.model_classes.User
import com.cafeyvinowinebar.cafe_y_vino_client.data.sources.FirebaseAuthSource
import com.cafeyvinowinebar.cafe_y_vino_client.data.sources.FirebaseFirestoreSource
import com.cafeyvinowinebar.cafe_y_vino_client.data.sources.FirebaseMessagingSource
import com.cafeyvinowinebar.cafe_y_vino_client.di.ApplicationScope
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class UserDataRepository @Inject constructor(
    private val fAuthSource: FirebaseAuthSource,
    private val fMessagingSource: FirebaseMessagingSource,
    private val fStoreSource: FirebaseFirestoreSource,
    private val userDataStore: DataStore<UserData>,
    @ApplicationScope
    private val appScope: CoroutineScope
) {

    init {
        appScope.launch {
            fStoreSource.userFlow.collect { userSnapshot ->
                updateUserData(userSnapshot!!)
            }
        }

    }


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

    val userPresenceFlow: Flow<Boolean> = userDataStore.data.map {
        it.isPresent
    }

    val userBonosFlow: Flow<Long> = userDataStore.data.map {
        it.bonos
    }

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

    suspend fun updateEmail(email: String) = fAuthSource.updateEmail(email)
    suspend fun updateNombre(nombre: String) = fStoreSource.updateNombre(nombre)
    suspend fun updateTelefono(telefono: String) = fStoreSource.updateTelefono(telefono)

    suspend fun updateBonos(newBonos: Long) {
        userDataStore.updateData { user ->
            user.toBuilder().setBonos(newBonos).build()
        }
    }

    private suspend fun updateUserData(userSnapshot: DocumentSnapshot) {
        userDataStore.updateData { user ->
            user.toBuilder()
                .setNombre(userSnapshot.getString(KEY_NOMBRE))
                .setTelefono(userSnapshot.getString("telefono"))
                .setEmail(userSnapshot.getString("email"))
                .setToken(userSnapshot.getString("token"))
                .setMesa(userSnapshot.getString(KEY_MESA))
                .setIsPresent(userSnapshot.getBoolean(KEY_IS_PRESENT)!!)
                .setBonos(userSnapshot.getLong("bonos")!!)
                .build()
        }
    }

}