package com.cafeyvinowinebar.cafe_y_vino_client.ui.main.home

import android.os.Bundle
import android.view.View
import android.view.View.*
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.cafeyvinowinebar.cafe_y_vino_client.*
import com.cafeyvinowinebar.cafe_y_vino_client.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Entry point to the app
 * Directs to the nested graphs
 * Sends entry requests
 */

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var entryRequestDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // check if the user is logged in; if not, navigate to the login flow
        if (!viewModel.uiState.value.isLoggedIn) {
            val action = HomeFragmentDirections.actionHomeFragmentToIntroNavGraph()
            findNavController().navigate(action)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentHomeBinding.bind(view)

        binding.apply {

            imgBtnGiftshop.setOnClickListener {
                val action = HomeFragmentDirections.actionHomeFragmentToGiftshopFragment()
                findNavController().navigate(action)
            }
            imgBtnMenu.setOnClickListener {
                val action = HomeFragmentDirections.actionHomeFragmentToCategoriesFragment()
                findNavController().navigate(action)
            }
            imgBtnReserv.setOnClickListener {
                val action = HomeFragmentDirections.actionHomeFragmentToReservasActivity()
                findNavController().navigate(action)
            }
            fabUserData.setOnClickListener {
                val action = HomeFragmentDirections.actionHomeFragmentToUserDataFragment()
                findNavController().navigate(action)
            }

            fabCanastaMain.setOnClickListener {
                val action = HomeFragmentDirections.actionHomeFragmentToCanastaFragment()
                findNavController().navigate(action)
            }
            fabCuentaMain.setOnClickListener {
                val action = HomeFragmentDirections.actionHomeFragmentToCuentaFragment()
                findNavController().navigate(action)
            }

            // before sending an entry request we show a dialog that explains the function of the button
            fabPuerta.setOnClickListener {

                val requestView = layoutInflater.inflate(R.layout.alert_entry_request, null)
                val alertMessage = requestView.findViewById<TextView>(R.id.txtRequestMsg)
                val btnPositive = requestView.findViewById<Button>(R.id.btnRequestPositive)
                val btnNegative = requestView.findViewById<Button>(R.id.btnRequestNegative)
                val progressBar = requestView.findViewById<ProgressBar>(R.id.pbEntry)

                entryRequestDialog = AlertDialog.Builder(requireContext())
                    .setView(requestView)
                    .create()

                /**
                 * if the user wants to send a request, we don't do it right away
                 * we need to make sure the time is right (inside the attendance hours)
                 * so we tell the view model to set the request status
                 */
                btnPositive.setOnClickListener {
                    if (!isOnline(requireContext())) {
                        Toast.makeText(requireContext(), R.string.no_connection, Toast.LENGTH_LONG)
                            .show()
                        entryRequestDialog.dismiss()
                    } else {
                        alertMessage.visibility = INVISIBLE
                        btnNegative.visibility = INVISIBLE
                        btnPositive.visibility = INVISIBLE
                        progressBar.visibility = VISIBLE
                        viewModel.setEntryRequestStatus()
                    }
                }
                btnNegative.setOnClickListener {
                    entryRequestDialog.dismiss()
                }
                entryRequestDialog.show()
            }
        }


        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {

                    // what fabs to display depends on the presence status of the user
                    if (!it.isUserPresent) {
                        binding.apply {
                            fabCanastaMain.visibility = GONE
                            fabCuentaMain.visibility = GONE
                            fabPuerta.visibility = VISIBLE
                            fabUserData.visibility = VISIBLE
                        }
                    } else {
                        binding.apply {
                            fabCanastaMain.visibility = VISIBLE
                            fabCuentaMain.visibility = VISIBLE
                            fabPuerta.visibility = GONE
                            fabUserData.visibility = GONE
                        }
                    }

                    /**
                     * the initial value of the UI state property is null
                     * when the user tries to send the request, the view model decides if they're allowed to do it
                     * when the decision is made, it sets the value to true or false
                     * and this block is supposed to react to this action
                     * after the appropriate reaction, the view model is asked to set the value to null again
                     */
                    if (it.canUserSendEntryRequest != null) {
                        if (it.canUserSendEntryRequest == true) {
                            viewModel.sendEntryRequest()
                            entryRequestDialog.dismiss()
                            Toast.makeText(
                                requireContext(),
                                R.string.main_request_puerta,
                                Toast.LENGTH_SHORT
                            ).show()
                            viewModel.nullifyEntryRequestStatus()
                        } else {
                            entryRequestDialog.dismiss()
                            Toast.makeText(
                                requireContext(),
                                R.string.main_we_closed,
                                Toast.LENGTH_SHORT
                            ).show()
                            viewModel.nullifyEntryRequestStatus()
                        }
                    }
                }
            }
        }

    }
}