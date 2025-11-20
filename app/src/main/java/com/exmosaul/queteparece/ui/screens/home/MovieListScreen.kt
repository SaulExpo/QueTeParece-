package com.exmosaul.queteparece.ui.screens.home

import LanguageManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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

@Composable
fun MovieListScreen(
    navController: NavController,
    title: String,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val language by LanguageManager.language.collectAsState()

    val movies = when (title) {
        "novedades" -> uiState.novedades
        "tendencias" -> uiState.tendencias
        "recomendadas" -> uiState.recomendadas
        "animacion" -> uiState.animacion
        "liveAction" -> uiState.liveAction
        else -> emptyList()
    }

    val screenTitle = when (title) {
        "novedades" -> stringResource(R.string.home_section_new)
        "tendencias" -> stringResource(R.string.home_section_trending)
        "recomendadas" -> stringResource(R.string.home_section_recommended)
        "animacion" -> stringResource(R.string.home_section_animation)
        "liveAction" -> stringResource(R.string.home_section_live_action)
        else -> ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            items(movies) { movie ->

                val localizedTitle = movie.title[language]
                    ?: movie.title["es"]
                    ?: ""

                val localizedDescription = movie.description[language]
                    ?: movie.description["es"]
                    ?: ""

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("movieDetail/${movie.id}") }
                ) {
                    AsyncImage(
                        model = movie.imageUrl,
                        contentDescription = localizedTitle,
                        modifier = Modifier
                            .width(100.dp)
                            .height(150.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(Modifier.width(12.dp))

                    Column(
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Text(
                            localizedTitle,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = localizedDescription.take(90) + "...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}
