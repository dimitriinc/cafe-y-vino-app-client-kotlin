package com.cafeyvinowinebar.cafe_y_vino_client.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.findNavController
import com.cafeyvinowinebar.cafe_y_vino_client.MainNavGraphDirections
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "MainActivity"
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val localBroadcastManager = LocalBroadcastManager.getInstance(applicationContext)

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {

                Log.d(TAG, "onReceive: the Intent is received!!!!!!")
                val action = MainNavGraphDirections.actionGlobalApologyFragment()
                val navController = findNavController(R.id.main_host_fragment)
                navController.navigate(action)
                localBroadcastManager.unregisterReceiver(this)
            }
        }
        val filter = IntentFilter("com.cafeyvinowinebar.RESTORE_DATA")
        localBroadcastManager.registerReceiver(receiver, filter)
    }
}