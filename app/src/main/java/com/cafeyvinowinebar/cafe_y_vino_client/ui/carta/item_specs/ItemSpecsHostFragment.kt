package com.cafeyvinowinebar.cafe_y_vino_client.ui.carta.item_specs

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
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
//        var initialPosition = 0
//        viewModel.uiState.value.items!!.forEach {
//            if (args.position == it.nombre) {
//                initialPosition = viewModel.uiState.value.items!!.indexOf(it)
//            }
//        }
        binding.root.apply {
            Log.d(TAG, "onViewCreated: INITIAL POSITION: ${args.position}")
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