package com.cafeyvinowinebar.cafe_y_vino_client.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.cafeyvinowinebar.cafe_y_vino_client.KEY_EMAIL
import com.google.firebase.auth.FirebaseAuth

class UpdateEmailWorker(context: Context, workerParams: WorkerParameters) : Worker(context,
    workerParams
) {
    override fun doWork(): Result {
        val email = inputData.getString(KEY_EMAIL) ?: return Result.failure()
        FirebaseAuth.getInstance().currentUser?.updateEmail(email)
        return Result.success()
    }
}