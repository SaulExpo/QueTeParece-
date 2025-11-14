package com.exmosaul.queteparece.ui.screens.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.exmosaul.queteparece.R
import com.exmosaul.queteparece.data.auth.MovieRepository
import com.exmosaul.queteparece.data.model.Movie
import com.exmosaul.queteparece.ui.navigation.BottomNavBar
import com.exmosaul.queteparece.ui.screens.profile.removeFriend
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder


@Composable
fun MovieDetailScreen(
    movieId: String,
    navController: NavController,
    viewModel: MovieDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val repo = remember { MovieRepository() }
    var movie by remember { mutableStateOf<Movie?>(null) }
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val user = auth.currentUser
    var username by remember { mutableStateOf<String?>(null) }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var showRatingSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()


    LaunchedEffect(user?.uid) {
        val uid = auth.currentUser?.uid ?: return@LaunchedEffect
        val doc = db.collection("users").document(uid).get().await()
        username = doc.getString("username")
        imageUrl = doc.getString("photoUrl")
    }

    LaunchedEffect(movieId) {
        movie = repo.getMovieById(movieId)
        viewModel.loadMovie(movieId)
    }
    if (movie == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    } else {
        Scaffold(
            bottomBar = { BottomNavBar(navController) },
            containerColor = MaterialTheme.colorScheme.background,

            ) { innerPadding ->
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {

                val movie = uiState.movie
                movie?.let {
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        /*Button(onClick = { viewModel.runActorMovieSync() }) {
                            Text("Sincronizar actores y películas")
                        }*/
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(onClick = {
                                viewModel.toggleFavorite(uiState.movie!!.id)
                            }) {
                                Icon(
                                    imageVector = if (uiState.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                    contentDescription = "Toggle Favorite",
                                    tint = if (uiState.isFavorite) Color.Red else MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.Top) {
                            AsyncImage(
                                model = movie.imageUrl,
                                contentDescription = movie.title,
                                modifier = Modifier
                                    .width(140.dp)
                                    .height(220.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(
                                    movie.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    movie.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                        Column {
                            Text(
                                "Valoración",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row{
                                RatingStars(movie.rating.toDouble())

                                TextButton(onClick = { showRatingSheet = true }) {
                                    Text("Valorar")
                                }
                            }
                        }
                        if (showRatingSheet) {
                            RatingSheet(
                                movie = movie,
                                currentUserRating = uiState.userRating,
                                onDismiss = { showRatingSheet = false },
                                onSubmit = { newRating ->
                                    viewModel.submitUserRating(movie.id, newRating)
                                    showRatingSheet = false
                                }
                            )
                        }

                        Spacer(Modifier.height(24.dp))
                        Text(
                            "Reparto",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(uiState.actors) { actor ->
                                print(actor)
                                Column(horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clickable { navController.navigate("actorDetail/${actor.id}") }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(90.dp) // tamaño fijo garantizado
                                            .clip(RoundedCornerShape(12.dp))
                                    ) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(actor.imageUrl)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = actor.fullName,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.matchParentSize(),
                                            placeholder = painterResource(R.drawable.profile),
                                            error = painterResource(R.drawable.profile)
                                        )
                                    }

                                    Spacer(Modifier.height(6.dp))

                                    Text(
                                        actor.fullName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(24.dp))
                        Text(
                            "Reviews",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Área para nueva reseña
                        Row(verticalAlignment = Alignment.Top) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "Foto de perfil",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.width(8.dp))
                            OutlinedTextField(
                                value = uiState.reviewText,
                                onValueChange = viewModel::onReviewTextChange,
                                placeholder = { Text("Añadir review") },
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(8.dp))
                            Button(onClick = {
                                scope.launch {
                                    viewModel.submitReview(movie.id)
                                }
                            }) {
                                Text("Enviar")
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Reviews
                        uiState.reviews.forEach { review ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        AsyncImage(
                                            model = review.userPhoto,
                                            contentDescription = "Foto de perfil",
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primaryContainer),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            review.username ?: "Usuario",
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                        )
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(review.text)
                                    Spacer(Modifier.height(8.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        ReviewVoteButton(
                                            isSelected = review.likedBy.contains(user?.uid),
                                            emoji = "👍",
                                            count = review.likes,
                                            onClick = {
                                                viewModel.toggleReviewVote(movie.id, review.id, isLike = true)
                                            }
                                        )

                                        ReviewVoteButton(
                                            isSelected = review.dislikedBy.contains(user?.uid),
                                            emoji = "👎",
                                            count = review.dislikes,
                                            onClick = {
                                                viewModel.toggleReviewVote(movie.id, review.id, isLike = false)
                                            }
                                        )
                                    }

                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun RatingStars(rating: Double) {
    val filledStars = (rating / 2).toInt()             // estrellas completas
    val hasHalfStar = (rating % 2) >= 1                // media estrella si sobra 1 punto
    val emptyStars = 5 - filledStars - if (hasHalfStar) 1 else 0

    Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(filledStars) {
            Icon(
                Icons.Filled.Star,
                contentDescription = null,
                tint = Color(0xFFFFD700), // Dorado
                modifier = Modifier.size(22.dp)
            )
        }

        if (hasHalfStar) {
            Icon(
                Icons.Filled.StarHalf,
                contentDescription = null,
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(22.dp)
            )
        }

        repeat(emptyStars) {
            Icon(
                Icons.Outlined.StarBorder,
                contentDescription = null,
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(Modifier.width(8.dp))

        Text(
            text = "${rating.toInt()} / 10",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatingSheet(
    movie: Movie,
    currentUserRating: Int?,
    onDismiss: () -> Unit,
    onSubmit: (Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedRating by remember { mutableStateOf(currentUserRating ?: 0) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.fillMaxWidth()
    ) {

        Text(
            "Valorar ${movie.title}",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )

        Spacer(Modifier.height(12.dp))

        // ⭐ ESTRELLAS DEL 1 AL 10 ⭐
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            (1..10).forEach { i ->
                val filled = i <= selectedRating
                Icon(
                    imageVector = if (filled) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = "$i",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier
                        .size(36.dp)
                        .padding(4.dp)
                        .clickable { selectedRating = i }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Nota numérica
        Text(
            "${selectedRating}/10",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { if (selectedRating > 0) onSubmit(selectedRating) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            enabled = selectedRating > 0
        ) {
            Text("Guardar valoración")
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun ReviewVoteButton(
    isSelected: Boolean,
    emoji: String,
    count: Int,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = emoji,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = count.toString(),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
