package com.exmosaul.queteparece.data.auth

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.exmosaul.queteparece.data.model.Movie
import kotlinx.coroutines.tasks.await

class MovieRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun getMoviesByCategory(category: String): List<Movie> {
        return try {
            db.collection("movies")
                .whereEqualTo("category", category)
                .get()
                .await()
                .toObjects(Movie::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getMoviesByType(type: String): List<Movie> {
        return try {
            db.collection("movies")
                .whereEqualTo("type", type)
                .get()
                .await()
                .toObjects(Movie::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getFeaturedMovie(): Movie? {
        return try {
            val snapshot = db.collection("movies")
                .whereEqualTo("isFeatured", true)
                .get()
                .await()
            val movies = snapshot.toObjects(Movie::class.java)
            movies.randomOrNull()
        } catch (e: Exception) {
            null
        }
    }
}
