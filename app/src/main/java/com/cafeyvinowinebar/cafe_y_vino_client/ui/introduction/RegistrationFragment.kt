package com.cafeyvinowinebar.cafe_y_vino_client.ui.introduction

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.cafeyvinowinebar.cafe_y_vino_client.databinding.AlertPrivacyBinding
import com.cafeyvinowinebar.cafe_y_vino_client.databinding.FragmentRegistrationBinding
import com.cafeyvinowinebar.cafe_y_vino_client.isOnline
import kotlinx.android.synthetic.main.fragment_bienvenido.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class RegistrationFragment : Fragment(R.layout.fragment_registration) {

    private val viewModel: IntroductionViewModel by viewModels()

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

                    // collect the values from edit texts
                    val name = edtName.text.toString().trim()
                    val phone = edtPhone.text.toString().trim()
                    val email = edtEmail.text.toString().trim()
                    val password = edtPass.text.toString().trim()
                    val birthdate = edtFechaNacimiento.text.toString().trim()

                    // check if all the values necessary are provided by the user
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
                    if (birthdate.isEmpty()) {
                        edtFechaNacimiento.error = getString(R.string.error_fecha_nacimiento)
                    }

                    // all values are good, we are ready to authenticate the user
                    // but first we need to ask him a permission to collect his personal data
                    val privacyAlertView = layoutInflater.inflate(R.layout.alert_privacy, null)
                    val privacyAlertBinding = AlertPrivacyBinding.bind(privacyAlertView)
                    val privacyAlertDialog = AlertDialog.Builder(requireContext())
                        .setView(privacyAlertView)
                        .create()
                    privacyAlertBinding.apply {
                        btnPrivacySaber.setOnClickListener {
                            // go to a information page with a condensed FAQ of our privacy policy
                            findNavController().navigate(R.id.action_registrationFragment_to_privacyFragment)
                        }
                        btnPrivacyRechazar.setOnClickListener {
                            privacyAlertDialog.dismiss()
                        }
                        btnPrivacyPermitir.setOnClickListener {
                            // he permission granted we can start the business logic, and take care of some views while it's processing
                            privacyAlertDialog.dismiss()
                            progressBar.visibility = View.VISIBLE

                            viewModel.authenticateUser(
                                email,
                                password,
                                name,
                                phone,
                                birthdate
                            )
                        }
                    }
                    privacyAlertDialog.show()


                } else {
                    Toast.makeText(requireContext(), R.string.no_connection, Toast.LENGTH_LONG).show()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    if (it.isRegistered) {
                        binding.progressBar.visibility = View.INVISIBLE
                        // TODO: navigate to the main fragment
                    }
                    if (it.errorMessage != null) {
                        binding.progressBar.visibility = View.INVISIBLE
                        Toast.makeText(requireContext(), it.errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }


    }
}