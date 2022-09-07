package com.cafeyvinowinebar.cafe_y_vino_client.ui.introduction

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.cafeyvinowinebar.cafe_y_vino_client.databinding.FragmentRegistrationBinding
import com.cafeyvinowinebar.cafe_y_vino_client.isOnline
import kotlinx.android.synthetic.main.fragment_bienvenido.*

class RegistrationFragment : Fragment(R.layout.fragment_registration) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentRegistrationBinding.bind(view)

        binding.apply {
            checkBoxPassword.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    edtPass.transformationMethod = HideReturnsTransformationMethod.getInstance()
                } else {
                    edtPass.transformationMethod = PasswordTransformationMethod.getInstance()
                }
            }
            btnReg.setOnClickListener {
                if (isOnline(requireContext())) {
                    val name = edtName.text.toString().trim()
                    val phone = edtPhone.text.toString().trim()
                    val email = edtEmail.text.toString().trim()
                    val password = edtPass.text.toString().trim()

                    if (name.isEmpty()) {
                        edtName.error = getString(R.string.error_nombre)
                        return@setOnClickListener
                    }
                    if (name == "cliente" || name == "Cliente" || name == "CLIENTE") {
                        edtName.error = getString(R.string.client_name_error)
                        return@setOnClickListener
                    }
                    if (phone.isEmpty()) {
                        edtPhone.error = getString(R.string.error_telefono)
                        return@setOnClickListener
                    }
                    if (email.isEmpty()) {
                        edtEmail.error = getString(R.string.error_email)
                        return@setOnClickListener
                    }
                    if (password.isEmpty()) {
                        edtPass.error = getString(R.string.error_password)
                        return@setOnClickListener
                    }
                    if (password.length < 6) {
                        edtPass.error = getString(R.string.error_password_length)
                        return@setOnClickListener
                    }
                    if (!checkBoxAge?.isChecked!!) {
                        Toast.makeText(requireContext(), R.string.age_restriction, Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    } else {

                    }
                } else {
                    Toast.makeText(requireContext(), R.string.no_connection, Toast.LENGTH_SHORT).show()
                }
            }
        }


    }
}