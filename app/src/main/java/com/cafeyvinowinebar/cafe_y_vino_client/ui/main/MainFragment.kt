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
import androidx.fragment.app.findFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.cafeyvinowinebar.cafe_y_vino_client.databinding.FragmentMainBinding
import com.cafeyvinowinebar.cafe_y_vino_client.isOnline
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainFragment : Fragment(R.layout.fragment_main) {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!viewModel.uiState.value.isLoggedIn) {
            findNavController().navigate(R.id.intro_nav_graph)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentMainBinding.bind(view)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
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
                }
            }
        }

        binding.apply {
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
            fabCanastaMain.setOnClickListener {
                findNavController().navigate(R.id.canastaFragment)
            }
            fabCuentaMain.setOnClickListener {
                findNavController().navigate(R.id.cuentaFragment)
            }
            fabPuerta.setOnClickListener {

                val requestView = layoutInflater.inflate(R.layout.alert_entry_request, null)
                val alertMessage = requestView.findViewById<TextView>(R.id.txtRequestMsg)
                val btnPositive = requestView.findViewById<Button>(R.id.btnRequestPositive)
                val btnNegative = requestView.findViewById<Button>(R.id.btnRequestNegative)
                val progressBar = requestView.findViewById<ProgressBar>(R.id.pbEntry)

                val dialog = AlertDialog.Builder(requireContext())
                    .setView(requestView)
                    .create()

                btnPositive.setOnClickListener { 
                    if (!isOnline(requireContext())) {
                        Toast.makeText(requireContext(), R.string.no_connection, Toast.LENGTH_LONG).show()
                        dialog.dismiss()
                    } else {

                    }
                }
                btnNegative.setOnClickListener {
                    dialog.dismiss()
                }
                dialog.show()
            }
        }
    }
}