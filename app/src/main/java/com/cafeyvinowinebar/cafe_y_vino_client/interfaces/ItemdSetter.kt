package com.cafeyvinowinebar.cafe_y_vino_client.interfaces

import com.cafeyvinowinebar.cafe_y_vino_client.data.data_models.ItemMenuFirestore

interface ItemsSetter {
    fun passItems(items: ArrayList<ItemMenuFirestore>)
}