package com.cafeyvinowinebar.cafe_y_vino_client.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

data class UserPreferences(
    val canSendPedidos: Boolean,
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
            UserPreferences(canSendPedidos)
        }

    suspend fun updateCanSendPedidos(canSendPedidos: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CAN_SEND_PEDIDOS] = canSendPedidos
        }
    }

    private object PreferencesKeys {
        val CAN_SEND_PEDIDOS = booleanPreferencesKey("canSendPedidos")
    }

}
