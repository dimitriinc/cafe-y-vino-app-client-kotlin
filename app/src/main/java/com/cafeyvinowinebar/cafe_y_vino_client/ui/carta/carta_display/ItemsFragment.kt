package com.cafeyvinowinebar.cafe_y_vino_client.ui.carta.carta_display

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.View.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.cafeyvinowinebar.cafe_y_vino_client.*
import com.cafeyvinowinebar.cafe_y_vino_client.data.data_models.ItemMenuFirestore
import com.cafeyvinowinebar.cafe_y_vino_client.databinding.FragmentItemsBinding
import com.cafeyvinowinebar.cafe_y_vino_client.interfaces.OnItemLongClickListener
import com.cafeyvinowinebar.cafe_y_vino_client.interfaces.OnProductClickListener
import com.cafeyvinowinebar.cafe_y_vino_client.ui.carta.carta_display.adapters.ItemsAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Displays the products of a category that have a positive presence status in form of a recycler view
 */
@AndroidEntryPoint
class ItemsFragment : Fragment(R.layout.fragment_items),
    OnProductClickListener,
    OnItemLongClickListener {

    lateinit var adapter: ItemsAdapter

    @Inject
    lateinit var fStore: FirebaseFirestore
    private val args: ItemsFragmentArgs by navArgs()
    private val viewModel: CartaDisplayViewModel by navGraphViewModels(R.id.carta_nav_graph)

    @SuppressLint("InflateParams")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentItemsBinding.bind(view)

        // build up the adapter
        val query = fStore.collection(args.categoryPath)
            .whereEqualTo(KEY_IS_PRESENT, true)
            .orderBy(KEY_NOMBRE)
        val options = FirestoreRecyclerOptions.Builder<ItemMenuFirestore>()
            .setQuery(query, ItemMenuFirestore::class.java)
            .build()
        adapter = ItemsAdapter(options, this, this)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {

                    // on the user's presence status depends what fabs we display
                    // if not present, it's just a home button
                    // if present, we have a fab, that expands to display some other fabs
                    if (it.isPresent) {
                        binding.apply {
                            fabItemsHome.visibility = GONE
                            fabItemsParent.visibility = VISIBLE
                        }
                    } else {
                        binding.apply {
                            fabItemsHome.visibility = VISIBLE
                            fabItemsParent.visibility = GONE
                        }
                        viewModel.collapseFabs()
                    }

                    // if the fabs are expanded is controlled by a UI state property
                    // the parent fab takes one different images depending on the value, and different functions to perform on click
                    if (it.areFabsExpanded) {
                        binding.apply {
                            fabItemsParent.apply {
                                setImageResource(R.drawable.ic_collapse)
                                setOnClickListener {
                                    viewModel.collapseFabs()
                                }
                            }
                            fabItemsCanasta.show()
                            fabItemsInfo.show()
                            txtItemsCanasta.visibility = VISIBLE
                            txtItemsInfo.visibility = VISIBLE

                        }
                    } else {
                        binding.apply {
                            fabItemsParent.apply {
                                setImageResource(R.drawable.ic_expand)
                                setOnClickListener {
                                    viewModel.expandFabs()
                                }
                            }
                            fabItemsCanasta.hide()
                            fabItemsInfo.hide()
                            txtItemsCanasta.visibility = GONE
                            txtItemsInfo.visibility = GONE
                        }
                    }
                }
            }
        }

        binding.apply {
            recItems.apply {
                // configure the recycler view
                adapter = adapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }

            // display a dialog that explains the functionality of the clicks
            fabItemsInfo.setOnClickListener {
                val dialogView = layoutInflater.inflate(R.layout.alert_info, null)
                val txtMsg = dialogView.findViewById<TextView>(R.id.txtInfoMsg)
                txtMsg.text = getString(R.string.message_gc_info)
                AlertDialog.Builder(requireContext())
                    .setView(dialogView)
                    .create()
                    .show()
                viewModel.collapseFabs()
            }
            fabItemsCanasta.setOnClickListener {
                val action = ItemsFragmentDirections.actionItemsFragmentToWhenPresentNavGraph()
                findNavController().navigate(action)
            }
            fabItemsHome.setOnClickListener {
                val action = MainNavGraphDirections.actionGlobalMainFragment()
                findNavController().navigate(action)

            }
        }
    }

    /**
     * Add one instance of the product to the Canasta Room table
     */
    override fun onLongClick(document: DocumentSnapshot) {
        viewModel.addProductToCanasta(document)
        Toast.makeText(requireContext(), R.string.on_adding_item, Toast.LENGTH_SHORT).show()
    }

    /**
     * On click the user navigates to the Item Specs
     * Which is a ViewPager, that displays all the available items of the category, starting with the one that has been pressed
     * So as arguments to the new activity, we pass the list of all the items, represented as instances of the ItemMenu class
     */
    override fun onClick(document: DocumentSnapshot, items: ArrayList<ItemMenuFirestore>) {
        val bundle = Bundle()
        bundle.putSerializable(KEY_ITEMS, items)
        bundle.putString(KEY_NOMBRE, document.getString(KEY_NOMBRE))
        val action = ItemSpecsNavGraphDirections.actionGlobalItemSpecsHostFragment(
            bundle,
            document.getString(KEY_NOMBRE)!!
        )
        findNavController().navigate(action)
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
        viewModel.collapseFabs()
    }


}