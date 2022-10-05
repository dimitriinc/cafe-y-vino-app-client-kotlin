package com.cafeyvinowinebar.cafe_y_vino_client.ui.main

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
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.fragment.findNavController
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.cafeyvinowinebar.cafe_y_vino_client.databinding.FragmentMainBinding
import com.cafeyvinowinebar.cafe_y_vino_client.isOnline
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Entry point to the app
 * Directs to the nested graphs
 * Sends entry requests
 */

@AndroidEntryPoint
class MainFragment : Fragment(R.layout.fragment_main) {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var entryRequestDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // check if the user is logged in
        // if not, direct to the log in flow
        if (!viewModel.uiState.value.isLoggedIn) {
            findNavController().navigate(R.id.intro_nav_graph)
            // TODO: you have to pop up the main graph somehow
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentMainBinding.bind(view)

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

                    // the initial value of the UI state property is null
                    // when the user tries to send the request, the view model decides if they're allowed to do it
                    // when the decision is made, it sets the value to true or false
                    // and this block is supposed to react to this action
                    // after the appropriate reaction, the view model is asked to set the value to null again
                    require(it.canUserSendEntryRequest != null) {
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

        binding.apply {

            // simple navigation to destinations
            imgBtnGiftshop.setOnClickListener {
                findNavController().navigate(R.id.giftshopFragment)
            }
            imgBtnMenu.setOnClickListener {
                findNavController().navigate(R.id.carta_nav_graph)
            }
            imgBtnReserv.setOnClickListener {
                findNavController().navigate(R.id.reservas_nav_graph)
            }
            fabUserData.setOnClickListener {
                findNavController().navigate(R.id.userDataFragment)
            }

            // deep links to the destinations from the carta nav graph, that are not start destinations (canasta and cuenta)
            fabCanastaMain.setOnClickListener {
                val request = NavDeepLinkRequest.Builder.fromAction("ACTION_CANASTA").build()
                findNavController().navigate(request)
            }
            fabCuentaMain.setOnClickListener {
                val request = NavDeepLinkRequest.Builder.fromAction("ACTION_CUENTA").build()
                findNavController().navigate(request)
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

                // if the user wants to send a request, we don't do it right away
                // we need to make sure the time is right (inside the attendance hours)
                // so we tell the view model to set the request status
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
    }
}