package com.exmosaul.queteparece.ui.screens.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.exmosaul.queteparece.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun FriendRequestsScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUid = auth.currentUser?.uid ?: return
    var requests by remember { mutableStateOf<List<UserFriendRequest>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        loading = true
        val userDoc = db.collection("users").document(currentUid).get().await()
        val incomingIds = userDoc.get("friendRequests") as? List<String> ?: emptyList()

        requests = incomingIds.mapNotNull { uid ->
            val doc = db.collection("users").document(uid).get().await()
            val username = doc.getString("username") ?: return@mapNotNull null
            val photo = doc.getString("photoUrl") ?: ""
            UserFriendRequest(uid, username, photo)
        }

        loading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.friend_requests_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { padding ->

        when {
            loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            requests.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.no_requests),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            else -> {
                LazyColumn(modifier = Modifier.padding(padding)) {
                    items(requests) { request ->

                        FriendRequestRow(
                            request = request,
                            onAccept = {
                                scope.launch {
                                    acceptFriend(db, currentUid, request.uid)
                                    requests = requests - request
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        AsyncImage(
            model = request.photoUrl,
            modifier = Modifier.size(48.dp).clip(CircleShape),
            contentDescription = null
        )
        Spacer(Modifier.width(12.dp))
        Text(request.username, Modifier.weight(1f))

        Button(onClick = onAccept) { Text(stringResource(R.string.accept)) }
        Spacer(Modifier.width(8.dp))
        OutlinedButton(onClick = onReject) { Text(stringResource(R.string.reject)) }
    }
}

