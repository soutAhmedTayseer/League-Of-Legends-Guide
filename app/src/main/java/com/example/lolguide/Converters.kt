package com.example.lolguide

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>?): String = gson.toJson(value)

    @TypeConverter
    fun toStringList(value: String): List<String>? {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromImageInfo(value: ImageInfo): String = gson.toJson(value)

    @TypeConverter
    fun toImageInfo(value: String): ImageInfo = gson.fromJson(value, ImageInfo::class.java)

    @TypeConverter
    fun fromInfo(value: Info?): String = gson.toJson(value)

    @TypeConverter
    fun toInfo(value: String?): Info? = gson.fromJson(value, Info::class.java)

    @TypeConverter
    fun fromStats(value: Stats?): String = gson.toJson(value)

    @TypeConverter
    fun toStats(value: String?): Stats? = gson.fromJson(value, Stats::class.java)
}