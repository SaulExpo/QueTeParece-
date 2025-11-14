package com.exmosaul.queteparece.ui.screens.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.exmosaul.queteparece.R
import com.exmosaul.queteparece.data.model.Movie
import com.exmosaul.queteparece.ui.navigation.BottomNavBar
import com.exmosaul.queteparece.ui.navigation.Routes
import com.exmosaul.queteparece.ui.screens.detail.Review
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
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

    LaunchedEffect(user?.uid) {
        val uid = auth.currentUser?.uid ?: return@LaunchedEffect

        try {
            // 1) Obtener datos del usuario
            val doc = db.collection("users").document(uid).get().await()

            username = doc.getString("username")
            name = doc.getString("name")
            imageUrl = doc.getString("photoUrl")
            val friendIds = doc.get("friends") as? List<String> ?: emptyList()
            friendsCount = friendIds.size

            // 2) Obtener detalles de los amigos
            friendsList = friendIds.mapNotNull { friendId ->
                val friend = db.collection("users").document(friendId).get().await()
                val friendUid = friend.getString("uid") ?: "Usuario"
                val friendName = friend.getString("username") ?: "Usuario"
                val friendPhoto = friend.getString("photoUrl") ?: ""
                Friend(friendUid, friendName, friendPhoto)
            }
            recommendedMovies = doc.get("recommendedMovies") as? List<String> ?: emptyList()

            if (recommendedMovies.isNotEmpty()) {
                recommendedMovieObjects = recommendedMovies.mapNotNull { movieId ->
                    val movieDoc = db.collection("movies").document(movieId).get().await()
                    movieDoc.toObject(Movie::class.java)?.copy(id = movieDoc.id)
                }
            }
            val moviesSnapshot = db.collection("movies").get().await()
            try {
                val reviewsSnapshot = db.collectionGroup("reviews")
                    .whereEqualTo("userId", currentUid)
                    .get()
                    .await()

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

        } catch (e: Exception) {
            println("❌ Error cargando perfil y amigos: ${e.message}")
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
                                    "Editar",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
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
                    // 🔹 Amigos
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
                                contentDescription = "Amigos",
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
                                "Amigos",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // 🔹 Buscar amigos
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
                                contentDescription = "Buscar amigos",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Buscar",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // 🔹 Solicitudes pendientes
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
                                contentDescription = "Solicitudes pendientes",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Solicitudes",
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
                        "Recomendaciones",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.weight(1f))

                    TextButton(onClick = { navController.navigate(Routes.EditRecommendations.route) }) {
                        Text("Editar")
                    }
                }

                Spacer(Modifier.height(12.dp))

                if (recommendedMovieObjects.isEmpty()) {
                    Text(
                        "No has recomendado ninguna película aún.",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(recommendedMovieObjects) { movie ->
                            AsyncImage(
                                model = movie.imageUrl,
                                contentDescription = movie.title,
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
            item {
                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text("Cerrar sesión")
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
                    Text("Sí, salir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Seguro que deseas cerrar tu sesión?") }
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
            Text("Eliminar")
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
                    Text("Sí, eliminarlo", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            },
            title = { Text("Eliminar a " + friend.name) },
            text = { Text("¿Seguro que deseas eliminarlo de amigo?") }
        )
    }
}

suspend fun removeFriend(db: FirebaseFirestore, currentUid: String, friendUid: String) {
    val currentRef = db.collection("users").document(currentUid)
    val friendRef = db.collection("users").document(friendUid)

    // Eliminar de ambas listas
    currentRef.update("friends", FieldValue.arrayRemove(friendUid)).await()
    friendRef.update("friends", FieldValue.arrayRemove(currentUid)).await()
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
        AsyncImage(
            model = movie.imageUrl,
            contentDescription = movie.title,
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.width(12.dp))

        Column {
            Text(movie.title, style = MaterialTheme.typography.titleSmall)
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