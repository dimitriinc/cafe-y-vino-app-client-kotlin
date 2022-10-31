package com.cafeyvinowinebar.cafe_y_vino_client.ui.reservas

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavArgument
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.cafeyvinowinebar.cafe_y_vino_client.databinding.FragmentReservasBinding
import kotlinx.coroutines.launch

class ReservasFragment : Fragment(R.layout.fragment_reservas) {

    private val viewModel: ReservasViewModel by hiltNavGraphViewModels(R.id.reservas_nav_graph)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentReservasBinding.bind(view)
        val host = parentFragmentManager.findFragmentById(R.id.reservas_host_fragment) as NavHostFragment? ?: return
        val navController = host.navController
        binding.reservasBottomNavigation.setupWithNavController(navController)

        lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->

                // here we define the access to the mesa and fin fragments
                // for the mesa fragment to function the fecha and part values must be chosen already
                // the fin fragment needs fecha, part, and mesa values; though since the mesa value won't be set without fecha and part,
                // we just ask for the mesa value
                if (uiState.fecha != null && uiState.part != null) {
                    viewModel.setPassToMesas(true)
                } else {
                    viewModel.setPassToMesas(false)
                }

                if (uiState.mesa != null) {
                    viewModel.setPassToFin(true)
                } else {
                    viewModel.setPassToFin(false)
                }

                // based on the values set earlier, we pass the access controls as arguments to the mesa and fin fragments
                navController.addOnDestinationChangedListener { _, destination, _ ->
                    when (destination.id) {

                        R.id.reservas_mesa_dest -> {
                            val passAllowed = uiState.passToMesasAllowed
                            val argument = NavArgument.Builder().setDefaultValue(passAllowed).build()
                            destination.addArgument("passAllowed", argument)
                        }

                        R.id.reservas_fin_dest -> {
                            val passAllowed = uiState.passToFinAllowed
                            val argument = NavArgument.Builder().setDefaultValue(passAllowed).build()
                            destination.addArgument("passAllowed", argument)
                        }
                    }
                }
            }
        }
    }
}