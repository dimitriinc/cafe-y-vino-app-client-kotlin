package com.cafeyvinowinebar.cafe_y_vino_client.data.data_models

import android.os.Parcelable
import androidx.annotation.Keep
import com.cafeyvinowinebar.cafe_y_vino_client.data.canasta.ItemCanasta
import kotlinx.android.parcel.Parcelize

/**
 * Represents a menu item, as it is stored in the menu collection in the database
 * Model for the AdapterCategory
 */

@Keep
data class ItemMenuFirestore(
    val nombre: String? = null,
    val categoria: String? = null,
    val descripcion: String? = null,
    val precio: String? = null,
    val image: String? = null,
    val icon: String? = null,
    val isPresent: Boolean? = null
) : java.io.Serializable {
}

fun ItemMenuFirestore.asItemCanasta(): ItemCanasta =
    ItemCanasta(
        name = nombre!!,
        category = categoria!!,
        icon = icon,
        price = precio!!.toLong()
    )