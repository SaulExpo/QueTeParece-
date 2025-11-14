package com.exmosaul.queteparece.data.auth

import com.exmosaul.queteparece.data.model.Actor
import com.exmosaul.queteparece.data.model.Movie
import kotlinx.coroutines.tasks.await

import com.google.firebase.firestore.FirebaseFirestore;

class ActorRepository(
    private val db:FirebaseFirestore= FirebaseFirestore.getInstance()
) {
    suspend fun getActorById(actorId: String): Actor? {
        val doc = db.collection("actors").document(actorId).get().await()
        print(doc.id)
        return doc.toObject(Actor::class.java)?.copy(id = doc.id)
    }

    suspend fun getMoviesByActor(actorId: String): List<Movie> {
        val snapshot = db.collection("movies")
            .whereArrayContains("actors", actorId)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Movie::class.java)?.copy(id = doc.id)
        }
    }
}
