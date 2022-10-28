package com.cafeyvinowinebar.cafe_y_vino_client.ui.introduction

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.cafeyvinowinebar.cafe_y_vino_client.databinding.AlertPrivacyBinding

class PrivacyDialog : DialogFragment() {

    val viewModel: IntroductionViewModel by hiltNavGraphViewModels(R.id.intro_nav_graph)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        
        return activity?.let {
            val view = requireActivity().layoutInflater.inflate(R.layout.alert_privacy, null)
            val binding = AlertPrivacyBinding.bind(view)
            val dialog = AlertDialog.Builder(requireContext())
                .setView(view)
                .create()
            binding.apply {
                btnPrivacyRechazar.setOnClickListener {
                    dismiss()
                }
                btnPrivacyPermitir.setOnClickListener {
                    viewModel.registerUser()
                    dismiss()

                }
                btnPrivacySaber.setOnClickListener {
                    val action = PrivacyDialogDirections.actionPrivacyDialogToPrivacyFragment()
                    findNavController().navigate(action)
                }
            }
            dialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}