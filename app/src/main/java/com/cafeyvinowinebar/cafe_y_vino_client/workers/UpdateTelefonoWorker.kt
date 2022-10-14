package com.cafeyvinowinebar.cafe_y_vino_client.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.cafeyvinowinebar.cafe_y_vino_client.KEY_TELEFONO
import com.cafeyvinowinebar.cafe_y_vino_client.KEY_USER_ID
import com.google.firebase.firestore.FirebaseFirestore

class UpdateTelefonoWorker(context: Context, workParams: WorkerParameters) :
    Worker(context, workParams) {
    override fun doWork(): Result {
        val userId = inputData.getString(KEY_USER_ID) ?: return Result.failure()
        val telefono = inputData.getString(KEY_TELEFONO) ?: return Result.failure()
        FirebaseFirestore.getInstance().collection("usuarios")
            .document(userId)
            .update(KEY_TELEFONO, telefono)
        return Result.success()

    }
}