package com.example.lolguide

data class ChampionResponse(
    val data: Map<String, Champion>
)

data class Champion(
    val id: String,
    val name: String,
    val title: String,
    val blurb: String,
    val tags: List<String>?,
    val image: ImageInfo
)

data class ImageInfo(
    val full: String
)