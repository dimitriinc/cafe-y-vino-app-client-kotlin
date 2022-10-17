package com.cafeyvinowinebar.cafe_y_vino_client.data.repositories

import android.content.Context
import android.content.res.Resources
import androidx.datastore.core.DataStore
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.cafeyvinowinebar.cafe_y_vino_client.*
import com.cafeyvinowinebar.cafe_y_vino_client.data.data_models.UserFirestore
import com.cafeyvinowinebar.cafe_y_vino_client.ui.data_models.User
import com.cafeyvinowinebar.cafe_y_vino_client.data.sources.FirebaseAuthSource
import com.cafeyvinowinebar.cafe_y_vino_client.data.sources.FirebaseFirestoreSource
import com.cafeyvinowinebar.cafe_y_vino_client.data.sources.FirebaseMessagingSource
import com.cafeyvinowinebar.cafe_y_vino_client.di.ApplicationScope
import com.cafeyvinowinebar.cafe_y_vino_client.workers.UpdateBonosWorker
import com.cafeyvinowinebar.cafe_y_vino_client.workers.UpdateEmailWorker
import com.cafeyvinowinebar.cafe_y_vino_client.workers.UpdateNombreWorker
import com.cafeyvinowinebar.cafe_y_vino_client.workers.UpdateTelefonoWorker
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import dagger.hilt.android.qualifiers.ApplicationContext
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
    private val appScope: CoroutineScope,
    @ApplicationContext val context: Context
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
     * Transmits the object store in the user dataStore as a User instance
     */
    val userFlow: Flow<User> = userDataStore.data.map {
        User(
            nombre = it.nombre,
            email = it.email,
            telefono = it.telefono,
            token = it.token,
            mesa = it.mesa,
            isPresent = it.isPresent,
            bonos = it.bonos
        )
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

    suspend fun getUserFirstName(): String =
        userDataStore.data.first()
            .nombre
            .substringBefore(" ")
            .lowercase()
            .replaceFirstChar {
                it.uppercaseChar()
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
     * The following four functions update different user data values according to the offline-first principles
     * First we update the local data (the UserData object in the Proto DataStore)
     * And then we submit a worker to the WorkManager to update the value in the Firestore DB
     */
    suspend fun updateEmail(email: String): Boolean {
        return try {
            userDataStore.updateData { user ->
                user.toBuilder().setEmail(email).build()
            }
            val request = OneTimeWorkRequestBuilder<UpdateEmailWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setInputData(
                    workDataOf(
                        KEY_EMAIL to email,
                        KEY_USER_ID to getUserId()
                    )
                )
                .build()
            WorkManager.getInstance(context).enqueue(request)
            true
        } catch (e: Throwable) {
            false
        }

    }
    suspend fun updateNombre(nombre: String): Boolean {
        return try {
            userDataStore.updateData { user ->
                user.toBuilder().setNombre(nombre).build()
            }
            val request = OneTimeWorkRequestBuilder<UpdateNombreWorker>()
                .setInputData(workDataOf(
                    KEY_NOMBRE to nombre,
                    KEY_USER_ID to getUserId()
                ))
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()
            WorkManager.getInstance(context).enqueue(request)
            true
        } catch (e: Throwable) {
            false
        }
    }
    suspend fun updateTelefono(telefono: String): Boolean {
        return try {
            userDataStore.updateData { user ->
                user.toBuilder().setTelefono(telefono).build()
            }
            val request = OneTimeWorkRequestBuilder<UpdateTelefonoWorker>()
                .setInputData(workDataOf(
                    KEY_USER_ID to getUserId(),
                    KEY_TELEFONO to telefono
                ))
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()
            WorkManager.getInstance(context).enqueue(request)
            true
        } catch (e: Throwable) {
            false
        }
    }
    suspend fun updateBonos(newBonos: Long) {
        userDataStore.updateData { user ->
            user.toBuilder().setBonos(newBonos).build()
        }
        val request = OneTimeWorkRequestBuilder<UpdateBonosWorker>()
            .setInputData(workDataOf(
                KEY_USER_ID to getUserId(),
                KEY_BONOS to newBonos
            ))
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()
        WorkManager.getInstance(context).enqueue(request)
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