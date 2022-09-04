package com.cafeyvinowinebar.cafe_y_vino_client.ui.carta.item_specs

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.cafeyvinowinebar.cafe_y_vino_client.databinding.ActivityItemSpecsBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ItemSpecsActivity : AppCompatActivity() {

    // what's a better way to provide the configuration changes safety? to store the args in the savedInstanceState, or in the viewModel?
    // i'll start with setting the data onto the uiState via viewModel, and maybe test the traditional way later, if it doesn't work.

    private val args: ItemSpecsActivityArgs by navArgs()
    private val viewModel: ItemSpecsViewModel by viewModels()
    private lateinit var binding: ActivityItemSpecsBinding

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        binding = ActivityItemSpecsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        viewModel.setArgsOnUiState(args.bundleWithItemsCollection, args.nameOfInitialItem)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    binding.root.apply {
                        adapter = Adapter()
                        currentItem = it.initialPosition
                        setPageTransformer(ZoomOutPageTransformer())
                    }

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