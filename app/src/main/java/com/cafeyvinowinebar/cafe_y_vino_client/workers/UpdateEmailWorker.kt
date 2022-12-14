package com.cafeyvinowinebar.cafe_y_vino_client.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.cafeyvinowinebar.cafe_y_vino_client.KEY_EMAIL
import com.cafeyvinowinebar.cafe_y_vino_client.KEY_USER_ID
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UpdateEmailWorker(context: Context, workerParams: WorkerParameters) : Worker(context,
    workerParams
) {
    override fun doWork(): Result {
        val email = inputData.getString(KEY_EMAIL) ?: return Result.failure()
        val userId = inputData.getString(KEY_USER_ID) ?: return Result.failure()
        FirebaseAuth.getInstance().currentUser?.updateEmail(email)
        FirebaseFirestore.getInstance().collection("usuarios")
            .document(userId)
            .update(KEY_EMAIL, email)
        return Result.success()
    }
}