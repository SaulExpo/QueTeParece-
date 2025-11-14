package com.exmosaul.queteparece.ui.screens.profile

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.rememberCoroutineScope
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.launch

@Composable
fun FriendRequestsScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUid = auth.currentUser?.uid ?: return
    var requests by remember { mutableStateOf<List<UserFriendRequest>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val userDoc = db.collection("users").document(currentUid).get().await()
        val incomingIds = userDoc.get("friendRequests") as? List<String> ?: emptyList()

        // Obtener info de cada usuario que envió solicitud
        requests = incomingIds.mapNotNull { uid ->
            val doc = db.collection("users").document(uid).get().await()
            val username = doc.getString("username") ?: return@mapNotNull null
            val photo = doc.getString("photoUrl") ?: ""
            UserFriendRequest(uid, username, photo)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Solicitudes de amistad") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->

        LazyColumn(modifier = Modifier.padding(padding)) {
            items(requests) { request ->

                FriendRequestRow(
                    request = request,
                    onAccept = {
                        scope.launch {
                            acceptFriend(db, currentUid, request.uid)
                            requests = requests - request // UI update instantáneo
                        }
                    },
                    onReject = {
                        scope.launch {
                            rejectFriend(db, currentUid, request.uid)
                            requests = requests - request
                        }

                    }
                )
            }
        }
    }
}

data class UserFriendRequest(
    val uid: String,
    val username: String,
    val photoUrl: String
)

@Composable
fun FriendRequestRow(
    request: UserFriendRequest,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(12.dp)
    ) {
        AsyncImage(
            model = request.photoUrl,
            modifier = Modifier.size(48.dp).clip(CircleShape),
            contentDescription = null
        )
        Spacer(Modifier.width(12.dp))
        Text(request.username, Modifier.weight(1f))

        Button(onClick = onAccept) { Text("Aceptar") }
        Spacer(Modifier.width(8.dp))
        OutlinedButton(onClick = onReject) { Text("Rechazar") }
    }
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
