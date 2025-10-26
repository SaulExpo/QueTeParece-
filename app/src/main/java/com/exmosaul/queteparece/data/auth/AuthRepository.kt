package com.exmosaul.queteparece.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.exmosaul.queteparece.data.model.User
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    val currentUser: FirebaseUser? get() = auth.currentUser

    suspend fun signIn(email: String, password: String): FirebaseUser? {
        println("_______________________")
        auth.signInWithEmailAndPassword(email.trim(), password).await()
        println(auth.currentUser)
        println("_______________________")
        return auth.currentUser
    }

    suspend fun signUp(name: String, username: String, email: String, password: String): FirebaseUser? {
        val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
        val firebaseUser = result.user ?: return null

        // Guardar datos adicionales en Firestore
        val user = User(
            uid = firebaseUser.uid,
            name = name.trim(),
            username = username.trim(),
            email = email.trim()
        )

        db.collection("users").document(firebaseUser.uid).set(user).await()
        return firebaseUser
    }

    fun signOut() { auth.signOut() }
}
