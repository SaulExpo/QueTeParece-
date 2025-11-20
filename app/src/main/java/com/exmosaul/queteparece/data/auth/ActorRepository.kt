package com.exmosaul.queteparece.data.auth

import com.exmosaul.queteparece.data.model.Actor
import com.exmosaul.queteparece.data.model.Movie
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ActorRepository(
    private val db:FirebaseFirestore= FirebaseFirestore.getInstance()
) {
    suspend fun getActorById(actorId: String): Actor? {
        val doc = db.collection("actors").document(actorId).get().await()
        return doc.toObject(Actor::class.java)?.copy(id = doc.id)
    }

    suspend fun getMoviesByActor(actorId: String): List<Movie> {
        val snapshot = db.collection("movies")
            .whereArrayContains("actors", actorId)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            val data = doc.data ?: return@mapNotNull null
            documentToMovie(data, doc.id)
        }
    }
}
