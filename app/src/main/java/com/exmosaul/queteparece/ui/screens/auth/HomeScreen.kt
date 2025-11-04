package com.exmosaul.queteparece.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.exmosaul.queteparece.R
import com.exmosaul.queteparece.data.model.Movie
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.exmosaul.queteparece.ui.navigation.BottomNavBar

@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

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
                item { FeaturedMovieSection(movie = uiState.featuredMovie, navController) }
                item { Section(title = "Novedades destacadas", movies = uiState.novedades, navController) }
                item { Section(title = "En tendencias", movies = uiState.tendencias, navController) }
                item { Section(title = "Películas Animadas", movies = uiState.animacion, navController) }
                item { Section(title = "Películas 'live action'", movies = uiState.liveAction, navController) }
            }
        }
    }
}

@Composable
fun FeaturedMovieSection(movie: Movie?, navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight() // adapta la altura automáticamente al tamaño de la imagen
    ) {
        // Imagen de la película (se ve entera y ocupa todo el ancho)
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(movie?.imageUrl ?: "")
                .crossfade(true)
                .build(),
            contentDescription = movie?.title ?: "Película destacada",
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            contentScale = ContentScale.FillWidth // ⚡ asegura que ocupe todo el ancho sin recortar alto
        )

        // Degradado inferior que fusiona con el fondo de la pantalla
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.background.copy(alpha = 0.9f)
                        ),
                        startY = 600f // cuanto más grande, más suave será el fade
                    )
                )
        )

        // Logo en la esquina superior derecha
        Icon(
            painter = painterResource(id = R.drawable.ic_clapper_placeholder),
            contentDescription = "Logo",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .size(32.dp)
        )

        // Botón de detalles centrado en la parte baja de la imagen
        Button(
            onClick = { movie?.id?.let { id ->
                navController.navigate("movieDetail/$id")
            }},
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Detalles", fontWeight = FontWeight.Bold)
        }
    }
}


@Composable
fun Section(title: String, movies: List<Movie>, navController: NavController) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
            Text("Ver todo", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(movies) { movie ->
                AsyncImage(
                    model = movie.imageUrl,
                    contentDescription = movie.title,
                    modifier = Modifier
                        .width(120.dp)
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            println("➡️ Navegando a detalle de ${movie.title} con ID ${movie.id}")
                            navController.navigate("movieDetail/${movie.id}")
                        },
                    contentScale = ContentScale.Crop
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

