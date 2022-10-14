package com.cafeyvinowinebar.cafe_y_vino_client.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.cafeyvinowinebar.cafe_y_vino_client.KEY_BONOS
import com.cafeyvinowinebar.cafe_y_vino_client.KEY_USER_ID
import com.google.firebase.firestore.FirebaseFirestore

class UpdateBonosWorker(context: Context, workParams: WorkerParameters) :
    Worker(context, workParams) {
    override fun doWork(): Result {
        val bonos = inputData.getLong(KEY_BONOS, -1)
        if (bonos.toInt() == -1) { return Result.failure()}
        val userId = inputData.getString(KEY_USER_ID) ?: return Result.failure()
        FirebaseFirestore.getInstance().collection("usuarios")
            .document(userId)
            .update(KEY_BONOS, bonos)
        return Result.success()
    }
}