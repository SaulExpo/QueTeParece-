package com.exmosaul.queteparece.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val userRating: Int? = null
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
                val user = auth.currentUser ?: return@launch

                val userRatingDoc = db.collection("movies")
                    .document(movieId)
                    .collection("ratings")
                    .document(user.uid)
                    .get().await()

                val userRating = userRatingDoc.getLong("rating")?.toInt()

                // Guardamos la película en UI
                _uiState.value = _uiState.value.copy(
                    movie = movie,
                    userRating = userRating
                )

                // ✅ Si la peli tiene reparto → cargamos actores
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
                        actorsList.add(actor.copy(id = doc.id)) // ✅ ahora el actor tiene id real
                    }
                }

                _uiState.value = _uiState.value.copy(actors = actorsList)

            } catch (e: Exception) {
                println("❌ Error cargando actores: ${e.message}")
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

    fun runActorMovieSync() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                addMoviesToActors()
                println("✅ Sincronización completada correctamente")
            } catch (e: Exception) {
                println("❌ Error: ${e.message}")
                e.printStackTrace()
            }
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

            // Guardar nota del usuario
            userDoc.set(mapOf(
                "rating" to rating,
                "timestamp" to System.currentTimeMillis()
            )).await()

            // Recalcular promedio
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

            loadMovie(movieId) // refrescar UI
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
                            // ❌ Quitar like
                            transaction.update(reviewRef, mapOf(
                                "likes" to newLikes - 1,
                                "likedBy" to FieldValue.arrayRemove(uid)
                            ))
                        }
                        alreadyDisliked -> {
                            // 👍 Cambiar dislike → like
                            transaction.update(reviewRef, mapOf(
                                "likes" to newLikes + 1,
                                "dislikes" to newDislikes - 1,
                                "likedBy" to FieldValue.arrayUnion(uid),
                                "dislikedBy" to FieldValue.arrayRemove(uid)
                            ))
                        }
                        else -> {
                            // 👍 Dar like
                            transaction.update(reviewRef, mapOf(
                                "likes" to newLikes + 1,
                                "likedBy" to FieldValue.arrayUnion(uid)
                            ))
                        }
                    }
                } else {
                    when {
                        alreadyDisliked -> {
                            // ❌ Quitar dislike
                            transaction.update(reviewRef, mapOf(
                                "dislikes" to newDislikes - 1,
                                "dislikedBy" to FieldValue.arrayRemove(uid)
                            ))
                        }
                        alreadyLiked -> {
                            // 👎 Cambiar like → dislike
                            transaction.update(reviewRef, mapOf(
                                "likes" to newLikes - 1,
                                "dislikes" to newDislikes + 1,
                                "likedBy" to FieldValue.arrayRemove(uid),
                                "dislikedBy" to FieldValue.arrayUnion(uid)
                            ))
                        }
                        else -> {
                            // 👎 Dar dislike
                            transaction.update(reviewRef, mapOf(
                                "dislikes" to newDislikes + 1,
                                "dislikedBy" to FieldValue.arrayUnion(uid)
                            ))
                        }
                    }
                }
            }.await()

            // Volver a cargar reviews
            loadMovie(movieId)
        }
    }


}

private fun capitalizeName(text: String): String {
    return text.replaceFirstChar { it.uppercase() }
}

suspend fun addMoviesToActors() {
    val db = FirebaseFirestore.getInstance()

    val moviesSnapshot = db.collection("movies").get().await()

    val actorToMovies = mutableMapOf<String, MutableList<String>>()

    for (doc in moviesSnapshot.documents) {
        val movieId = doc.id
        val actors = doc.get("actors") as? List<String> ?: emptyList()

        for (actorId in actors) {
            actorToMovies.getOrPut(actorId) { mutableListOf() }.add(movieId)
        }
    }

    val batch = db.batch()

    actorToMovies.forEach { (actorId, movieList) ->
        val actorRef = db.collection("actors").document(actorId)
        // Si el doc no existe aún, lo crea
        batch.set(actorRef, mapOf("movies" to movieList), SetOptions.merge())
    }

    batch.commit().await()
}