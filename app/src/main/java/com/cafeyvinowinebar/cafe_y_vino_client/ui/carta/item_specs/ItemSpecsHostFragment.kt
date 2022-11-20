package com.cafeyvinowinebar.cafe_y_vino_client.ui.carta.item_specs

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.cafeyvinowinebar.cafe_y_vino_client.databinding.FragmentItemSpecsHostBinding
import com.cafeyvinowinebar.cafe_y_vino_client.ui.carta.carta_display.items.ItemsFragment
import com.cafeyvinowinebar.cafe_y_vino_client.ui.carta.carta_display.items.ItemsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

private const val TAG = "ItemSpecsHostFragment"
/**
 * Hosts a ViewPager2 that displays a collection of menu items of the chosen category
 */
@AndroidEntryPoint
class ItemSpecsHostFragment : Fragment(R.layout.fragment_item_specs_host) {

    private val viewModel: ItemsViewModel by hiltNavGraphViewModels(R.id.menu_items_nav_graph)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentItemSpecsHostBinding.bind(view)

        Log.d(TAG, "onViewCreated: VIEW_CREATED")

        binding.root.apply {
            adapter = Adapter()
            setPageTransformer(ZoomOutPageTransformer())
            currentItem = viewModel.uiState.value.initialPosition
        }
    }

    inner class Adapter : FragmentStateAdapter(this) {

        override fun getItemCount(): Int {
            Log.d(TAG, "getItemCount: GET_ITEM_COUNT: ${viewModel.uiState.value.items!!.size}")
            return viewModel.uiState.value.items!!.size
        }

        override fun createFragment(position: Int): Fragment {
            viewModel.setThePagingFragment(position)
            Log.d(TAG, "createFragment: CREATE_FRAGMENT")
            return ItemSpecsFragment()
        }

    }
}