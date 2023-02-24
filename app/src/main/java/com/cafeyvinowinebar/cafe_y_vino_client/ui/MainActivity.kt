package com.cafeyvinowinebar.cafe_y_vino_client.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.cafeyvinowinebar.cafe_y_vino_client.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }
}