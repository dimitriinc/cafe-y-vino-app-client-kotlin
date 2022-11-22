package com.cafeyvinowinebar.cafe_y_vino_client.ui.carta.carta_display.items.item_specs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.cafeyvinowinebar.cafe_y_vino_client.databinding.FragmentItemSpecsHostBinding
import com.cafeyvinowinebar.cafe_y_vino_client.ui.carta.carta_display.items.ItemsViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Hosts a ViewPager2 that displays a collection of menu items of the chosen category
 */
@AndroidEntryPoint
class ItemSpecsHostFragment : Fragment() {

    private val viewModel: ItemsViewModel by hiltNavGraphViewModels(R.id.menu_items_nav_graph)
    private val args: ItemSpecsHostFragmentArgs by navArgs()

    private var _binding: FragmentItemSpecsHostBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentItemSpecsHostBinding.inflate(inflater, container, false)
        binding.root.apply {
            adapter = Adapter()
            setPageTransformer(ZoomOutPageTransformer())
            currentItem = args.position
        }
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    inner class Adapter : FragmentStateAdapter(this) {

        override fun getItemCount(): Int {
            return viewModel.uiState.value.items!!.size
        }

        override fun createFragment(position: Int): Fragment {
            return ItemSpecsFragment.newInstance(position)
        }

    }
}