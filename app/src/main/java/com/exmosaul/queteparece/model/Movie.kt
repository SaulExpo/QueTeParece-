package com.exmosaul.queteparece.data.model

data class Movie(
    val title: String = "",
    val imageUrl: String = "",
    val category: String = "",
    val isFeatured: Boolean = false,
    val genres: List<String> = emptyList(),
    val type : String = ""
)
