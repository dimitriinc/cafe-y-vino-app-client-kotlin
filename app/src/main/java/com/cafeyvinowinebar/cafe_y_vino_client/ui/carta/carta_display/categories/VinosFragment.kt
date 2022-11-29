package com.cafeyvinowinebar.cafe_y_vino_client.ui.carta.carta_display.categories

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.cafeyvinowinebar.cafe_y_vino_client.databinding.FragmentVinosBinding

/**
 * Displays text views of different wine subcategories
 * On click we navigate to the items fragment with the proper category path as an argument
 */
class VinosFragment : Fragment(R.layout.fragment_vinos) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentVinosBinding.bind(view)
        binding.apply {

            txtTintos.setOnClickListener {
                navigateToItems("menu/03.vinos/vinos/Vinos tintos/vinos")
            }
            txtBlancos.setOnClickListener {
                navigateToItems("menu/03.vinos/vinos/Vinos blancos/vinos")
            }
            txtRose.setOnClickListener {
                navigateToItems("menu/03.vinos/vinos/Vinos rose/vinos")
            }
            txtPostre.setOnClickListener {
                navigateToItems("menu/03.vinos/vinos/Vinos de postre/vinos")
            }
            txtCopa.setOnClickListener {
                navigateToItems("menu/03.vinos/vinos/Vinos por copa/vinos")
            }
        }
    }

    private fun navigateToItems(catPath: String) {
        val action = VinosFragmentDirections.actionVinosFragmentToMenuItemsNavGraph(catPath)
        findNavController().navigate(action)
    }

}