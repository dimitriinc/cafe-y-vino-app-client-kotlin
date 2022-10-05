package com.cafeyvinowinebar.cafe_y_vino_client.ui.main.giftshop

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.cafeyvinowinebar.cafe_y_vino_client.databinding.FragmentGiftshopBinding
import com.cafeyvinowinebar.cafe_y_vino_client.ui.main.MainViewModel
import kotlinx.coroutines.launch

class GiftshopFragment : Fragment(R.layout.fragment_giftshop) {

    private val viewModel: MainViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentGiftshopBinding.bind(view)

        binding.apply {

            // if the user's status is present, they can access the gifts menu
            imgGastar.setOnClickListener {
                if (viewModel.uiState.value.isUserPresent) {
                    GiftshopMenuFragment().show(childFragmentManager, "TAG")
                } else {
                    Toast.makeText(requireContext(), R.string.regalo_not_present_rejection, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {

                    // displays the message on the screen with how much bonus points does the user have
                    binding.apply {
                        txtMensaje.text = getString(R.string.gift_user_msg, it.userName)
                        txtBonos.text = it.bonos.toString()
                    }
                }
            }
        }
    }
}