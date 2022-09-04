package com.cafeyvinowinebar.cafe_y_vino_client.ui.carta.when_present.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cafeyvinowinebar.cafe_y_vino_client.data.model_classes.ItemCuenta
import com.cafeyvinowinebar.cafe_y_vino_client.databinding.ListItemCuentaBinding
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class CuentaAdapter(
    options: FirestoreRecyclerOptions<ItemCuenta>
) : FirestoreRecyclerAdapter<ItemCuenta, CuentaAdapter.CuentaViewHolder>(options){

    inner class CuentaViewHolder(
        private val binding: ListItemCuentaBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(model: ItemCuenta) {
            binding.apply {
                cuentaCount.text = model.count.toString()
                cuentaName.text = model.name
                cuentaPrice.text = model.price.toString()
                cuentaTotal.text = model.total.toString()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CuentaViewHolder {
        val binding = ListItemCuentaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CuentaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CuentaViewHolder, position: Int, model: ItemCuenta) {
        holder.bind(model)
    }
}