package com.cafeyvinowinebar.cafe_y_vino_client.ui.main.giftshop

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cafeyvinowinebar.cafe_y_vino_client.KEY_IS_PRESENT
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.cafeyvinowinebar.cafe_y_vino_client.data.data_models.GiftFirestore
import com.cafeyvinowinebar.cafe_y_vino_client.interfaces.OnGiftClickListener
import com.cafeyvinowinebar.cafe_y_vino_client.isOnline
import com.cafeyvinowinebar.cafe_y_vino_client.ui.data_models.Gift
import com.cafeyvinowinebar.cafe_y_vino_client.ui.main.MainViewModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Displays the gifts menu in form of a recycler view
 * By pressing an item, the user purchases it with their bonus points (if sufficient)
 */
@AndroidEntryPoint
class GiftshopMenuFragment : DialogFragment(), OnGiftClickListener {

    private val viewModel: MainViewModel by hiltNavGraphViewModels(R.id.main_nav_graph)

    @Inject
    lateinit var fStore: FirebaseFirestore
    private lateinit var adapterGifts: GiftshopMenuAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // build a recycler view and return it as the fragment's view

        val recycleView = RecyclerView(requireContext())
        val query = fStore.collection("regalos").whereEqualTo(KEY_IS_PRESENT, true)
        val options = FirestoreRecyclerOptions.Builder<GiftFirestore>()
            .setQuery(query, GiftFirestore::class.java)
            .build()
        adapterGifts = GiftshopMenuAdapter(options, this)
        recycleView.apply {
            adapter = adapterGifts
            layoutManager = GridLayoutManager(requireContext(), 2)
            setHasFixedSize(true)

        }

        return recycleView
    }

    override fun onClick(gift: Gift) {
        if (!isOnline(requireContext())) {
            Toast.makeText(requireContext(), R.string.no_connection, Toast.LENGTH_SHORT).show()
        } else {
            AlertDialog.Builder(requireContext())
                .setMessage(getString(R.string.gift_msg, gift.nombre))
                .setPositiveButton(R.string.confirmar) { _, _ ->
                    if (viewModel.uiState.value.bonos - gift.precio.toLong() < 0) {
                        Toast.makeText(requireContext(), R.string.gift_saldo, Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        viewModel.sendGiftRequest(gift)
                    }
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .create().show()
        }
    }

    override fun onStart() {
        super.onStart()
        adapterGifts.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapterGifts.stopListening()
    }
}