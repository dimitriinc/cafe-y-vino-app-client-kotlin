package com.cafeyvinowinebar.cafe_y_vino_client.ui.main.apology

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.cafeyvinowinebar.cafe_y_vino_client.MainNavGraphDirections
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.cafeyvinowinebar.cafe_y_vino_client.databinding.FragmentApologyBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ApologyFragment : Fragment(R.layout.fragment_apology) {

    private val viewModel: ApologyViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentApologyBinding.bind(view)

        binding.apply {
            fabApologyOk.setOnClickListener {
                val nombre = edtApologyNombre.text.toString().trim()
                val telefono = edtApologyTelefono.text.toString().trim()

                if (nombre.isEmpty()) {
                    edtApologyNombre.error = getString(R.string.error_nombre)
                    return@setOnClickListener
                }
                if (telefono.isEmpty()) {
                    edtApologyTelefono.error = getString(R.string.error_telefono)
                    return@setOnClickListener
                }

                viewModel.registerUser(nombre, telefono)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    if (it.isRegistered) {
                        val action = MainNavGraphDirections.actionGlobalHomeFragment()
                        findNavController().navigate(action)
                    }
                }
            }
        }
    }

}