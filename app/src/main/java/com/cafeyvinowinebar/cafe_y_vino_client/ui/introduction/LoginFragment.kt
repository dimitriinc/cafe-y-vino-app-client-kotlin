package com.cafeyvinowinebar.cafe_y_vino_client.ui.introduction

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.cafeyvinowinebar.cafe_y_vino_client.databinding.FragmentLoginBinding
import com.cafeyvinowinebar.cafe_y_vino_client.isOnline
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {

    private val viewModel: IntroductionViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentLoginBinding.bind(view)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {

                    // if the log in operation was a success we display a greeting toast and navigate to the main screen
                    if (it.isLoggedIn) {
                        binding.progressBarLogin.visibility = View.INVISIBLE
                        Toast.makeText(requireContext(), R.string.login_inicio, Toast.LENGTH_SHORT).show()
                        val action = LoginFragmentDirections.actionLoginFragmentToMainNavGraph()
                        findNavController().navigate(action)
                    }

                    // if some of the operations fails we get an error message and display it as a toast
                    if (it.errorMessage != null) {
                        Toast.makeText(requireContext(), it.errorMessage, Toast.LENGTH_LONG).show()
                    }

                    // specially for when an email with the password reset form is sent to the user's address
                    // we notify them with a message
                    if (it.message != null) {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.apply {

            // toggle the password visibility
            checkBoxPassword.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    edtPassLogin.transformationMethod =
                        HideReturnsTransformationMethod.getInstance()
                } else {
                    edtPassLogin.transformationMethod = PasswordTransformationMethod.getInstance()
                }
            }

            // build a dialog to ask for an email where to send a form for a password resetting
            // gather the email and pass it to the view model
            btnRestablecer.setOnClickListener {
                val restablecerView = layoutInflater.inflate(R.layout.user_data_et, null)
                val edtEmail = restablecerView.findViewById<EditText>(R.id.edtUserEt)
                edtEmail.hint = getString(R.string.su_email)
                AlertDialog.Builder(requireContext())
                    .setView(restablecerView)
                    .setTitle(R.string.dialog_reset_email_title)
                    .setMessage(R.string.no_email)
                    .setPositiveButton(R.string.send_email) { _, _ ->
                        if (isOnline(requireContext())) {
                            val email = edtEmail.text.toString().trim()
                            if (email.isEmpty()) {
                                edtEmail.error = getString(R.string.no_email)
                                return@setPositiveButton
                            }
                            viewModel.resetPassword(email)
                        } else {
                            Toast.makeText(
                                requireContext(),
                                R.string.no_connection,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    .create()
                    .show()

            }

            // get the input values and pass them to the view model to start the logging in
            btnLogin.setOnClickListener {
                if (isOnline(requireContext())) {
                    val email = edtEmailLogin.text.toString().trim()
                    val password = edtPassLogin.text.toString().trim()

                    if (email.isEmpty()) {
                        edtEmailLogin.error = getString(R.string.error_email)
                        return@setOnClickListener
                    }
                    if (password.length < 6) {
                        edtPassLogin.error = getString(R.string.error_password_length)
                        return@setOnClickListener
                    }
                    if (password.isEmpty()) {
                        edtPassLogin.error = getString(R.string.error_password)
                        return@setOnClickListener
                    }

                    progressBarLogin.visibility = View.VISIBLE
                    viewModel.loginUser(email, password)
                } else {
                    Toast.makeText(requireContext(), R.string.no_connection, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }
}