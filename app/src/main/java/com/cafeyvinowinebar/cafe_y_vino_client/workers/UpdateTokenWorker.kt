package com.cafeyvinowinebar.cafe_y_vino_client.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.cafeyvinowinebar.cafe_y_vino_client.KEY_TOKEN
import com.cafeyvinowinebar.cafe_y_vino_client.KEY_USER_ID
import com.google.firebase.firestore.FirebaseFirestore

class UpdateTokenWorker(context: Context, workingParams: WorkerParameters) : Worker(context, workingParams) {
    override fun doWork(): Result {
        val token = inputData.getString(KEY_TOKEN) ?: return Result.failure()
        val userId = inputData.getString(KEY_USER_ID) ?: return Result.failure()
        FirebaseFirestore.getInstance()
            .collection("usuarios")
            .document(userId)
            .update(KEY_TOKEN, token)
        return Result.success()
    }
}