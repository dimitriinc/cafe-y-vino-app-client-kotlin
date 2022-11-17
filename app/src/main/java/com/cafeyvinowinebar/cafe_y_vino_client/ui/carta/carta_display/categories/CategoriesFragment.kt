package com.cafeyvinowinebar.cafe_y_vino_client.ui.carta.carta_display.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.cafeyvinowinebar.cafe_y_vino_client.data.data_models.MenuCategoryFirestore
import com.cafeyvinowinebar.cafe_y_vino_client.data.sources.FirebaseStorageSource
import com.cafeyvinowinebar.cafe_y_vino_client.databinding.FragmentCategoriesBinding
import com.cafeyvinowinebar.cafe_y_vino_client.interfaces.OnItemClickListener
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
class CategoriesFragment : Fragment(), OnItemClickListener {

    private val viewModel: CategoriesViewModel by viewModels()
    lateinit var adapterCategories: CategoriesAdapter

    @Inject
    lateinit var fStore: FirebaseFirestore
    @Inject
    lateinit var fStorage: FirebaseStorageSource

    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fabHome.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
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
        val toItemsAction =
            CategoriesFragmentDirections.actionCategoriesFragmentToItemsFragment(categoryPath)
        val toVinosAction = CategoriesFragmentDirections.actionCategoriesFragmentToVinosFragment()

        when (categoryName) {
            "Vinos" -> findNavController().navigate(toVinosAction)
            "Ofertas" -> {
                if (!viewModel.uiState.value.isPresent) {
                    findNavController().navigate(toItemsAction)
                } else {
                    val uiState = viewModel.uiState.value
                    if (uiState.isHappyHour == false) {
                        Toast.makeText(
                            requireContext(),
                            R.string.main_menu_ofertas_404,
                            Toast.LENGTH_SHORT
                        ).show()
                    } else if (uiState.isHappyHour == true) {
                        findNavController().navigate(toItemsAction)
                    }
                }
            }
            else -> findNavController().navigate(toItemsAction)
        }
    }

    override fun onResume() {
        super.onResume()

        val query = fStore.collection("menu")
        val options = FirestoreRecyclerOptions.Builder<MenuCategoryFirestore>()
            .setQuery(query, MenuCategoryFirestore::class.java)
            .build()
        adapterCategories = CategoriesAdapter(options, this, fStorage)

        binding.recCategories.apply {
            adapter = adapterCategories
            layoutManager = LinearLayoutManager(requireContext())
        }
        adapterCategories.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapterCategories.stopListening()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}