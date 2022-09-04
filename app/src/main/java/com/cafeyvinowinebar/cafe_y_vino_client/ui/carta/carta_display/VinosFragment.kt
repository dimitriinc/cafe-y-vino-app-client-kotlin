package com.cafeyvinowinebar.cafe_y_vino_client.ui.carta.carta_display

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.cafeyvinowinebar.cafe_y_vino_client.databinding.FragmentVinosBinding

class VinosFragment : Fragment(R.layout.fragment_vinos) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentVinosBinding.bind(view)
        binding.apply {
            txtTintos.setOnClickListener {
                val action = VinosFragmentDirections.actionVinosFragmentToItemsFragment(
                    "menu/03.vinos/vinos/Vinos tintos/vinos"
                )
                findNavController().navigate(action)
            }
            txtBlancos.setOnClickListener {
                val action = VinosFragmentDirections.actionVinosFragmentToItemsFragment(
                    "menu/03.vinos/vinos/Vinos blancos/vinos"
                )
                findNavController().navigate(action)
            }
            txtRose.setOnClickListener {
                val action = VinosFragmentDirections.actionVinosFragmentToItemsFragment(
                    "menu/03.vinos/vinos/Vinos rose/vinos"
                )
                findNavController().navigate(action)
            }
            txtPostre.setOnClickListener {
                val action = VinosFragmentDirections.actionVinosFragmentToItemsFragment(
                    "menu/03.vinos/vinos/Vinos de postre/vinos"
                )
                findNavController().navigate(action)
            }
            txtCopa.setOnClickListener {
                val action = VinosFragmentDirections.actionVinosFragmentToItemsFragment(
                    "menu/03.vinos/vinos/Vinos por copa/vinos"
                )
                findNavController().navigate(action)
            }
        }
    }

}