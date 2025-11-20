package com.exmosaul.queteparece.ui.screens.actor

import LanguageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.exmosaul.queteparece.R
import com.exmosaul.queteparece.data.model.Movie
import com.exmosaul.queteparece.ui.navigation.BottomNavBar
import com.exmosaul.queteparece.ui.screens.movie.MoviePoster


@Composable
fun ActorDetailScreen(
    actorId: String,
    navController: NavController,
    viewModel: ActorDetailViewModel = viewModel()
) {

    val uiState by viewModel.uiState.collectAsState()
    val currentLang by LanguageManager.language.collectAsState()

    LaunchedEffect(actorId) { viewModel.loadActor(actorId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        },
        bottomBar = { BottomNavBar(navController) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        if (uiState.isLoading) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        uiState.actor?.let { actor ->

            val bioText =
                actor.bio[currentLang]
                    ?: actor.bio["es"]
                    ?: stringResource(R.string.no_bio)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // FOTO + EDAD
                Row(verticalAlignment = Alignment.CenterVertically) {

                    AsyncImage(
                        model = actor.imageUrl,
                        contentDescription = actor.fullName,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(Modifier.width(16.dp))

                    Column {
                        Text(
                            actor.fullName,
                            style = MaterialTheme.typography.headlineSmall
                        )

                        Text(
                            stringResource(R.string.years_old, actor.age),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                Text(bioText, style = MaterialTheme.typography.bodyMedium)

                Text(
                    stringResource(R.string.movies_title),
                    style = MaterialTheme.typography.titleMedium
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.movies) { movie ->
                        MoviePoster(
                            movie = movie,
                            isFavorite = false,
                            onFavoriteClick = {},
                            onClick = {
                                navController.navigate("movieDetail/${movie.id}")
                            }
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun MoviePoster(
    movie: Movie,
    onClick: () -> Unit
) {
    MoviePoster(
        movie = movie,
        isFavorite = false,
        onFavoriteClick = {},
        onClick = onClick
    )
}
