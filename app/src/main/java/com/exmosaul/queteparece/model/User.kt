package com.exmosaul.queteparece.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val username: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val friends: List<String> = emptyList(),
    val friendRequests: List<String> = emptyList(),
    val recommendedMovies: List<String> = emptyList(),
    val favorites: List<String> =  emptyList(),
)
