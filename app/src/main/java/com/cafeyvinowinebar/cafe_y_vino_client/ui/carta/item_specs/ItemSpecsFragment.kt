package com.cafeyvinowinebar.cafe_y_vino_client.ui.carta.item_specs

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.cafeyvinowinebar.cafe_y_vino_client.databinding.FragmentItemSpecsBinding
import com.cafeyvinowinebar.cafe_y_vino_client.ui.carta.carta_display.items.ItemsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

private const val TAG = "ItemSpecsFragment"
@AndroidEntryPoint
class ItemSpecsFragment : Fragment(R.layout.fragment_item_specs) {

    private val viewModel: ItemsViewModel by hiltNavGraphViewModels(R.id.menu_items_nav_graph)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "onViewCreated: VIEW_CREATED")
        val binding = FragmentItemSpecsBinding.bind(view)

        binding.apply {
            fabItemSpecsHome.setOnClickListener {
                findNavController().navigate(R.id.homeFragment)
            }
            fabMainMenu.setOnClickListener {
                findNavController().navigate(R.id.categoriesFragment)
            }
            fabCanasta.setOnClickListener {
                findNavController().navigate(R.id.canastaFragment)
            }

            /**
             * Add the current item on display to the Canasta Room table
             */
            fabAdd.setOnClickListener {
                viewModel.addProductToCanasta(viewModel.uiState.value.currentItem!!)
                Toast.makeText(requireContext(), R.string.on_adding_item, Toast.LENGTH_SHORT).show()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->

                    /**
                     * Configure the views of the fragment
                     */
                    binding.apply {
                        // an ItemMenu instance should be stored in the UI state at this point
                        // we get the data from its properties to populate the layout
                        val item = uiState.currentItem
                        require(item != null)

                        // some new items may not have image in the Storage yet; in this case we give them a stand in image
                        if (item.image != null) {
                            Glide.with(requireContext())
                                .load(uiState.itemImgReference)
                                .into(imgItem)
                        } else {
                            imgItem.setImageResource(R.drawable.logo_stand_in)
                        }

                        // some new items also may not have a description; in this case we give them a default description
                        if (item.descripcion == null || item.descripcion.isEmpty()) {
                            txtDesc.text = getString(R.string.no_desc)
                        } else {
                            txtDesc.text = item.descripcion
                        }

                        txtItemName.text = item.nombre
                        txtPrecioInt.text = item.precio
                        txtPrecio.visibility = VISIBLE

                        // the first item in the collection shouldn't display the left arrow; the last one - the right arrow
                        if (uiState.currentPosition == 0) {
                            arrowLeft.visibility = GONE
                        }
                        if (uiState.currentPosition == uiState.setSize?.minus(1)) {
                            arrowRight.visibility = GONE
                        }


                        // the fabs visibility depends on a value stored in the UI state
                        if (uiState.isPresent) {
                            binding.apply {
                                fabItemSpecsHome.visibility = GONE
                                fabExpand.visibility = VISIBLE
                            }
                        } else {
                            binding.apply {
                                fabItemSpecsHome.visibility = VISIBLE
                                fabExpand.visibility = GONE
                                viewModel.setSpecsFabs(false)
                            }
                        }

                        if (uiState.areSpecsFabsExpanded) {
                            binding.apply {
                                fabExpand.apply {
                                    setImageResource(R.drawable.ic_collapse)
                                    setOnClickListener {
                                        viewModel.setSpecsFabs(false)
                                    }
                                }
                                fabAdd.show()
                                fabCanasta.show()
                                fabMainMenu.show()
                                txtAgregar.visibility = VISIBLE
                                txtMenu.visibility = VISIBLE
                                txtCanasta.visibility = VISIBLE
                            }
                        } else {
                            binding.apply {
                                fabExpand.apply {
                                    setImageResource(R.drawable.ic_expand)
                                    setOnClickListener {
                                        viewModel.setSpecsFabs(true)
                                    }
                                }
                                fabAdd.hide()
                                fabCanasta.hide()
                                fabMainMenu.hide()
                                txtAgregar.visibility = GONE
                                txtMenu.visibility = GONE
                                txtCanasta.visibility = GONE
                            }
                        }

                    }
                }
            }
        }
    }
}