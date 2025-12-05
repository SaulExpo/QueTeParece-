package com.exmosaul.queteparece.ui.screens.detail

import LanguageManager
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.exmosaul.queteparece.R
import com.exmosaul.queteparece.data.auth.MovieRepository
import com.exmosaul.queteparece.data.model.Movie
import com.exmosaul.queteparece.ui.navigation.BottomNavBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


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
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val language by LanguageManager.language.collectAsState()

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
        val localizedTitle = movie?.title[language] ?: movie?.title["es"] ?: ""
        val localizedDescription = movie?.description[language] ?: movie?.description["es"] ?: ""
        Scaffold(
            topBar = {
                TopAppBar(

                    title = { Text(localizedTitle) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                        }
                    }
                )
            },
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
                                contentDescription = localizedTitle,
                                modifier = Modifier
                                    .width(140.dp)
                                    .height(220.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(
                                    localizedTitle,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    localizedDescription,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }

                        val context = LocalContext.current

                        movie.trailer?.let { trailerUrl ->
                            Button(
                                onClick = {
                                    val url = trailerUrl

                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "No se pudo abrir el trailer", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.watch_trailer))
                            }
                        }
                        Column {
                            Text(
                                stringResource(R.string.rating_title),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row{
                                RatingStars(movie.rating.toDouble())

                                TextButton(onClick = { showRatingSheet = true }) {
                                    Text(stringResource(R.string.rate_button))
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
                                },
                                title = localizedTitle
                            )
                        }
                        movie.platforms?.let { platforms ->
                            if (platforms.isNotEmpty()) {
                                Spacer(Modifier.height(24.dp))

                                Text(
                                    stringResource(R.string.where_to_watch),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Spacer(Modifier.height(12.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    platforms.forEach { platform ->
                                        val logo = when (platform.lowercase()) {
                                            "netflix" -> R.drawable.netflix
                                            "prime" -> R.drawable.prime
                                            "hbo" -> R.drawable.hbo
                                            "disney" -> R.drawable.disney
                                            "movistar" -> R.drawable.movistar
                                            "apple" -> R.drawable.apple_tv
                                            else -> null
                                        }

                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.Black.copy(alpha = 0.1f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (logo != null) {
                                                AsyncImage(
                                                    model = logo,
                                                    contentDescription = platform,
                                                    modifier = Modifier
                                                        .fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                        if (movie.type.lowercase() != "animada") {
                            Text(
                                stringResource(R.string.cast_title),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(uiState.actors) { actor ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clickable { navController.navigate("actorDetail/${actor.id}") }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(90.dp)
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
                        }
                        Spacer(Modifier.height(24.dp))
                        Text(
                            stringResource(R.string.reviews_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
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
                                placeholder = { Text(stringResource(R.string.add_review_placeholder)) },
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(8.dp))
                            Button(onClick = {
                                scope.launch {
                                    viewModel.submitReview(movie.id)
                                }
                            }) {
                                Text(stringResource(R.string.send))
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        uiState.reviews.forEach { review ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Box{
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
                                    if (review.userId == user?.uid) {
                                        IconButton(
                                            onClick = {
                                                showDeleteDialog = review.id
                                            },
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Delete,
                                                contentDescription = stringResource(R.string.delete_review),
                                                tint = Color.Red
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        if (showDeleteDialog != null) {
                            AlertDialog(
                                onDismissRequest = { showDeleteDialog = null },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            viewModel.deleteReview(movie.id, showDeleteDialog!!)
                                            showDeleteDialog = null
                                        }
                                    ) {
                                        Text(stringResource(R.string.yes_delete), color = MaterialTheme.colorScheme.error)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDeleteDialog = null }) {
                                        Text(stringResource(R.string.cancel))
                                    }
                                },
                                title = { Text(stringResource(R.string.delete_review)) },
                                text = { Text(stringResource(R.string.delete_review_confirm)) }
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun RatingStars(rating: Double) {
    val filledStars = (rating / 2).toInt()
    val hasHalfStar = (rating % 2) >= 1
    val emptyStars = 5 - filledStars - if (hasHalfStar) 1 else 0

    Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(filledStars) {
            Icon(
                Icons.Filled.Star,
                contentDescription = null,
                tint = Color(0xFFFFD700),
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
    onSubmit: (Int) -> Unit,
    title: String
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedRating by remember { mutableStateOf(currentUserRating ?: 0) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.fillMaxWidth()
    ) {

        Text(
            stringResource(R.string.rate_button) + " " + title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )

        Spacer(Modifier.height(12.dp))
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


fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        if (context.baseContext === context) break
        context = context.baseContext
    }
    return null
}