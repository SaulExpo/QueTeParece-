package com.exmosaul.queteparece.ui.screens.profile

import android.graphics.Bitmap
import android.net.Uri
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


suspend fun removeFriend(db: FirebaseFirestore, currentUid: String, friendUid: String) {
    val currentRef = db.collection("users").document(currentUid)
    val friendRef = db.collection("users").document(friendUid)

    currentRef.update("friends", FieldValue.arrayRemove(friendUid)).await()
    friendRef.update("friends", FieldValue.arrayRemove(currentUid)).await()
}

fun uploadUriToCloudinary(
    context: android.content.Context,
    uri: Uri,
    userId: String,
    onUploaded: (String) -> Unit
) {
    val tempFile = File(context.cacheDir, "img_$userId.jpg")

    context.contentResolver.openInputStream(uri)?.use { input ->
        FileOutputStream(tempFile).use { output ->
            input.copyTo(output)
        }
    }

    uploadFileToCloudinary(tempFile, onUploaded)
}

fun uploadBitmapToCloudinary(
    context: android.content.Context,
    bitmap: Bitmap,
    userId: String,
    onUploaded: (String) -> Unit
) {
    val file = File(context.cacheDir, "photo_$userId.jpg")
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
    }

    uploadFileToCloudinary(file, onUploaded)
}

fun uploadFileToCloudinary(
    file: File,
    onUploaded: (String) -> Unit
) {
    val cloudName = "dhy9na0gx"
    val preset = "QueTeParece"

    val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart(
            "file",
            file.name,
            file.asRequestBody("image/jpeg".toMediaType())
        )
        .addFormDataPart("upload_preset", preset)
        .build()

    val request = Request.Builder()
        .url("https://api.cloudinary.com/v1_1/$cloudName/image/upload")
        .post(requestBody)
        .build()

    OkHttpClient().newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
        }

        override fun onResponse(call: Call, response: Response) {
            response.body?.string()?.let {
                val secureUrl = JSONObject(it).getString("secure_url")
                onUploaded(secureUrl)
            }
        }
    })
}

fun updateFirebasePhoto(url: String, user: FirebaseUser, db: FirebaseFirestore) {
    user.updateProfile(
        userProfileChangeRequest {
            photoUri = Uri.parse(url)
        }
    )

    db.collection("users").document(user.uid)
        .set(mapOf("photoUrl" to url), SetOptions.merge())
}

suspend fun acceptFriend(db: FirebaseFirestore, currentUid: String, otherUid: String) {
    val currentRef = db.collection("users").document(currentUid)
    val otherRef = db.collection("users").document(otherUid)

    currentRef.update("friends", FieldValue.arrayUnion(otherUid))
    otherRef.update("friends", FieldValue.arrayUnion(currentUid))

    currentRef.update("friendRequests", FieldValue.arrayRemove(otherUid))
}

suspend fun rejectFriend(db: FirebaseFirestore, currentUid: String, otherUid: String) {
    db.collection("users").document(currentUid)
        .update("friendRequests", FieldValue.arrayRemove(otherUid))
}

suspend fun searchUsers(query: String, db: FirebaseFirestore, currentUserId: String): List<UserResult> {
    if (query.isBlank()) return emptyList()

    val snapshot = db.collection("users")
        .get()
        .await()

    val queryLower = query.lowercase()

    return snapshot.documents.mapNotNull { doc ->
        val id = doc.id
        if (id == currentUserId) return@mapNotNull null

        val username = doc.getString("username") ?: return@mapNotNull null

        if (!username.contains(query, ignoreCase = true)) return@mapNotNull null

        val photo = doc.getString("photoUrl") ?: ""
        val uid = doc.getString("uid") ?: ""

        val friends = doc.get("friends") as? List<String> ?: emptyList()
        val incoming = doc.get("friendRequests") as? List<String> ?: emptyList()

        val status = when {
            currentUserId in friends -> FriendStatus.FRIENDS
            currentUserId in incoming -> FriendStatus.REQUEST_SENT
            else -> FriendStatus.NONE
        }

        UserResult(id, uid, username, photo, status)
    }
}

suspend fun sendFriendRequest(receiverId: String, senderId: String, db: FirebaseFirestore) {
    db.collection("users").document(receiverId)
        .update("friendRequests", FieldValue.arrayUnion(senderId))
        .await()
}