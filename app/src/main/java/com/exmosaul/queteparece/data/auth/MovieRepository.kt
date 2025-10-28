package com.exmosaul.queteparece.data.auth

import com.google.firebase.firestore.FirebaseFirestore
import com.exmosaul.queteparece.data.model.Movie
import kotlinx.coroutines.tasks.await

class MovieRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    // 🟢 Películas por categoría
    suspend fun getMoviesByCategory(category: String): List<Movie> {
        return try {
            val snapshot = db.collection("movies")
                .whereEqualTo("category", category)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Movie::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            println("❌ Error getMoviesByCategory: ${e.message}")
            emptyList()
        }
    }

    // 🟢 Películas por tipo (animada / live action)
    suspend fun getMoviesByType(type: String): List<Movie> {
        return try {
            val snapshot = db.collection("movies")
                .whereEqualTo("type", type)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Movie::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            println("❌ Error getMoviesByType: ${e.message}")
            emptyList()
        }
    }

    // 🟢 Película destacada
    suspend fun getFeaturedMovie(): Movie? {
        return try {
            val snapshot = db.collection("movies")
                .whereEqualTo("isFeatured", true)
                .limit(1)
                .get()
                .await()

            snapshot.documents.firstOrNull()?.let { doc ->
                doc.toObject(Movie::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            println("❌ Error getFeaturedMovie: ${e.message}")
            null
        }
    }

    // 🟢 Obtener película por ID (para la pantalla de detalle)
    suspend fun getMovieById(id: String): Movie? {
        return try {
            val doc = db.collection("movies").document(id).get().await()
            if (!doc.exists()) {
                println("⚠️ Documento con ID $id no existe en Firestore")
                null
            } else {
                doc.toObject(Movie::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            println("❌ Error getMovieById: ${e.message}")
            null
        }
    }
}
