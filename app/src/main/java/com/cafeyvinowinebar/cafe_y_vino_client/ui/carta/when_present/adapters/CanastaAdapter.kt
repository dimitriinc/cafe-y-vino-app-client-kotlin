package com.cafeyvinowinebar.cafe_y_vino_client.ui.carta.when_present.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.cafeyvinowinebar.cafe_y_vino_client.data.canasta.ItemCanasta
import com.cafeyvinowinebar.cafe_y_vino_client.data.sources.FirebaseStorageSource
import com.cafeyvinowinebar.cafe_y_vino_client.databinding.ListItemCanastaBinding
import com.cafeyvinowinebar.cafe_y_vino_client.interfaces.OnCanastaListener
import javax.inject.Inject

class CanastaAdapter(
    private val listener: OnCanastaListener
) : ListAdapter<ItemCanasta, CanastaAdapter.CanastaViewHolder>(DiffCallback()) {

    @Inject
    private lateinit var fStorage: FirebaseStorageSource

    inner class CanastaViewHolder(
        private val binding: ListItemCanastaBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                root.setOnClickListener {
                    if (absoluteAdapterPosition != RecyclerView.NO_POSITION) {
                        listener.onCanastaClick(getItem(absoluteAdapterPosition))
                    }
                }
                root.setOnLongClickListener {
                    if (absoluteAdapterPosition != RecyclerView.NO_POSITION) {
                        listener.onCanastaLongClick(getItem(absoluteAdapterPosition))
                    }
                    true
                }
            }
        }

        fun bind(item: ItemCanasta) {
            binding.apply {
                txtCanastaItem.text = item.name
                if (item.icon != null) {
                    Glide.with(root)
                        .load(item.icon)
                        .into(imgCanasta)
                } else {
                    imgCanasta.setImageResource(R.drawable.logo_mini)
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ItemCanasta>() {
        override fun areItemsTheSame(oldItem: ItemCanasta, newItem: ItemCanasta) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ItemCanasta, newItem: ItemCanasta) = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CanastaViewHolder {
        val binding = ListItemCanastaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CanastaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CanastaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}