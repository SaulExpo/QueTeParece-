package com.exmosaul.queteparece.data.model

data class Actor(
    val id: String ="",
    val name: String = "",
    val surname: String = "",
    val age: Int = 0,
    val bio: String = "",
    val imageUrl: String = "",
    val movies: List<String> = emptyList(),
    ){
    val fullName get() = "$name $surname"
}