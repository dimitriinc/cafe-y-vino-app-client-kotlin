package com.cafeyvinowinebar.cafe_y_vino_client.ui.main.giftshop

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
import com.cafeyvinowinebar.cafe_y_vino_client.data.data_models.GiftFirestore
import com.cafeyvinowinebar.cafe_y_vino_client.data.sources.FirebaseStorageSource
import com.cafeyvinowinebar.cafe_y_vino_client.databinding.FragmentDialogGiftshopBinding
import com.cafeyvinowinebar.cafe_y_vino_client.interfaces.OnGiftClickListener
import com.cafeyvinowinebar.cafe_y_vino_client.isOnline
import com.cafeyvinowinebar.cafe_y_vino_client.ui.data_models.Gift
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

    private val viewModel: GiftshopViewModel by viewModels(ownerProducer = { requireParentFragment() })

    @Inject
    lateinit var fStore: FirebaseFirestore
    private lateinit var adapterGifts: GiftshopMenuAdapter
    @Inject lateinit var fStorage: FirebaseStorageSource

    private var _binding: FragmentDialogGiftshopBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDialogGiftshopBinding.inflate(inflater, container, false)
        return binding.root
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
                .create().show()
        }
    }

    override fun onResume() {
        super.onResume()

        val query = fStore.collection("regalos").whereEqualTo(KEY_IS_PRESENT, true)
        val options = FirestoreRecyclerOptions.Builder<GiftFirestore>()
            .setQuery(query, GiftFirestore::class.java)
            .build()
        adapterGifts = GiftshopMenuAdapter(options, this, requireContext(), fStorage)

        binding.root.apply {
            adapter = adapterGifts
            layoutManager = GridLayoutManager(requireContext(), 2)
        }

        adapterGifts.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapterGifts.stopListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}