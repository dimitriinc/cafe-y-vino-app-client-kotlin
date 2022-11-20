package com.cafeyvinowinebar.cafe_y_vino_client.ui.carta.carta_display.categories

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cafeyvinowinebar.cafe_y_vino_client.data.data_models.MenuCategoryFirestore
import com.cafeyvinowinebar.cafe_y_vino_client.data.sources.FirebaseStorageSource
import com.cafeyvinowinebar.cafe_y_vino_client.databinding.ListItemMenuBinding
import com.cafeyvinowinebar.cafe_y_vino_client.interfaces.OnItemClickListener
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class CategoriesAdapter(
    options: FirestoreRecyclerOptions<MenuCategoryFirestore>,
    val listener: OnItemClickListener,
    val fStorage: FirebaseStorageSource
) : FirestoreRecyclerAdapter<MenuCategoryFirestore, CategoriesAdapter.ViewHolder>(options) {

    inner class ViewHolder(
        private val binding: ListItemMenuBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                root.setOnClickListener {
                    if (absoluteAdapterPosition != RecyclerView.NO_POSITION) {
                        listener.onItemClick(snapshots.getSnapshot(absoluteAdapterPosition))
                    }
                }
            }
        }

        fun bind(model: MenuCategoryFirestore) {
            binding.apply {
                txtCategoryName.text = model.name
                Glide.with(root)
                    .load(fStorage.getImgReference(model.image!!))
                    .into(imgCategory)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemMenuBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: MenuCategoryFirestore) {
        holder.bind(model)
    }
}