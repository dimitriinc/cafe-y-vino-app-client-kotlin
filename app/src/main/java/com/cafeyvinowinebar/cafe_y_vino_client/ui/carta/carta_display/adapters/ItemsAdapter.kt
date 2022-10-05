package com.cafeyvinowinebar.cafe_y_vino_client.ui.carta.carta_display.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.cafeyvinowinebar.cafe_y_vino_client.data.data_models.ItemMenu
import com.cafeyvinowinebar.cafe_y_vino_client.data.sources.FirebaseStorageSource
import com.cafeyvinowinebar.cafe_y_vino_client.databinding.ListItemMenuItemBinding
import com.cafeyvinowinebar.cafe_y_vino_client.interfaces.OnItemLongClickListener
import com.cafeyvinowinebar.cafe_y_vino_client.interfaces.OnProductClickListener
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import javax.inject.Inject

class ItemsAdapter(
    options: FirestoreRecyclerOptions<ItemMenu>,
    val listener: OnProductClickListener,
    val longListener: OnItemLongClickListener
) : FirestoreRecyclerAdapter<ItemMenu, ItemsAdapter.ViewHolder>(options) {

    @Inject
    lateinit var fStorage: FirebaseStorageSource

    inner class ViewHolder(
        private val binding: ListItemMenuItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.apply {
                root.setOnClickListener {
                    if (absoluteAdapterPosition != RecyclerView.NO_POSITION) {
                        listener.onClick(
                            snapshots.getSnapshot(absoluteAdapterPosition),
                            ArrayList(snapshots)
                        )
                    }
                }
                root.setOnLongClickListener {
                    if (absoluteAdapterPosition != RecyclerView.NO_POSITION) {
                        longListener.onLongClick(snapshots.getSnapshot(absoluteAdapterPosition))
                    }
                    true
                }
            }
        }

        fun bind(model: ItemMenu) {
            binding.apply {
                txtItem.text = model.nombre
                if (model.image != null) {
                    Glide.with(root)
                        .load(fStorage.getImgReference(model.image))
                        .into(imgItem)
                } else {
                    imgItem.setImageResource(R.drawable.logo_stand_in)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ListItemMenuItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: ItemMenu) {
        holder.bind(model)
    }
}