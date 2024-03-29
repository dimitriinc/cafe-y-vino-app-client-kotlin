package com.cafeyvinowinebar.cafe_y_vino_client.ui.introduction

import android.content.Context
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.cafeyvinowinebar.cafe_y_vino_client.MainNavGraphDirections
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.cafeyvinowinebar.cafe_y_vino_client.databinding.FragmentRegistrationBinding
import com.cafeyvinowinebar.cafe_y_vino_client.isOnline
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * The user fills in the edit texts with their data needed for registration
 */
@AndroidEntryPoint
class RegistrationFragment : Fragment(R.layout.fragment_registration) {

    private val viewModel: IntroductionViewModel by hiltNavGraphViewModels(R.id.intro_nav_graph)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentRegistrationBinding.bind(view)

        binding.apply {

            // toggles the visibility of the content of the password edit texts
            checkBoxPassword.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    edtPass.transformationMethod = HideReturnsTransformationMethod.getInstance()
                } else {
                    edtPass.transformationMethod = PasswordTransformationMethod.getInstance()
                }
            }

            btnRegistration.setOnClickListener {
                if (isOnline(requireContext())) {

                    // collect the values from the edit texts
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
                        return@setOnClickListener
                    }

                    // hide the keyboard
                    val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(requireView().windowToken, 0)

                    // store the data to the UI state
                    viewModel.storeFormData(email, password, name, phone, birthdate)

                    // all values are good, we are ready to register the user
                    // but first we need to explicitly ask him for permission to collect his personal data
                    val action = RegistrationFragmentDirections.actionRegistrationFragmentToPrivacyDialog()
                    findNavController().navigate(action)

                } else {
                    Toast.makeText(requireContext(), R.string.no_connection, Toast.LENGTH_LONG).show()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {

                    if (it.privacyPolicyVisited) {
                        val action = RegistrationFragmentDirections.actionRegistrationFragmentToPrivacyDialog()
                        findNavController().navigate(action)
                        viewModel.setPrivacyFlag(false)
                    }

                    // if the isRegistered property of the UI state is true, that means the registration has terminated successfully
                    // so we start the session and navigate to the main screen
                    if (it.isRegistered) {
                        val action = MainNavGraphDirections.actionGlobalHomeFragment()
                        findNavController().navigate(action)
                    }

                    if (it.progressBarVisible) {
                        binding.progressBar.visibility = View.VISIBLE
                    } else {
                        binding.progressBar.visibility = View.INVISIBLE
                    }

                    // when the registration operation fails we get an error message and display it as a toast
                    if (it.errorMessageId != null) {
                        viewModel.setProgressBarVisibility(false)
                        Toast.makeText(requireContext(), it.errorMessageId, Toast.LENGTH_LONG).show()
                        viewModel.nullifyErrorMessage()
                    }
                }
            }
        }


    }
}