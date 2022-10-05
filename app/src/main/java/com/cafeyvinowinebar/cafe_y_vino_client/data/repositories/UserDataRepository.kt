package com.cafeyvinowinebar.cafe_y_vino_client.data.repositories

import android.content.res.Resources
import androidx.datastore.core.DataStore
import com.cafeyvinowinebar.cafe_y_vino_client.*
import com.cafeyvinowinebar.cafe_y_vino_client.data.data_models.UserFirestore
import com.cafeyvinowinebar.cafe_y_vino_client.ui.data_models.User
import com.cafeyvinowinebar.cafe_y_vino_client.data.sources.FirebaseAuthSource
import com.cafeyvinowinebar.cafe_y_vino_client.data.sources.FirebaseFirestoreSource
import com.cafeyvinowinebar.cafe_y_vino_client.data.sources.FirebaseMessagingSource
import com.cafeyvinowinebar.cafe_y_vino_client.di.ApplicationScope
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * A repository for the operations that manipulate the data stored in the user document in the 'usuarios' collection in the Firestore
 */
class UserDataRepository @Inject constructor(
    private val fAuthSource: FirebaseAuthSource,
    private val fMessagingSource: FirebaseMessagingSource,
    private val fStoreSource: FirebaseFirestoreSource,
    private val userDataStore: DataStore<UserData>,
    @ApplicationScope
    private val appScope: CoroutineScope
) {

    init {

        // inside the application scope we listen to the user document and update the object stored in the Proto DataStore
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

    /**
     * Gets the flow from the Proto DataStore, converts it to a flow of the isPresent property for its exposure to the UI layer
     */
    val userPresenceFlow: Flow<Boolean> = userDataStore.data.map {
        it.isPresent
    }

    /**
     * Gets the flow from the Proto DataStore, converts it to a flow of the bonos property for its exposure to the UI layer
     */
    val userBonosFlow: Flow<Long> = userDataStore.data.map {
        it.bonos
    }

    /**
     * Returns an Auth object for the start destination to decided if the user is logged in
     */
    fun getUserObject(): FirebaseUser? =
        fAuthSource.getUserObject()

    fun getUserId(): String =
        getUserObject()!!.uid

    /**
     * Gets the object from the Proto DataStore
     * Converts it to an instance of the User class, designed to be exposed to the UI layer
     * Returns the instance
     */
    suspend fun getUser(): User {
        val userData = userDataStore.data.first()
        return User(
            nombre = userData.nombre,
            telefono = userData.telefono,
            email = userData.email,
            token = userData.token,
            mesa = userData.mesa,
            isPresent = userData.isPresent,
            bonos = userData.bonos
        )
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
            val user = UserFirestore(
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


    private suspend fun storeUserDoc(user: UserFirestore): Boolean {
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

    /**
     * With a user document snapshot from the fStore flow we update the Proto DataStore object
     */
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