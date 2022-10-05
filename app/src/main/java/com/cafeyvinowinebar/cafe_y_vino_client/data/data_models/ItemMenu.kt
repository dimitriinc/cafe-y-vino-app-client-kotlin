package com.cafeyvinowinebar.cafe_y_vino_client.data.data_models

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

/**
 * Represents a menu item, as it is stored in the menu collection in the database
 * Model for the AdapterCategory
 */

@Parcelize
@Keep
class ItemMenu(
    val nombre: String,
    val categoria: String,
    val descripcion: String?,
    val precio: String,
    val image: String?,
    val icon: String?,
    val isPresent: Boolean
) : Parcelable {
}