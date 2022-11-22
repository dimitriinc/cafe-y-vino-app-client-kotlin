package com.cafeyvinowinebar.cafe_y_vino_client

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.firestore.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlin.coroutines.CoroutineContext

private const val TAG = "Extensions"
fun Context.isDarkThemeOn(): Boolean {
    return resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
}

fun DocumentReference.snapshotAsFlow(
    coroutineContext: CoroutineContext = Dispatchers.IO
): Flow<DocumentSnapshot?> = callbackFlow {
    try {
        val listener = EventListener<DocumentSnapshot> { snapshot, error ->
            if (error != null) {
                close(error)
                return@EventListener
            }
            trySend(snapshot).isSuccess
        }
        val registration = addSnapshotListener(listener)
        awaitClose { registration.remove() }
    } catch (e: FirebaseFirestoreException) {
        close(e)
    } catch (e: FirebaseException) {
        close(e)
    }
}.flowOn(coroutineContext)

fun Query.snapshotAsFlow(
    coroutineContext: CoroutineContext = Dispatchers.IO
): Flow<QuerySnapshot?> = callbackFlow {
    try {
        val listener = EventListener<QuerySnapshot> { snapshot, error ->
            if (error != null) {
                close(error)
                return@EventListener
            }
            trySend(snapshot).isSuccess
        }
        val registration = addSnapshotListener(listener)
        awaitClose { registration.remove() }
    } catch (e: FirebaseFirestoreException) {
        close(e)
    } catch (e: FirebaseException) {
        close(e)
    }
}.flowOn(coroutineContext)

fun Query.asCuentaTotalFlow(
    coroutineContext: CoroutineContext = Dispatchers.IO
): Flow<Long> = snapshotAsFlow(coroutineContext).map { query ->
    var total: Long = 0
    query?.forEach {
        val itemTotal = it.getLong("total")
        if (itemTotal != null) {
            total += itemTotal
        }
    }
    Log.d(TAG, "asCuentaTotalFlow: TOTAL_CUENTA: $total")
    total

}

