package com.exmosaul.queteparece.ui.screens.home

import LanguageManager
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.exmosaul.queteparece.R
import com.exmosaul.queteparece.data.model.Movie
import com.exmosaul.queteparece.ui.navigation.BottomNavBar

@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val language by LanguageManager.language.collectAsState()

    Scaffold(
        bottomBar = { BottomNavBar(navController) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {

                item { FeaturedMovieSection(uiState.featuredMovie, navController, language) }

                item {
                    Section(
                        title = stringResource(R.string.home_section_new),
                        movies = uiState.novedades,
                        language = language,
                        navController = navController,
                        section = "novedades"
                    )
                }

                item {
                    Section(
                        title = stringResource(R.string.home_section_trending),
                        movies = uiState.tendencias,
                        language = language,
                        navController = navController,
                        section = "tendencias"
                    )
                }

                item {
                    Section(
                        title = stringResource(R.string.home_section_recommended),
                        movies = uiState.recomendadas,
                        language = language,
                        navController = navController,
                        section = "recomendadas"
                    )
                }

                item {
                    Section(
                        title = stringResource(R.string.home_section_animation),
                        movies = uiState.animacion,
                        language = language,
                        navController = navController,
                        section = "animacion"
                    )
                }

                item {
                    Section(
                        title = stringResource(R.string.home_section_live_action),
                        movies = uiState.liveAction,
                        language = language,
                        navController = navController,
                        section = "liveAction"
                    )
                }
            }
        }
    }
}

@Composable
fun FeaturedMovieSection(movie: Movie?, navController: NavController, language: String) {

    val localizedTitle = movie?.title?.get(language) ?: movie?.title?.get("es") ?: ""

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(movie?.imageUrl ?: "")
                .crossfade(true)
                .build(),
            contentDescription = localizedTitle,
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.FillWidth
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.background.copy(alpha = 0.9f)
                        ),
                        startY = 600f
                    )
                )
        )

        Icon(
            painter = painterResource(id = R.drawable.ic_clapper_placeholder),
            contentDescription = stringResource(R.string.logo_desc),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .size(32.dp)
        )

        Button(
            onClick = { movie?.id?.let { navController.navigate("movieDetail/$it") } },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(stringResource(R.string.home_featured_button), fontWeight = FontWeight.Bold)
        }
    }
}


@Composable
fun Section(
    title: String,
    movies: List<Movie>,
    language: String,
    navController: NavController,
    section: String
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                stringResource(R.string.see_all),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.clickable {
                    navController.navigate("movieList/$section")
                }
            )
        }

        Spacer(Modifier.height(8.dp))
        val shuffledMovies = remember(movies) { movies.shuffled() }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(shuffledMovies) { movie ->

                val localizedTitle = movie.title[language] ?: movie.title["es"] ?: ""

                AsyncImage(
                    model = movie.imageUrl,
                    contentDescription = localizedTitle,
                    modifier = Modifier
                        .width(120.dp)
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            navController.navigate("movieDetail/${movie.id}")
                        },
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

