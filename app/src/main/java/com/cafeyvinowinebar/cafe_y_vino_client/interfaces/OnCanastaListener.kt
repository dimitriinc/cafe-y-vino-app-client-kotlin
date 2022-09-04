package com.cafeyvinowinebar.cafe_y_vino_client.interfaces

import com.cafeyvinowinebar.cafe_y_vino_client.data.canasta.ItemCanasta

interface OnCanastaListener {
    fun onCanastaClick(item: ItemCanasta)
    fun onCanastaLongClick(item: ItemCanasta)
}