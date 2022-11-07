package com.cafeyvinowinebar.cafe_y_vino_client.ui.reservas

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavArgument
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.cafeyvinowinebar.cafe_y_vino_client.databinding.ActivityReservasBinding
import kotlinx.coroutines.launch

class ReservasActivity : AppCompatActivity() {

    private val viewModel: ReservasViewModel by viewModels()
    private lateinit var binding: ActivityReservasBinding

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

        binding = ActivityReservasBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val host = supportFragmentManager.findFragmentById(R.id.reservas_host_fragment) as NavHostFragment? ?: return
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