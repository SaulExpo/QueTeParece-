package com.exmosaul.queteparece.data.model

import com.exmosaul.queteparece.data.model.Actor

data class Movie(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val category: String = "",
    val isFeatured: Boolean = false,
    val genres: List<String> = emptyList(),
    val type : String = "",
    val actors: List<String> = emptyList(),
    val rating: Int = 0,
)



