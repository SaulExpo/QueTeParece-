package com.exmosaul.queteparece.data.auth

import com.exmosaul.queteparece.data.model.Movie
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MovieRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun getMoviesByCategory(category: String): List<Movie> {
        return try {
            val snapshot = db.collection("movies")
                .whereEqualTo("category", category)
                .get()
                .await()

            snapshot.documents.map { doc ->
                documentToMovie(doc.data ?: emptyMap(), doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getMoviesByType(type: String): List<Movie> {
        return try {
            val snapshot = db.collection("movies")
                .whereEqualTo("type", type)
                .get()
                .await()

            snapshot.documents.map { doc ->
                documentToMovie(doc.data ?: emptyMap(), doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }


    suspend fun getMovieById(id: String): Movie? {
        return try {
            val doc = db.collection("movies").document(id).get().await()
            if (!doc.exists()) {
                null
            } else {
                documentToMovie(doc.data ?: emptyMap(), doc.id)
            }
        } catch (e: Exception) {
            null
        }
    }


}

fun extractStringOrLocalized(value: Any?): Map<String, String> {
    return when (value) {
        is String -> mapOf("default" to value) // Si era string simple
        is Map<*, *> -> value.mapNotNull {
            val key = it.key as? String
            val v = it.value as? String
            if (key != null && v != null) key to v else null
        }.toMap()
        else -> emptyMap()
    }
}

fun documentToMovie(doc: Map<String, Any?>, id: String): Movie {
    return Movie(
        id = id,
        title = extractStringOrLocalized(doc["title"]),
        description = extractStringOrLocalized(doc["description"]),
        imageUrl = doc["imageUrl"] as? String ?: "",
        category = doc["category"] as? String ?: "",
        isFeatured = doc["isFeatured"] as? Boolean ?: false,
        genres = (doc["genres"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
        type = doc["type"] as? String ?: "",
        actors = (doc["actors"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
        rating = (doc["rating"] as? Number)?.toInt() ?: 0,
        trailer = doc["trailer"] as? String ?: "",
        platforms = (doc["platforms"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
    )
}