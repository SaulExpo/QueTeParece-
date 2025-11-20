package com.exmosaul.queteparece.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exmosaul.queteparece.data.auth.documentToMovie
import com.exmosaul.queteparece.data.model.Actor
import com.exmosaul.queteparece.data.model.Movie
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
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
    val dislikes: Int = 0,
    val movieId: String = "",
    val likedBy: List<String> = emptyList(),
    val dislikedBy: List<String> = emptyList()

)

data class MovieDetailUiState(
    val movie: Movie? = null,
    val actors: List<Actor> = emptyList(),
    val reviews: List<Review> = emptyList(),
    val isFavorite: Boolean = false,
    val isLoading: Boolean = false,
    val reviewText: String = "",
    val error: String? = null,
    val userRating: Int? = null,
    val translatedTitle: String? = null,
    val translatedDescription: String? = null
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
                val data = movieDoc.data
                val movie = if (data != null) {
                    documentToMovie(data, movieId)
                } else null
                val user = auth.currentUser ?: return@launch

                val userRatingDoc = db.collection("movies")
                    .document(movieId)
                    .collection("ratings")
                    .document(user.uid)
                    .get().await()

                val userRating = userRatingDoc.getLong("rating")?.toInt()

                _uiState.value = _uiState.value.copy(
                    movie = movie,
                    userRating = userRating
                )

                movie?.actors.let { actor ->
                    actor?.let { loadActors(it) }
                }
                checkIfFavorite(movieId)
                loadReviews(movieId)

                _uiState.value = _uiState.value.copy(isLoading = false)

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    private fun loadActors(actorIds: List<String>) {
        viewModelScope.launch {
            try {
                val actorsList = mutableListOf<Actor>()

                for (actorId in actorIds) {
                    val doc = db.collection("actors").document(actorId).get().await()
                    doc.toObject(Actor::class.java)?.let { actor ->
                        actorsList.add(actor.copy(id = doc.id))
                    }
                }

                _uiState.value = _uiState.value.copy(actors = actorsList)

            } catch (e: Exception) {
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

    suspend fun submitReview(movieId: String) {
        val text = _uiState.value.reviewText.trim()
        val user = auth.currentUser ?: return
        val currentUid = auth.currentUser?.uid ?: ""
        val doc = db.collection("users").document(currentUid).get().await()
        if (text.isEmpty()) return

        val review = Review(
            id = db.collection("movies").document(movieId)
                .collection("reviews").document().id,
            userId = user.uid,
            username = doc.getString("username") ?: user.email ?: "Usuario",
            userPhoto = doc.getString("photoUrl"),
            movieId = movieId,
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

    fun toggleFavorite(movieId: String) {
        val user = auth.currentUser ?: return
        val userRef = db.collection("users").document(user.uid)

        viewModelScope.launch {
            val currentlyFavorite = _uiState.value.isFavorite

            if (currentlyFavorite) {
                userRef.update("favorites", FieldValue.arrayRemove(movieId)).await()
            } else {
                userRef.update("favorites", FieldValue.arrayUnion(movieId)).await()
            }

            _uiState.value = _uiState.value.copy(isFavorite = !currentlyFavorite)
        }
    }

    fun checkIfFavorite(movieId: String) {
        val user = auth.currentUser ?: return
        val userRef = db.collection("users").document(user.uid)

        viewModelScope.launch {
            val snapshot = userRef.get().await()
            val favorites = snapshot.get("favorites") as? List<String> ?: emptyList()

            _uiState.value = _uiState.value.copy(
                isFavorite = favorites.contains(movieId)
            )
        }
    }

    fun submitUserRating(movieId: String, rating: Int) {
        viewModelScope.launch {
            val user = FirebaseAuth.getInstance().currentUser ?: return@launch
            val db = FirebaseFirestore.getInstance()

            val userDoc = db.collection("movies")
                .document(movieId)
                .collection("ratings")
                .document(user.uid)

            userDoc.set(mapOf(
                "rating" to rating,
                "timestamp" to System.currentTimeMillis()
            )).await()

            val ratingsSnapshot = db.collection("movies")
                .document(movieId)
                .collection("ratings")
                .get().await()

            val ratings = ratingsSnapshot.documents.mapNotNull { it.getLong("rating")?.toInt() }

            val avg = if (ratings.isNotEmpty()) ratings.sum() / ratings.size.toDouble() else 0.0

            db.collection("movies").document(movieId)
                .update(
                    "rating", avg,
                    "ratingCount", ratings.size
                )

            loadMovie(movieId)
        }
    }

    fun deleteReview(movieId: String, reviewId: String) {
        viewModelScope.launch {
            try {
                db.collection("movies")
                    .document(movieId)
                    .collection("reviews")
                    .document(reviewId)
                    .delete()
                    .await()

                loadReviews(movieId)

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Error eliminando reseña: ${e.message}")
            }
        }
    }

    fun toggleReviewVote(movieId: String, reviewId: String, isLike: Boolean) {
        viewModelScope.launch {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            val db = FirebaseFirestore.getInstance()

            val reviewRef = db.collection("movies")
                .document(movieId)
                .collection("reviews")
                .document(reviewId)

            db.runTransaction { transaction ->
                val snap = transaction.get(reviewRef)

                val likedBy = (snap.get("likedBy") as? List<String>) ?: emptyList()
                val dislikedBy = (snap.get("dislikedBy") as? List<String>) ?: emptyList()

                val alreadyLiked = likedBy.contains(uid)
                val alreadyDisliked = dislikedBy.contains(uid)

                val newLikes = snap.getLong("likes")?.toInt() ?: 0
                val newDislikes = snap.getLong("dislikes")?.toInt() ?: 0

                if (isLike) {
                    when {
                        alreadyLiked -> {
                            transaction.update(reviewRef, mapOf(
                                "likes" to newLikes - 1,
                                "likedBy" to FieldValue.arrayRemove(uid)
                            ))
                        }
                        alreadyDisliked -> {
                            transaction.update(reviewRef, mapOf(
                                "likes" to newLikes + 1,
                                "dislikes" to newDislikes - 1,
                                "likedBy" to FieldValue.arrayUnion(uid),
                                "dislikedBy" to FieldValue.arrayRemove(uid)
                            ))
                        }
                        else -> {
                            transaction.update(reviewRef, mapOf(
                                "likes" to newLikes + 1,
                                "likedBy" to FieldValue.arrayUnion(uid)
                            ))
                        }
                    }
                } else {
                    when {
                        alreadyDisliked -> {
                            transaction.update(reviewRef, mapOf(
                                "dislikes" to newDislikes - 1,
                                "dislikedBy" to FieldValue.arrayRemove(uid)
                            ))
                        }
                        alreadyLiked -> {
                            transaction.update(reviewRef, mapOf(
                                "likes" to newLikes - 1,
                                "dislikes" to newDislikes + 1,
                                "likedBy" to FieldValue.arrayRemove(uid),
                                "dislikedBy" to FieldValue.arrayUnion(uid)
                            ))
                        }
                        else -> {
                            transaction.update(reviewRef, mapOf(
                                "dislikes" to newDislikes + 1,
                                "dislikedBy" to FieldValue.arrayUnion(uid)
                            ))
                        }
                    }
                }
            }.await()

            loadMovie(movieId)
        }
    }
}

