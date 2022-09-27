package com.cafeyvinowinebar.cafe_y_vino_client.ui.main

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.cafeyvinowinebar.cafe_y_vino_client.data.model_classes.Gift
import com.cafeyvinowinebar.cafe_y_vino_client.data.sources.FirebaseStorageSource
import com.cafeyvinowinebar.cafe_y_vino_client.databinding.ListItemGiftBinding
import com.cafeyvinowinebar.cafe_y_vino_client.interfaces.OnGiftClickListener
import com.cafeyvinowinebar.cafe_y_vino_client.interfaces.OnItemClickListener
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GiftshopMenuAdapter(
    options: FirestoreRecyclerOptions<Gift>,
    private val listener: OnGiftClickListener
): FirestoreRecyclerAdapter<Gift, GiftshopMenuAdapter.ViewHolder>(options) {

    @Inject lateinit var fStorage: FirebaseStorageSource

    inner class ViewHolder(
        private val binding: ListItemGiftBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                root.setOnClickListener {
                    if (absoluteAdapterPosition != RecyclerView.NO_POSITION) {
                        val snapshot = snapshots.getSnapshot(absoluteAdapterPosition)
                        val gift = Gift(
                            nombre = snapshot.getString("nombre")!!,
                            precio = snapshot.getString("precio")!!,
                            isPresent = true,
                            imagen = ""
                        )
                        listener.onClick(gift)
                    }
                }
            }
        }

        fun bind(model: Gift) {
            binding.apply {
                txtGift.text = model.nombre
                txtGiftPrecio.text = Resources.getSystem().getString(R.string.gift_list_item_price, model.precio)
                Glide.with(root).load(fStorage.getImgReference(model.imagen)).into(imgGift)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemGiftBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: Gift) {
        holder.bind(model)
    }
}