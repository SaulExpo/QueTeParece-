package com.exmosaul.queteparece.data.model


data class Movie(
    val id: String = "",
    val title: Map<String, String> = emptyMap(),
    val description: Map<String, String> = emptyMap(),
    val imageUrl: String = "",
    val category: String = "",
    val isFeatured: Boolean = false,
    val genres: List<String> = emptyList(),
    val type : String = "",
    val actors: List<String> = emptyList(),
    val rating: Int = 0,
    val platforms: List<String> = emptyList(),
    val trailer: String = ""
)



