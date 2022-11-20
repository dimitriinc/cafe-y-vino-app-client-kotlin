package com.cafeyvinowinebar.cafe_y_vino_client.ui.carta.carta_display.items

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.cafeyvinowinebar.cafe_y_vino_client.*
import com.cafeyvinowinebar.cafe_y_vino_client.data.data_models.ItemMenuFirestore
import com.cafeyvinowinebar.cafe_y_vino_client.data.sources.FirebaseStorageSource
import com.cafeyvinowinebar.cafe_y_vino_client.databinding.FragmentItemsBinding
import com.cafeyvinowinebar.cafe_y_vino_client.interfaces.ItemsSetter
import com.cafeyvinowinebar.cafe_y_vino_client.interfaces.OnItemLongClickListener
import com.cafeyvinowinebar.cafe_y_vino_client.interfaces.OnProductClickListener
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ItemsFragment"

/**
 * Displays the products of a category that have a positive presence status in form of a recycler view
 */
@AndroidEntryPoint
class ItemsFragment : Fragment(),
    OnProductClickListener,
    OnItemLongClickListener,
    ItemsSetter {

    lateinit var adapterItems: ItemsAdapter

    @Inject
    lateinit var fStore: FirebaseFirestore

    @Inject
    lateinit var fStorage: FirebaseStorageSource
    private val viewModel: ItemsViewModel by hiltNavGraphViewModels(R.id.menu_items_nav_graph)
    private val args: ItemsFragmentArgs by navArgs()

    private var _binding: FragmentItemsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        FirebaseFirestore.getInstance().collection(arguments?.getString(KEY_CAT_PATH)!!)
//            .get().addOnSuccessListener {
//                val itemsArray = arrayListOf<ItemMenuFirestore>()
//                it.forEach { docSnapshot ->
//                    itemsArray.add(docSnapshot.toObject(ItemMenuFirestore::class.java))
//                }
//                viewModel.setItems(itemsArray)
//            }
        _binding = FragmentItemsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("InflateParams")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {

                    /**iewModels()
                     * on the user's presence status depends what fabs we display
                     * if not present, it's just a home button
                     * if present, we have a fab, that expands to display some other fabs
                     */
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
                        viewModel.setItemsFabs(false)
                    }

                    // if the fabs are expanded is controlled by a UI state property
                    // the parent fab takes one different images depending on the value, and different functions to perform on click
                    if (it.areItemsFabsExpanded) {
                        binding.apply {
                            fabItemsParent.apply {
                                setImageResource(R.drawable.ic_collapse)
                                setOnClickListener {
                                    viewModel.setItemsFabs(false)
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
                                    viewModel.setItemsFabs(true)
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

            // display a dialog that explains the functionality of the clicks
            fabItemsInfo.setOnClickListener {
                val dialogView = layoutInflater.inflate(R.layout.alert_info, null)
                val txtMsg = dialogView.findViewById<TextView>(R.id.txtInfoMsg)
                txtMsg.text = getString(R.string.message_gc_info)
                AlertDialog.Builder(requireContext())
                    .setView(dialogView)
                    .create()
                    .show()
                viewModel.setItemsFabs(false)
            }
            fabItemsCanasta.setOnClickListener {
                findNavController().navigate(R.id.canastaFragment)
            }
            fabItemsHome.setOnClickListener {
                findNavController().navigate(R.id.homeFragment)

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
    override fun onClick(document: DocumentSnapshot) {
        viewModel.setInitPosition(document.getString(KEY_NOMBRE)!!)
        findNavController().navigate(R.id.itemSpecsHostFragment)
    }

    override fun onResume() {
        super.onResume()

        val query = fStore.collection(args.catPath)
            .whereEqualTo(KEY_IS_PRESENT, true)
            .orderBy(KEY_NOMBRE)
        val options = FirestoreRecyclerOptions.Builder<ItemMenuFirestore>()
            .setQuery(query, ItemMenuFirestore::class.java)
            .build()
        adapterItems = ItemsAdapter(options, this, this, fStorage, this)

        binding.recItems.apply {
            adapter = adapterItems
            layoutManager = LinearLayoutManager(requireContext())
        }

        adapterItems.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapterItems.stopListening()
        viewModel.setItemsFabs(false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun passItems(items: ArrayList<ItemMenuFirestore>) {
        items.forEach {
            Log.d(TAG, "passItems: THE_ITEMS_ARRAY: \nITEM_NAME - ${it.nombre}")
        }

        viewModel.setItems(items)
    }

}