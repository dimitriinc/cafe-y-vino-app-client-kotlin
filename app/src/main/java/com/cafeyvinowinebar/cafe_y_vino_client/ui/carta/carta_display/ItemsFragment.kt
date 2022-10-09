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
import androidx.recyclerview.widget.LinearLayoutManager
import com.cafeyvinowinebar.cafe_y_vino_client.KEY_IS_PRESENT
import com.cafeyvinowinebar.cafe_y_vino_client.KEY_ITEMS
import com.cafeyvinowinebar.cafe_y_vino_client.KEY_NOMBRE
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.cafeyvinowinebar.cafe_y_vino_client.data.data_models.ItemMenu
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

@AndroidEntryPoint
class ItemsFragment : Fragment(R.layout.fragment_items),
    OnProductClickListener,
    OnItemLongClickListener {

    lateinit var adapter: ItemsAdapter

    @Inject
    lateinit var fStore: FirebaseFirestore
    private val args: ItemsFragmentArgs by navArgs()
    private val viewModel: CartaDisplayViewModel by viewModels()

    @SuppressLint("InflateParams")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentItemsBinding.bind(view)

        val query = fStore.collection(args.categoryPath)
            .whereEqualTo(KEY_IS_PRESENT, true)
            .orderBy(KEY_NOMBRE)
        val options = FirestoreRecyclerOptions.Builder<ItemMenu>()
            .setQuery(query, ItemMenu::class.java)
            .build()
        adapter = ItemsAdapter(options, this, this)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {

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
                adapter = adapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }
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
                val action = ItemsFragmentDirections.actionItemsFragmentToCanastaFragment()
                findNavController().navigate(action)
            }
            fabItemsHome.setOnClickListener {
                findNavController().navigate(R.id.main_nav_graph)

            }
        }
    }

    override fun onLongClick(document: DocumentSnapshot) {
        viewModel.addProductToCanasta(document)
        Toast.makeText(requireContext(), R.string.on_adding_item, Toast.LENGTH_SHORT).show()
    }

    override fun onClick(document: DocumentSnapshot, items: ArrayList<ItemMenu>) {
        val bundle = Bundle()
        bundle.putSerializable(KEY_ITEMS, items)
        val action = ItemsFragmentDirections.actionItemsFragmentToItemSpecsActivity(
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