package com.cafeyvinowinebar.cafe_y_vino_client.ui.main

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.cafeyvinowinebar.cafe_y_vino_client.databinding.FragmentUserDataBinding
import kotlinx.coroutines.launch

class UserDataFragment : Fragment(R.layout.fragment_user_data) {

    private val viewModel: MainViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentUserDataBinding.bind(view)

        binding.apply {
            btnLogOut.setOnClickListener {
                viewModel.logout()
                findNavController().popBackStack(R.id.intro_nav_graph, false)
            }
            btnEmail.setOnClickListener {
                val emailView = layoutInflater.inflate(R.layout.user_data_et, null)
                val editText = emailView.findViewById<EditText>(R.id.edtUserEt)
                editText.hint = getString(R.string.new_email)
                editText.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                AlertDialog.Builder(requireContext())
                    .setView(emailView)
                    .setPositiveButton(R.string.cambiar) { _, _ ->
                        val email = editText.text.toString().trim()
                        if (email.isEmpty()) {
                            Toast.makeText(requireContext(), R.string.user_empty_field_email, Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.updateEmail(email)
                        }
                    }
                    .create().show()
            }
            btnNombre.setOnClickListener {
                val nombreView = layoutInflater.inflate(R.layout.user_data_et, null)
                val editText = nombreView.findViewById<EditText>(R.id.edtUserEt)
                editText.hint = getString(R.string.new_name)
                editText.inputType = InputType.TYPE_CLASS_TEXT
                AlertDialog.Builder(requireContext())
                    .setView(nombreView)
                    .setPositiveButton(R.string.cambiar) { _, _ ->
                        val nombre = editText.text.toString().trim()
                        if (nombre.isEmpty()) {
                            Toast.makeText(requireContext(), R.string.user_empty_field_name, Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.updateNombre(nombre)
                        }
                    }
                    .create().show()

            }
            btnTelefono.setOnClickListener {
                val telefonoView = layoutInflater.inflate(R.layout.user_data_et, null)
                val editText = telefonoView.findViewById<EditText>(R.id.edtUserEt)
                editText.hint = getString(R.string.new_phone)
                editText.inputType = InputType.TYPE_CLASS_PHONE
                AlertDialog.Builder(requireContext())
                    .setView(telefonoView)
                    .setPositiveButton(R.string.cambiar) { _,_ ->
                        val telefono = editText.text.toString().trim()
                        if (telefono.isEmpty()) {
                            Toast.makeText(requireContext(), R.string.user_empty_field_phone, Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.updateTelefono(telefono)
                        }
                    }
                    .create().show()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    if (it.message != null) {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                        viewModel.nullifyMessage()
                    }
                }
            }
        }
    }
}