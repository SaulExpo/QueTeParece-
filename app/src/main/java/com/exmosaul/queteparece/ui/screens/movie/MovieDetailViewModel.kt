package com.exmosaul.queteparece.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exmosaul.queteparece.data.model.Movie
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class Review(
    val id: String = "",
    val userId: String = "",
    val username: String = "",
    val userPhoto: String? = null,
    val text: String = "",
    val likes: Int = 0,
    val dislikes: Int = 0
)

data class MovieDetailUiState(
    val movie: Movie? = null,
    val reviews: List<Review> = emptyList(),
    val isLoading: Boolean = false,
    val reviewText: String = "",
    val error: String? = null
)

class MovieDetailViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(MovieDetailUiState())
    val uiState: StateFlow<MovieDetailUiState> = _uiState

    fun loadMovie(movieId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val movieDoc = db.collection("movies").document(movieId).get().await()
                val movie = movieDoc.toObject(Movie::class.java)?.copy(id = movieId)
                _uiState.value = _uiState.value.copy(movie = movie, isLoading = false)
                loadReviews(movieId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun loadReviews(movieId: String) {
        viewModelScope.launch {
            val snapshot = db.collection("movies").document(movieId)
                .collection("reviews").get().await()
            val reviews = snapshot.documents.mapNotNull { it.toObject(Review::class.java) }
            _uiState.value = _uiState.value.copy(reviews = reviews)
        }
    }

    fun onReviewTextChange(text: String) {
        _uiState.value = _uiState.value.copy(reviewText = text)
    }

    fun submitReview(movieId: String) {
        val text = _uiState.value.reviewText.trim()
        val user = auth.currentUser ?: return
        if (text.isEmpty()) return

        val review = Review(
            id = db.collection("movies").document(movieId)
                .collection("reviews").document().id,
            userId = user.uid,
            username = user.displayName ?: user.email ?: "Usuario",
            text = text
        )

        viewModelScope.launch {
            db.collection("movies").document(movieId)
                .collection("reviews").document(review.id)
                .set(review)
            _uiState.value = _uiState.value.copy(reviewText = "")
            loadReviews(movieId)
        }
    }
}
