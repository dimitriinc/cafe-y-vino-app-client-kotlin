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

    /**
     * Listens to the error flow from the fAuth source, and according to the exception type,
     * transforms it into an error message
     */
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

    /**
     * Gathers the data necessary for registration and for creating a user document in the Firestore
     * First calls a suspending fun to register the user in the fAuth
     * If the registration was successful, creates an instance of the User class for storing into the Firestore DB
     * Returns the result of the storing operation
     */
    suspend fun registerUser(
        email: String,
        password: String,
        name: String,
        phone: String,
        birthdate: String
    ): Boolean {
        val authenticated = fAuthSource.registerUser(email, password)
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

    /**
     * Stores a User document in the "usuarios" collection into the Firebase DB, using the Uid as the ID of the document
     * Return the result
     */
    private suspend fun storeUserDoc(user: UserFirestore): Boolean {
        val userId = getUserId()
        return fStoreSource.storeUserDoc(user, userId)
    }

    /**
     * Pass the email further to the fAuth source
     * Return the result of the operation
     */
    suspend fun resetPassword(email: String): Boolean {
        return fAuthSource.resetPassword(email)
    }

    /**
     * Pass the email + password further for the logging in
     * Return the result
     */
    suspend fun loginUser(email: String, password: String): Boolean {
        return fAuthSource.loginUser(email, password)
    }

    fun logout() {
        fAuthSource.logout()
    }

    /**
     * Updates email in the fAuth system
     */
    suspend fun updateEmail(email: String) = fAuthSource.updateEmail(email)


    // TODO: update the writes according to the offline-first principles
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
                .setTelefono(userSnapshot.getString(KEY_TELEFONO))
                .setEmail(userSnapshot.getString(KEY_EMAIL))
                .setToken(userSnapshot.getString(KEY_TOKEN))
                .setMesa(userSnapshot.getString(KEY_MESA))
                .setIsPresent(userSnapshot.getBoolean(KEY_IS_PRESENT)!!)
                .setBonos(userSnapshot.getLong(KEY_BONOS)!!)
                .build()
        }
    }
}