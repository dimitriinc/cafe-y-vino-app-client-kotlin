package com.cafeyvinowinebar.cafe_y_vino_client.ui.main.giftshop

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.cafeyvinowinebar.cafe_y_vino_client.data.data_models.GiftFirestore
import com.cafeyvinowinebar.cafe_y_vino_client.data.sources.FirebaseStorageSource
import com.cafeyvinowinebar.cafe_y_vino_client.databinding.ListItemGiftBinding
import com.cafeyvinowinebar.cafe_y_vino_client.interfaces.OnGiftClickListener
import com.cafeyvinowinebar.cafe_y_vino_client.ui.data_models.Gift
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

class GiftshopMenuAdapter(
    options: FirestoreRecyclerOptions<GiftFirestore>,
    private val listener: OnGiftClickListener,
    val context: Context,
    val fStorage: FirebaseStorageSource
): FirestoreRecyclerAdapter<GiftFirestore, GiftshopMenuAdapter.ViewHolder>(options) {

    inner class ViewHolder(
        private val binding: ListItemGiftBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                root.setOnClickListener {
                    if (absoluteAdapterPosition != RecyclerView.NO_POSITION) {
                        val snapshot = snapshots.getSnapshot(absoluteAdapterPosition)
                        // convert it to a UI version of gift
                        val gift = Gift(
                            nombre = snapshot.getString("nombre")!!,
                            precio = snapshot.getString("precio")!!
                        )
                        listener.onClick(gift)
                    }
                }
            }
        }

        fun bind(model: GiftFirestore) {
            binding.apply {
                txtGift.text = model.nombre
                txtGiftPrecio.text = context.getString(R.string.gift_bonos, model.precio)
                Glide.with(root)
                    .load(fStorage.getImgReference(model.imagen!!))
                    .into(imgGift)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemGiftBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: GiftFirestore) {
        holder.bind(model)
    }
}