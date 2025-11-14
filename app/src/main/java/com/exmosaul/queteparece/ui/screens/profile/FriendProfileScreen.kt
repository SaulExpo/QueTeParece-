package com.exmosaul.queteparece.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.exmosaul.queteparece.data.model.Movie
import com.exmosaul.queteparece.ui.screens.actor.MoviePoster
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import com.exmosaul.queteparece.ui.navigation.Routes
import com.exmosaul.queteparece.ui.screens.detail.Review
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue

enum class FriendStatus {
    NONE,          // No hay amistad ni solicitud
    REQUEST_SENT, // Hay solicitud en cualquier dirección
    FRIENDS        // Ya son amigos
}


@Composable
fun FriendProfileScreen(navController: NavController, friendId: String) {
    val db = FirebaseFirestore.getInstance()

    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid

    var isFriend by remember { mutableStateOf(false) }
    var friendCount by remember { mutableStateOf(0) }
    var friendsList by remember { mutableStateOf<List<Friend>>(emptyList()) }
    var showFriendsSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)


    var username by remember { mutableStateOf<String?>(null) }
    var name by remember { mutableStateOf<String?>(null) }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var recommendedMovies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var userReviews by remember { mutableStateOf<List<Pair<Movie, Review>>>(emptyList()) }


    var friendStatus by remember { mutableStateOf(FriendStatus.NONE) }


    LaunchedEffect(friendId) {
        val uid = currentUserId ?: return@LaunchedEffect
        val doc = db.collection("users").document(friendId).get().await()

        username = doc.getString("username")
        name = doc.getString("name")
        imageUrl = doc.getString("photoUrl")

        val targetFriendRequests = doc.get("friendRequests") as? List<String> ?: emptyList()
        friendStatus = if (uid in targetFriendRequests) FriendStatus.REQUEST_SENT else FriendStatus.NONE


        val movieIds = doc.get("recommendedMovies") as? List<String> ?: emptyList()
        val moviesSnapshot = db.collection("movies")
            .whereIn(FieldPath.documentId(), movieIds.ifEmpty { listOf("dummy") })
            .get().await()
        recommendedMovies = moviesSnapshot.documents.mapNotNull { it.toObject(Movie::class.java)?.copy(id = it.id) }

        val friendIds = doc.get("friends") as? List<String> ?: emptyList()
        friendCount = friendIds.size

        // 2) Obtener detalles de los amigos
        friendsList = friendIds.mapNotNull { friendId ->
            val friend = db.collection("users").document(friendId).get().await()
            val friendUid = friend.getString("uid") ?: "Usuario"
            val friendName = friend.getString("username") ?: "Usuario"
            val friendPhoto = friend.getString("photoUrl") ?: ""
            Friend(friendUid, friendName, friendPhoto)
        }
        val friendList = doc.get("friends") as? List<String> ?: emptyList()
        println(friendList)
        if (uid in friendList) {
            friendStatus = FriendStatus.FRIENDS
            isFriend = true
            return@LaunchedEffect
        }

        try {
            val reviewsSnapshot = db.collectionGroup("reviews")
                .whereEqualTo("userId", friendId)
                .get()
                .await()

            if (reviewsSnapshot.isEmpty) {
                userReviews = emptyList()
                return@LaunchedEffect
            }
            val reviews = reviewsSnapshot.documents.mapNotNull { doc ->
                val review = doc.toObject(Review::class.java) ?: return@mapNotNull null
                review
            }

            // Cargar películas (en lote)
            val movieIds = reviews.map { it.movieId }.distinct()
            val moviesSnapshot = db.collection("movies")
                .whereIn(FieldPath.documentId(), movieIds.take(10)) // Firestore permite máx 10 por whereIn
                .get()
                .await()

            val movies = moviesSnapshot.documents.mapNotNull { doc ->
                doc.toObject(Movie::class.java)?.copy(id = doc.id)
            }

            // Relacionar movie con review
            userReviews = reviews.mapNotNull { review ->
                val movie = movies.find { it.id == review.movieId } ?: return@mapNotNull null
                movie to review
            }

        } catch (e: Exception) {
            println("❌ Error cargando reseñas: ${e.message}")
        }

    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(username ?: "") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(name ?: "Usuario", style = MaterialTheme.typography.titleLarge)
                    Text(username ?: "", style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (currentUserId != null && currentUserId != friendId) {
                when (friendStatus) {

                    FriendStatus.NONE -> Button(
                        onClick = {
                            db.collection("users").document(friendId)
                                .update("friendRequests", FieldValue.arrayUnion(currentUserId))
                            friendStatus = FriendStatus.REQUEST_SENT
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
                    ) { Text("Enviar solicitud") }

                    FriendStatus.REQUEST_SENT -> Button(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.surfaceVariant)
                    ) { Text("Solicitud enviada") }

                    FriendStatus.FRIENDS -> Button(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Amigos") }
                }
            }

            Text("Amigos ($friendCount)", style = MaterialTheme.typography.titleMedium)

            if (isFriend) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showFriendsSheet = true },
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(Modifier.width(12.dp))
                        Text("Amigos", style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.weight(1f))
                        Text("$friendCount", style = MaterialTheme.typography.titleMedium)
                    }
                }
            } else {
                Text(
                    "Añádelo como amigo para ver a quién sigue.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }


            Text("Recomendaciones", style = MaterialTheme.typography.titleMedium)

            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(recommendedMovies) { movie ->
                    MoviePoster(movie = movie, onClick = {
                        navController.navigate("movieDetail/${movie.id}")
                    })
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
                        "Amigos",
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
                            FriendRowScreen(friend) {
                                println(friend.uid)
                                navController.navigate(Routes.FriendProfile.create(friend.uid)) // friend.first = UID
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }
            Spacer(Modifier.height(24.dp))
            Text("Reseñas", style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp) // ✅ Hace la sección scrolleable sin romper layout
            ) {
                items(userReviews) { (movie, review) ->
                    ReviewItem(movie, review) {
                        navController.navigate("movieDetail/${movie.id}")
                    }
                }
            }
        }
    }
}

@Composable
fun FriendRowScreen(friend: Friend, onClick: () -> Unit) {
    val (uid, name, photoUrl) = friend

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 6.dp)
    ) {
        AsyncImage(
            model = photoUrl,
            contentDescription = null,
            modifier = Modifier
                .size(55.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(12.dp))
        Text(name, style = MaterialTheme.typography.titleMedium)
    }
}