package com.cafeyvinowinebar.cafe_y_vino_client.ui.carta.carta_display.items.item_specs

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.cafeyvinowinebar.cafe_y_vino_client.MainNavGraphDirections
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.cafeyvinowinebar.cafe_y_vino_client.data.repositories.MenuDataRepository
import com.cafeyvinowinebar.cafe_y_vino_client.databinding.FragmentItemSpecsBinding
import com.cafeyvinowinebar.cafe_y_vino_client.ui.carta.carta_display.items.ItemsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ItemSpecsFragment : Fragment() {

    @Inject
    lateinit var menuDataRepo: MenuDataRepository
    private val viewModel: ItemsViewModel by hiltNavGraphViewModels(R.id.menu_items_nav_graph)

    private var _binding: FragmentItemSpecsBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val INIT_POSITION = "initialPosition"
        fun newInstance(initPosition: Int) = ItemSpecsFragment().apply {
            arguments = bundleOf(
                INIT_POSITION to initPosition
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentItemSpecsBinding.inflate(inflater, container, false)

        val currentPosition = arguments?.getInt(INIT_POSITION)
        val currentItem = viewModel.uiState.value.items?.get(currentPosition!!)
        val setSize = viewModel.uiState.value.items?.size
        require(currentItem != null)

        binding.apply {

            // some new items may not have image in the Storage yet; in this case we give them a stand in image
            if (currentItem.image != null && currentItem.image != "lg.png" && currentItem.image.isNotEmpty()) {
                val imgReference = menuDataRepo.getImgReference(currentItem.image)
                Glide.with(requireContext())
                    .load(imgReference)
                    .into(imgItem)
            } else {
                imgItem.setImageResource(R.drawable.lg)
            }

            // some new items also may not have a description; in this case we give them a default description
            if (currentItem.descripcion == null || currentItem.descripcion.isEmpty()) {
                txtDesc.text = getString(R.string.no_desc)
            } else {
                txtDesc.text = currentItem.descripcion
            }

            txtItemName.text = currentItem.nombre
            txtPrecioInt.text = currentItem.precio
            txtPrecio.visibility = VISIBLE

            // the first item in the collection shouldn't display the left arrow; the last one - the right arrow
            if (currentPosition == 0) {
                arrowLeft.visibility = GONE
            }
            if (currentPosition == setSize?.minus(1)) {
                arrowRight.visibility = GONE
            }

            fabItemSpecsHome.setOnClickListener {
                val action = MainNavGraphDirections.actionGlobalHomeFragmentNoPopup()
                findNavController().navigate(action)
            }
            fabMainMenu.setOnClickListener {
                val action = MainNavGraphDirections.actionGlobalCategoriesFragment()
                findNavController().navigate(action)
            }
            fabCanasta.setOnClickListener {
                val action = MainNavGraphDirections.actionGlobalCanastaFragment()
                findNavController().navigate(action)
            }

            /**
             * Add the current item on display to the Canasta Room table
             */
            fabAdd.setOnClickListener {
                viewModel.addProductToCanasta(currentItem)
                Toast.makeText(requireContext(), R.string.on_adding_item, Toast.LENGTH_SHORT).show()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
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

        return binding.root
    }

    override fun onStop() {
        super.onStop()
        viewModel.setSpecsFabs(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}