package com.cafeyvinowinebar.cafe_y_vino_client.ui.carta.carta_display

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.cafeyvinowinebar.cafe_y_vino_client.data.model_classes.ItemMenu
import com.cafeyvinowinebar.cafe_y_vino_client.data.model_classes.MenuCategory
import com.cafeyvinowinebar.cafe_y_vino_client.databinding.FragmentCategoriesBinding
import com.cafeyvinowinebar.cafe_y_vino_client.interfaces.OnItemClickListener
import com.cafeyvinowinebar.cafe_y_vino_client.ui.carta.carta_display.adapters.CategoriesAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class CategoriesFragment : Fragment(R.layout.fragment_categories), OnItemClickListener {

    private val viewModel: CartaDisplayViewModel by viewModels()
    @Inject lateinit var fStore: FirebaseFirestore
    lateinit var adapter: CategoriesAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentCategoriesBinding.bind(view)
        val query = fStore.collection("menu")
        val options = FirestoreRecyclerOptions.Builder<MenuCategory>()
            .setQuery(query, MenuCategory::class.java)
            .build()
        adapter = CategoriesAdapter(options, this)

        binding.apply {
            fabHome.setOnClickListener {
                // TODO: navigate to the main fragment
            }
            recCategories.apply {
                adapter = adapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }
        }

    }

    override fun onItemClick(item: DocumentSnapshot) {
        val categoryName = item.getString("name")!!
        val categoryPath = item.getString("catPath")!!
        val toItemsAction = CategoriesFragmentDirections.actionCategoriesFragmentToItemsFragment(categoryPath)
        val toVinosAction = CategoriesFragmentDirections.actionCategoriesFragmentToVinosFragment()

        when (categoryName) {
            "Vinos" -> findNavController().navigate(toVinosAction)
            "Ofertas" -> {
                if (!viewModel.uiState.value.isPresent) {
                    findNavController().navigate(toItemsAction)
                } else {
                    viewModel.isHourHappy()
                    lifecycleScope.launch {
                        viewModel.uiState.collect {
                            if (it.isHappyHour == false) {
                                Toast.makeText(requireContext(), R.string.main_menu_ofertas_404, Toast.LENGTH_SHORT).show()
                            } else if (it.isHappyHour == true) {
                                findNavController().navigate(toItemsAction)
                            }
                        }
                    }
                }
            }
            else -> findNavController().navigate(toItemsAction)
        }
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }
}