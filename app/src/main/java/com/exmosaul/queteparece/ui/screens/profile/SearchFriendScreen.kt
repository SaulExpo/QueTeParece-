package com.exmosaul.queteparece.ui.screens.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.exmosaul.queteparece.ui.navigation.BottomNavBar
import com.exmosaul.queteparece.ui.navigation.Routes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch

data class UserResult(
    val id: String,
    val uid: String,
    val username: String,
    val photoUrl: String,
    val status: FriendStatus
)


@Composable
fun SearchFriendsScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid ?: return

    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<UserResult>>(emptyList()) }
    val scope = rememberCoroutineScope()

    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            TextField(
                value = query,
                onValueChange = { text ->
                    query = text
                    scope.launch { results = searchUsers(text, db, currentUserId) }
                },
                label = { Text("Buscar usuarios...") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(results) { user ->
                    UserSearchRow(
                        navController,
                        user = user,
                        onSendRequest = {
                            scope.launch {
                                sendFriendRequest(user.id, currentUserId, db)
                                results = results.map {
                                    if (it.id == user.id) it.copy(status = FriendStatus.REQUEST_SENT)
                                    else it
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun UserSearchRow(navController: NavController, user: UserResult, onSendRequest: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate(Routes.FriendProfile.create(user.uid))
            }
    ) {
        AsyncImage(
            model = user.photoUrl,
            contentDescription = user.username,
            modifier = Modifier.size(48.dp).clip(RectangleShape),
        )
        Spacer(Modifier.width(12.dp))
        Text(user.username, Modifier.weight(1f))

        when (user.status) {
            FriendStatus.NONE ->
                TextButton(onClick = onSendRequest) { Text("Agregar") }
            FriendStatus.REQUEST_SENT ->
                Text("Solicitud enviada", color = MaterialTheme.colorScheme.primary)
            FriendStatus.FRIENDS ->
                Text("Amigos", color = MaterialTheme.colorScheme.secondary)
        }
    }
}

suspend fun searchUsers(query: String, db: FirebaseFirestore, currentUserId: String): List<UserResult> {
    if (query.isBlank()) return emptyList()

    val snapshot = db.collection("users")
        .whereGreaterThanOrEqualTo("username", query.lowercase())
        .whereLessThanOrEqualTo("username", query.lowercase() + "\uf8ff")
        .get()
        .await()

    return snapshot.documents.mapNotNull { doc ->
        val id = doc.id
        if (id == currentUserId) return@mapNotNull null

        val username = doc.getString("username") ?: return@mapNotNull null
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