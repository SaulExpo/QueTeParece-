package com.exmosaul.queteparece.ui.screens.favorites


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.exmosaul.queteparece.data.model.Movie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class FavoritesUiState(
    val movies: List<Movie> = emptyList(),
    val favorites: Set<String> = emptySet(),
    val isLoading: Boolean = false
)

class FavoritesViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(FavoritesUiState(isLoading = true))
    val uiState: StateFlow<FavoritesUiState> = _uiState

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        val user = auth.currentUser ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Load favorites IDs
            println("A")
            val userDoc = db.collection("users").document(user.uid).get().await()
            println(userDoc)
            val favoriteIds = userDoc.get("favorites") as? List<String> ?: emptyList()
            println(favoriteIds)

            val movies = if (favoriteIds.isNotEmpty()) {
                db.collection("movies").whereIn("__name__", favoriteIds).get().await()
                    .documents.mapNotNull { it.toObject(Movie::class.java)?.copy(id = it.id) }
            } else emptyList()

            _uiState.value = FavoritesUiState(
                movies = movies,
                favorites = favoriteIds.toSet(),
                isLoading = false
            )
        }
    }

    fun toggleFavorite(movieId: String) {
        val user = auth.currentUser ?: return
        val current = _uiState.value.favorites.toMutableSet()

        if (current.contains(movieId)) current.remove(movieId)
        else current.add(movieId)

        _uiState.value = _uiState.value.copy(favorites = current)

        db.collection("users")
            .document(user.uid)
            .update("favorites", current.toList())
    }
}
