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
import androidx.navigation.fragment.findNavController
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.cafeyvinowinebar.cafe_y_vino_client.databinding.FragmentUserDataBinding

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
            }
        }
    }
}