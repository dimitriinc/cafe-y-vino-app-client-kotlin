package com.cafeyvinowinebar.cafe_y_vino_client.ui.carta.item_specs

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.cafeyvinowinebar.cafe_y_vino_client.KEY_ITEMS
import com.cafeyvinowinebar.cafe_y_vino_client.KEY_NOMBRE
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.cafeyvinowinebar.cafe_y_vino_client.data.data_models.ItemMenuFirestore
import com.cafeyvinowinebar.cafe_y_vino_client.databinding.FragmentItemSpecsHostBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Hosts a ViewPager2 that displays a collection of menu items of the chosen category
 */
@AndroidEntryPoint
class ItemSpecsHostFragment : Fragment(R.layout.fragment_item_specs_host) {
    

    // what's a better way to provide the configuration changes safety? to store the args in the savedInstanceState, or in the viewModel?
    // i'll start with setting the data onto the uiState via viewModel, and maybe test the traditional way later, if it doesn't work.

    private val viewModel: ItemSpecsViewModel by hiltNavGraphViewModels(R.id.item_specs_nav_graph)

    init {
        val items = arguments?.getSerializable(KEY_ITEMS) as ArrayList<ItemMenuFirestore>
        val initialItemName = arguments?.getString(KEY_NOMBRE)!!
        // pass the arguments to the viewModel, so it stores the necessary values into the UI state
        viewModel.setArgsOnUiState(items, initialItemName)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentItemSpecsHostBinding.bind(view)

        // configure the ViewPager
        binding.root.apply {
            adapter = Adapter()
            setPageTransformer(ZoomOutPageTransformer())
            currentItem = viewModel.uiState.value.initialPosition
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    // it might happen that the initial position in the UI state is set after the onCreate call (?)
                    binding.root.currentItem = it.initialPosition
                }
            }
        }
    }

    inner class Adapter : FragmentStateAdapter(this) {

        override fun getItemCount(): Int {
            return viewModel.uiState.value.items!!.size
        }

        override fun createFragment(position: Int): Fragment {
            viewModel.setThePagingFragment(position)
            return ItemSpecsFragment()
        }

    }
}