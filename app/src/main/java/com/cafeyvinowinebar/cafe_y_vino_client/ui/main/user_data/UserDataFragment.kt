package com.cafeyvinowinebar.cafe_y_vino_client.ui.main.user_data

import android.content.res.Configuration
import android.os.Bundle
import android.text.InputType
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
import com.cafeyvinowinebar.cafe_y_vino_client.databinding.FragmentUserDataBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Displays the user data
 * Allows the user to modify it
 * And to log out
 */
@AndroidEntryPoint
class UserDataFragment : Fragment(R.layout.fragment_user_data) {

    private val viewModel: UserDataViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentUserDataBinding.bind(view)

        binding.apply {

            // log out of the session, navigate to the intro nav graph
            btnLogOut.setOnClickListener {
                viewModel.logout()
                val action = UserDataFragmentDirections.actionUserDataFragmentToIntroNavGraph2()
                findNavController().navigate(action)

            }

            /** for each pressed button we create an alert dialog, using the same layout resource, and configuring its edit text's hint
             * and input type
             * the implementation of the positive button is slightly different, calling different view model functions
             */
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
                            Toast.makeText(
                                requireContext(),
                                R.string.user_empty_field_email,
                                Toast.LENGTH_SHORT
                            ).show()
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
                            Toast.makeText(
                                requireContext(),
                                R.string.user_empty_field_name,
                                Toast.LENGTH_SHORT
                            ).show()
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
                    .setPositiveButton(R.string.cambiar) { _, _ ->
                        val telefono = editText.text.toString().trim()
                        if (telefono.isEmpty()) {
                            Toast.makeText(
                                requireContext(),
                                R.string.user_empty_field_phone,
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            viewModel.updateTelefono(telefono)
                        }
                    }
                    .create().show()
            }

            // set the max width of the text views with custom user data to 70% of the screen width, portrait orientation only
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                val screenWidth = resources.displayMetrics.widthPixels
                val maxWidthPercentage = (screenWidth * 0.7).toInt()
                txtInfoName.maxWidth = maxWidthPercentage
                txtInfoEmail.maxWidth = maxWidthPercentage
                txtInfoTelefono.maxWidth = maxWidthPercentage
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {

                    binding.apply {
                        txtInfoEmail.text = it.userEmail
                        txtInfoName.text = it.userName
                        txtInfoTelefono.text = it.userTelefono
                    }

                    /** as a response to updating the user data, a message should appear on the screen
                     * without updating the message value is null
                     * and it returns to null right after displaying a message
                     */
                    if (it.messageId != null) {
                        Toast.makeText(requireContext(), it.messageId, Toast.LENGTH_LONG).show()
                        viewModel.nullifyMessage()
                    }
                }
            }
        }
    }
}