package com.cafeyvinowinebar.cafe_y_vino_client.interfaces

import com.cafeyvinowinebar.cafe_y_vino_client.data.model_classes.Gift

interface OnGiftClickListener {
    fun onClick(gift: Gift)
}