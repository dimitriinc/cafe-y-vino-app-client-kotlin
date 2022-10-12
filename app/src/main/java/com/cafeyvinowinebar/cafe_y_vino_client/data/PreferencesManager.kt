package com.cafeyvinowinebar.cafe_y_vino_client.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

data class UserPreferences(
    val canSendPedidos: Boolean,
    val isUserPresent: Boolean
)

class PreferencesManager @Inject constructor(
    val dataStore: DataStore<Preferences>
    ) {

    val preferencesFlow = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val canSendPedidos = preferences[PreferencesKeys.CAN_SEND_PEDIDOS] ?: true
            val isUserPresent = preferences[PreferencesKeys.IS_USER_PRESENT] ?: false
            UserPreferences(canSendPedidos, isUserPresent)
        }

    suspend fun updateCanSendPedidos(canSendPedidos: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CAN_SEND_PEDIDOS] = canSendPedidos
        }
    }

    suspend fun updateIsUserPresent(isUserPresent: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_USER_PRESENT] = isUserPresent
        }
    }

    suspend fun getCanSendPedidos() =
        dataStore.data.first().toPreferences()[PreferencesKeys.CAN_SEND_PEDIDOS] ?: true


    private object PreferencesKeys {
        val CAN_SEND_PEDIDOS = booleanPreferencesKey("canSendPedidos")
        val IS_USER_PRESENT = booleanPreferencesKey("isUserPresent")
    }

}
