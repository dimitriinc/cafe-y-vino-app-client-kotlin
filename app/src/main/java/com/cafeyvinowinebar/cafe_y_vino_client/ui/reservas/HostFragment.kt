package com.cafeyvinowinebar.cafe_y_vino_client.ui.reservas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.cafeyvinowinebar.cafe_y_vino_client.databinding.FragmentReservasHostBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HostFragment : Fragment() {

    private var _binding: FragmentReservasHostBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReservasViewModel by viewModels()
    private lateinit var fManager: FragmentManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReservasHostBinding.inflate(inflater, container, false)

        fManager = childFragmentManager

        replaceFragment(FechaFragment())

        binding.reservasBottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.reservas_fecha_dest -> replaceFragment(FechaFragment())
                R.id.reservas_mesa_dest -> replaceFragment(MesaFragment())
                R.id.reservas_fin_dest -> replaceFragment(FinFragment())
            }
            true
        }

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentTransaction = fManager.beginTransaction()
        fragmentTransaction.replace(R.id.reservas_host_fragment, fragment)
        fragmentTransaction.commit()
    }
}