package com.exmosaul.queteparece.ui.screens.profile

import LanguageManager
import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.exmosaul.queteparece.R
import com.exmosaul.queteparece.data.auth.documentToMovie
import com.exmosaul.queteparece.data.model.Movie
import com.exmosaul.queteparece.ui.navigation.BottomNavBar
import com.exmosaul.queteparece.ui.navigation.Routes
import com.exmosaul.queteparece.ui.screens.detail.Review
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class Friend(
    val uid: String,
    val name: String,
    val photoUrl: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val db = FirebaseFirestore.getInstance()
    val currentUid = auth.currentUser?.uid ?: ""
    val scope = rememberCoroutineScope()

    var username by remember { mutableStateOf<String?>(null) }
    var name by remember { mutableStateOf<String?>(null) }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var friendsCount by remember { mutableStateOf(0) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var recommendedMovies by remember { mutableStateOf<List<String>>(emptyList()) }
    var recommendedMovieObjects by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var userReviews by remember { mutableStateOf<List<Pair<Movie, Review>>>(emptyList()) }

    var friendsList by remember { mutableStateOf<List<Friend>>(emptyList()) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showFriendsSheet by remember { mutableStateOf(false) }
    val language by LanguageManager.language.collectAsState()


    LaunchedEffect(user?.uid) {
        val uid = auth.currentUser?.uid ?: return@LaunchedEffect

        try {
            val doc = db.collection("users").document(uid).get().await()

            username = doc.getString("username")
            name = doc.getString("name")
            imageUrl = doc.getString("photoUrl")
            val friendIds = doc.get("friends") as? List<String> ?: emptyList()
            friendsCount = friendIds.size

            friendsList = friendIds.mapNotNull { friendId ->
                val friend = db.collection("users").document(friendId).get().await()
                Friend(
                    uid = friend.getString("uid") ?: "",
                    name = friend.getString("username") ?: "Usuario",
                    photoUrl = friend.getString("photoUrl") ?: ""
                )
            }

            recommendedMovies = doc.get("recommendedMovies") as? List<String> ?: emptyList()

            if (recommendedMovies.isNotEmpty()) {
                recommendedMovieObjects = recommendedMovies.mapNotNull { movieId ->
                    val movieDoc = db.collection("movies").document(movieId).get().await()
                    val data = movieDoc.data
                    if (data != null) documentToMovie(data, movieDoc.id) else null
                }
            }

            val reviewsSnapshot = db.collectionGroup("reviews")
                .whereEqualTo("userId", currentUid)
                .get()
                .await()

            val reviews = reviewsSnapshot.documents.mapNotNull { doc ->
                doc.toObject(Review::class.java)
            }

            val movieIds = reviews.map { it.movieId }.distinct()

            val moviesSnapshot = db.collection("movies")
                .whereIn(FieldPath.documentId(), movieIds.take(10))
                .get()
                .await()

            val movies = moviesSnapshot.documents.mapNotNull { doc ->
                val data = doc.data
                if (data != null) documentToMovie(data, doc.id) else null
            }

            userReviews = reviews.mapNotNull { review ->
                val movie = movies.find { it.id == review.movieId } ?: return@mapNotNull null
                movie to review
            }

        } catch (e: Exception) {
        }
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_clapper_placeholder),
                        contentDescription = "Logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(12.dp)
                            .size(32.dp)
                    )
                }
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = name ?: "Usuario",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Text(
                            text = username ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )

                        Spacer(Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { navController.navigate(Routes.EditProfile.route) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Editar perfil",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    stringResource(R.string.edit),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                }
            }
            item {
                Text(
                    stringResource(R.string.language),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(Modifier.height(8.dp))

                var expanded by remember { mutableStateOf(false) }
                val currentLang by LanguageManager.language.collectAsState()

                Box {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val flagRes = when (currentLang) {
                                "es" -> R.drawable.spain
                                "en" -> R.drawable.uk
                                "fr" -> R.drawable.france
                                "de" -> R.drawable.germany
                                else -> R.drawable.spain
                            }

                            AsyncImage(
                                model = flagRes,
                                contentDescription = "Bandera",
                                modifier = Modifier.size(28.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(Modifier.width(12.dp))

                            Text(
                                when (currentLang) {
                                    "es" -> stringResource(R.string.spanish)
                                    "en" -> stringResource(R.string.english)
                                    "fr" -> stringResource(R.string.french)
                                    "de" -> stringResource(R.string.german)
                                    else -> stringResource(R.string.language)
                                },
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {

                        LanguageDropdownItem("es", stringResource(R.string.spanish), R.drawable.spain) { expanded = false }
                        LanguageDropdownItem("en", stringResource(R.string.english), R.drawable.uk) { expanded = false }
                        LanguageDropdownItem("fr", stringResource(R.string.french), R.drawable.france) { expanded = false }
                        LanguageDropdownItem("de", stringResource(R.string.german), R.drawable.germany) { expanded = false }
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showFriendsSheet = true }
                            .height(120.dp),
                        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(vertical = 12.dp)
                                .fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Filled.Group,
                                contentDescription = stringResource(R.string.friends),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "$friendsCount",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                stringResource(R.string.friends),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { navController.navigate(Routes.SearchFriends.route) }
                            .height(120.dp),
                        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(vertical = 12.dp)
                                .fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Filled.PersonAdd,
                                contentDescription = stringResource(R.string.search_friends),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                stringResource(R.string.search),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { navController.navigate(Routes.FriendRequests.route) }
                            .height(120.dp),
                        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(vertical = 12.dp)
                                .fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Filled.MailOutline,
                                contentDescription = stringResource(R.string.requests),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                stringResource(R.string.requests),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.recommendations),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.weight(1f))

                    TextButton(onClick = { navController.navigate(Routes.EditRecommendations.route) }) {
                        Text(stringResource(R.string.edit))
                    }
                }

                Spacer(Modifier.height(12.dp))

                if (recommendedMovieObjects.isEmpty()) {
                    Text(
                        stringResource(R.string.no_recommendations),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(recommendedMovieObjects) { movie ->
                            AsyncImage(
                                model = movie.imageUrl,
                                contentDescription = movie.title[language] ?: movie.title["es"].orEmpty(),
                                modifier = Modifier
                                    .width(110.dp)
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { navController.navigate("movieDetail/${movie.id}") },
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
            item {
                Spacer(Modifier.height(24.dp))
                Text(stringResource(R.string.reviews_title), style = MaterialTheme.typography.titleMedium)

                Spacer(Modifier.height(12.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                ) {
                    items(userReviews) { (movie, review) ->
                        ReviewItem(movie, review) {
                            navController.navigate("movieDetail/${movie.id}")
                        }
                    }
                }
            }
            item {
                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text(stringResource(R.string.logout))
                }
            }
        }
        if (showFriendsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFriendsSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {

                Text(
                    stringResource(R.string.friends),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(friendsList) { friend ->
                        FriendRow(
                            friend = friend,
                            onRemove = { friendUid ->
                                scope.launch {
                                    removeFriend(db, currentUid, friendUid)
                                    friendsList = friendsList.filterNot { it.uid == friendUid }
                                    friendsCount -= 1
                                }
                            },
                            onOpenProfile = { friendUid ->
                                navController.navigate(Routes.FriendProfile.create(friendUid))
                            }
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }

    }
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        auth.signOut()
                        navController.navigate(Routes.Auth.route) {
                            popUpTo(Routes.Home.route) { inclusive = true }
                        }
                    }
                ) {
                    Text(stringResource(R.string.yes_logout), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            title = { Text(stringResource(R.string.logout)) },
            text = { Text(stringResource(R.string.logout_confirm)) }
        )
    }
}

@Composable
fun FriendRow(friend: Friend, onRemove: (String) -> Unit, onOpenProfile: (String) -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onOpenProfile(friend.uid) }
    ) {
        AsyncImage(
            model = friend.photoUrl,
            contentDescription = null,
            modifier = Modifier
                .size(55.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.width(12.dp))

        Text(
            friend.name,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )

        TextButton(
            onClick = { showDeleteDialog = true },
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text(stringResource(R.string.delete))
        }
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRemove(friend.uid)
                    }
                ) {
                    Text(stringResource(R.string.yes_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            title = { Text(stringResource(R.string.delete_friend, friend.name)) },
            text = { Text(stringResource(R.string.delete_friend_confirm)) }
        )
    }
}

@Composable
fun ReviewItem(movie: Movie, review: Review, onMovieClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onMovieClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val language by LanguageManager.language.collectAsState()

        AsyncImage(
            model = movie.imageUrl,
            contentDescription = movie.title[language] ?: movie.title["es"].orEmpty(),
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.width(12.dp))

        Column {
            Text(movie.title[language] ?: movie.title["es"].orEmpty(), style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(4.dp))
            Text(
                review.text,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }
}
@Composable
fun LanguageDropdownItem(
    lang: String,
    label: String,
    flagRes: Int,
    onSelect: () -> Unit
) {

    val context = LocalContext.current

    DropdownMenuItem(
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = flagRes,
                    contentDescription = label,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(12.dp))
                Text(label)
            }
        },
        onClick = {
            LanguageManager.setLanguage(lang)
            onSelect()

            val activity = context as Activity
            activity.recreate()
        }
    )
}