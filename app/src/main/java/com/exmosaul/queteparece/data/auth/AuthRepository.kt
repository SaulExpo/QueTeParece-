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
        auth.signInWithEmailAndPassword(email.trim(), password).await()
        val user = auth.currentUser

        // Si el usuario existe pero NO está verificado → impedir acceso
        if (user != null && !user.isEmailVerified) {
            auth.signOut()
            throw Exception("EMAIL_NOT_VERIFIED")
        }

        return user
    }

    suspend fun signUp(name: String, username: String, email: String, password: String): Boolean {
        val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
        val firebaseUser = result.user ?: return false

        val newUser = User(
            uid = firebaseUser.uid,
            name = name.trim(),
            username = username.trim(),
            email = email.trim(),
            photoUrl = "https://icon-library.com/images/default-user-icon/default-user-icon-13.jpg",
            friends = emptyList(),
            friendRequests = emptyList(),
            recommendedMovies = emptyList(),
            favorites = emptyList(),
        )

        db.collection("users").document(firebaseUser.uid).set(newUser).await()

        firebaseUser.sendEmailVerification().await()

        auth.signOut()

        return true
    }
}