package com.cafeyvinowinebar.cafe_y_vino_client.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cafeyvinowinebar.cafe_y_vino_client.KEY_IS_PRESENT
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.cafeyvinowinebar.cafe_y_vino_client.data.model_classes.Gift
import com.cafeyvinowinebar.cafe_y_vino_client.interfaces.OnGiftClickListener
import com.cafeyvinowinebar.cafe_y_vino_client.isOnline
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GiftshopMenuFragment : DialogFragment(), OnGiftClickListener {

    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var fStore: FirebaseFirestore
    private lateinit var adapterGifts: GiftshopMenuAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val recycleView = RecyclerView(requireContext())
        val query = fStore.collection("regalos").whereEqualTo(KEY_IS_PRESENT, true)
        val options = FirestoreRecyclerOptions.Builder<Gift>()
            .setQuery(query, Gift::class.java)
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