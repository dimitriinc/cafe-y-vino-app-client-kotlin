package com.cafeyvinowinebar.cafe_y_vino_client.data.utils_data

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun toStringList(value: String) = value.split(",")

    @TypeConverter
    fun fromStringList(value: List<String>) = value.joinToString(separator = ",")

    @TypeConverter
    fun fromLongList(value: List<Long>) = value.joinToString(separator = ",")

    @TypeConverter
    fun toLongList(value: String) = value.split(",").map { it.toLong() }
}