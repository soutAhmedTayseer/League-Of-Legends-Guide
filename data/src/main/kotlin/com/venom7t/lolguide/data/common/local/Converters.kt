package com.venom7t.lolguide.data.common.local

import androidx.room.TypeConverter
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

/**
 * Room type converters.
 *
 * Uses kotlinx.serialization rather than a delimiter join: champion tags are
 * safe to join on a comma today, but ability text and future list fields are
 * not, and a converter that silently corrupts on an unexpected character is
 * worse than one that is slightly more verbose.
 */
class Converters {

    @TypeConverter
    fun stringListToJson(value: List<String>?): String =
        json.encodeToString(ListSerializer(String.serializer()), value.orEmpty())

    @TypeConverter
    fun jsonToStringList(value: String?): List<String> =
        if (value.isNullOrBlank()) {
            emptyList()
        } else {
            runCatching { json.decodeFromString(ListSerializer(String.serializer()), value) }
                .getOrDefault(emptyList())
        }

    private companion object {
        val json = Json { ignoreUnknownKeys = true }
    }
}
