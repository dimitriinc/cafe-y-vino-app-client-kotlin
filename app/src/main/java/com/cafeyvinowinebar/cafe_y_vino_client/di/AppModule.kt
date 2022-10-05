package com.cafeyvinowinebar.cafe_y_vino_client.di

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.cafeyvinowinebar.cafe_y_vino_client.data.canasta.CanastaDatabase
import com.cafeyvinowinebar.cafe_y_vino_client.data.utils_data.UtilsDatabase
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.google.firebase.storage.ktx.storage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

private const val USER_PREFERENCES_NAME = "user_preferences"

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideCanastaDatabase(
        app: Application
    ) = Room.databaseBuilder(app, CanastaDatabase::class.java, "canasta_database")
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    @Singleton
    fun provideUtilsDatabase(
        app: Application
    ) = Room.databaseBuilder(app, UtilsDatabase::class.java, "utils_database")
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    fun provideCanastaDao(db: CanastaDatabase) = db.canastaDao()

    @Provides
    fun provideUtilsDao(db: UtilsDatabase) = db.utilsDao()

    @ApplicationScope
    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())

    @Provides
    @Singleton
    fun provideFirestore() = Firebase.firestore

    @Provides
    @Singleton
    fun provideFirebaseAuthentication() = Firebase.auth

    @Provides
    @Singleton
    fun provideFirebaseMessaging() = Firebase.messaging

    @Provides
    @Singleton
    fun provideFirebaseStorage() = Firebase.storage

    @Singleton
    @Provides
    fun providePreferencesDataStore(@ApplicationContext appContext: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler(
                produceNewData = { emptyPreferences()}
            ),
            migrations = listOf(SharedPreferencesMigration(appContext, USER_PREFERENCES_NAME)),
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile = { appContext.preferencesDataStoreFile(USER_PREFERENCES_NAME)}
        )

}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope