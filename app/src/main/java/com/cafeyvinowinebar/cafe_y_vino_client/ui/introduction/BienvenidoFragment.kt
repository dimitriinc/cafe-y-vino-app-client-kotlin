package com.cafeyvinowinebar.cafe_y_vino_client.ui.introduction

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.cafeyvinowinebar.cafe_y_vino_client.databinding.FragmentBienvenidoBinding
import com.google.firebase.firestore.FirebaseFirestore

class BienvenidoFragment : Fragment(R.layout.fragment_bienvenido) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentBienvenidoBinding.bind(view)
        binding.apply {
            btnLog.setOnClickListener {
                val action = BienvenidoFragmentDirections.actionBienvenidoFragmentToLoginFragment()
                findNavController().navigate(action)
            }
            btnReg.setOnClickListener {
                val action = BienvenidoFragmentDirections.actionBienvenidoFragmentToRegistrationFragment()
                findNavController().navigate(action)
            }
        }
    }
}