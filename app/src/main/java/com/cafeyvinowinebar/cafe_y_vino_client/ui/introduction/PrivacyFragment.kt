package com.cafeyvinowinebar.cafe_y_vino_client.ui.introduction

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import com.cafeyvinowinebar.cafe_y_vino_client.R
import dagger.hilt.android.AndroidEntryPoint

/**
 * Displays a FAQ about the privacy policy
 * Sets a flag in the UI state to let know the registration fragment to recreate the privacy dialog
 */
@AndroidEntryPoint
class PrivacyFragment : Fragment(R.layout.fragment_privacy_policy) {

    private val viewModel: IntroductionViewModel by hiltNavGraphViewModels(R.id.intro_nav_graph)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.setPrivacyFlag(true)
    }
}