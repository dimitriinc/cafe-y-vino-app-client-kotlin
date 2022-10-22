package com.cafeyvinowinebar.cafe_y_vino_client.ui.carta.carta_display

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.cafeyvinowinebar.cafe_y_vino_client.data.data_models.MenuCategoryFirestore
import com.cafeyvinowinebar.cafe_y_vino_client.data.sources.FirebaseStorageSource
import com.cafeyvinowinebar.cafe_y_vino_client.databinding.FragmentCategoriesBinding
import com.cafeyvinowinebar.cafe_y_vino_client.interfaces.OnItemClickListener
import com.cafeyvinowinebar.cafe_y_vino_client.ui.carta.carta_display.adapters.CategoriesAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * All the products in the menu are divided into categories
 * The fragment displays them as a recycler view
 */
@AndroidEntryPoint
class CategoriesFragment @Inject constructor(
    val fStore: FirebaseFirestore,
    val fStorage: FirebaseStorageSource
) : Fragment(R.layout.fragment_categories), OnItemClickListener {

    private val viewModel: CartaDisplayViewModel by navGraphViewModels(R.id.carta_nav_graph)
    lateinit var adapter: CategoriesAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentCategoriesBinding.bind(view)

        // construct the adapter
        val query = fStore.collection("menu")
        val options = FirestoreRecyclerOptions.Builder<MenuCategoryFirestore>()
            .setQuery(query, MenuCategoryFirestore::class.java)
            .build()
        adapter = CategoriesAdapter(options, this, fStorage)

        binding.apply {
            fabHome.setOnClickListener {
                val action = CategoriesFragmentDirections.actionCategoriesFragmentToMainNavGraph()
                findNavController().navigate(action)
            }
            recCategories.apply {
                adapter = adapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }
        }

    }

    /**
     * On click we navigate to the next fragment displaying the items of the chosen category
     * If the category is Vinos, we navigate to the fragment where the user chooses a subcategory of wines
     * If the category is Ofertas (which contains the items of happy hour), we first check the presence status of the user
     * If not present, they can move to the items freely, if present (means they can add items to Canasta), they can enter only during the happy hours
     */
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
                    val uiState = viewModel.uiState.value
                    if (uiState.isHappyHour == false) {
                        Toast.makeText(requireContext(), R.string.main_menu_ofertas_404, Toast.LENGTH_SHORT).show()
                    } else if (uiState.isHappyHour == true) {
                        findNavController().navigate(toItemsAction)
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