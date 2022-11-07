package com.cafeyvinowinebar.cafe_y_vino_client.workers

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.cafeyvinowinebar.cafe_y_vino_client.KEY_NOMBRE
import com.cafeyvinowinebar.cafe_y_vino_client.KEY_USER_ID
import com.google.firebase.firestore.FirebaseFirestore

class UpdateNombreWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    override fun doWork(): Result {
        val nombre = inputData.getString(KEY_NOMBRE) ?: return Result.failure()
        val userId = inputData.getString(KEY_USER_ID) ?: return Result.failure()
        Log.d(TAG, "doWork: name: $nombre \n userId: $userId")
        FirebaseFirestore.getInstance().collection("usuarios")
            .document(userId)
            .update(KEY_NOMBRE, nombre)
        return Result.success()
    }

}