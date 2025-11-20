package com.exmosaul.queteparece.ui.screens.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.exmosaul.queteparece.R
import com.exmosaul.queteparece.ui.navigation.BottomNavBar
import com.exmosaul.queteparece.ui.navigation.Routes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
                label = { Text(stringResource(R.string.search_users)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            when {
                query.isBlank() -> {
                    Text(
                        text = stringResource(R.string.results_will_show_here),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                query.isNotBlank() && results.isEmpty() -> {
                    Text(
                        text = stringResource(R.string.no_users_found),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                else -> {
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
            .padding(8.dp)
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
                TextButton(onClick = onSendRequest) {
                    Text(stringResource(R.string.add_friend))
                }

            FriendStatus.REQUEST_SENT ->
                Text(
                    stringResource(R.string.request_sent),
                    color = MaterialTheme.colorScheme.primary
                )

            FriendStatus.FRIENDS ->
                Text(
                    stringResource(R.string.friends_status),
                    color = MaterialTheme.colorScheme.secondary
                )
        }
    }
}

