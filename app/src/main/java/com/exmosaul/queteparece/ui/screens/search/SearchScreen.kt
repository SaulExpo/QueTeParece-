package com.exmosaul.queteparece.ui.screens.search

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.exmosaul.queteparece.R
import com.exmosaul.queteparece.ui.navigation.BottomNavBar


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(navController: NavController, viewModel: SearchViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val uiState by viewModel.uiState.collectAsState()


    Scaffold(
        bottomBar = { BottomNavBar(navController) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
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

            // 🔍 Barra de búsqueda
            item {
                Text(
                    "Buscador",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = uiState.query,
                    onValueChange = viewModel::onQueryChange,
                    placeholder = { Text("Buscar película...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    shape = RoundedCornerShape(25.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                )
            }

            // 🧭 Selección de categoría
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Selecciona una categoría",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(12.dp))

                val categories = listOf("novedades", "tendencias")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    categories.forEach { category ->
                        val isSelected = uiState.selectedGenres.contains(category)
                        val bgColor by animateColorAsState(
                            if (isSelected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                        )
                        val scale by animateFloatAsState(if (isSelected) 1.1f else 1f)

                        Box(
                            modifier = Modifier
                                .scale(scale)
                                .clip(RoundedCornerShape(20.dp))
                                .background(bgColor)
                                .clickable {
                                    // Solo puede haber una categoría activa
                                    viewModel.selectExclusiveCategory(category)
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                category.replaceFirstChar { it.uppercase() },
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )
                        }
                    }
                }
            }

// 🎬 Selección de tipo
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Selecciona el tipo de película",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(12.dp))

                val types = listOf("animada", "live action")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    types.forEach { type ->
                        val isSelected = uiState.selectedGenres.contains(type)
                        val bgColor by animateColorAsState(
                            if (isSelected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                        )
                        val scale by animateFloatAsState(if (isSelected) 1.1f else 1f)

                        Box(
                            modifier = Modifier
                                .scale(scale)
                                .clip(RoundedCornerShape(20.dp))
                                .background(bgColor)
                                .clickable {
                                    // Solo puede haber un tipo activo
                                    viewModel.selectExclusiveType(type)
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                type.replaceFirstChar { it.uppercase() },
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )
                        }
                    }
                }
            }

            // 🎭 Selección de géneros (múltiple, como ya tenías)
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Elige el género de lo que buscas",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(12.dp))

                val genres = listOf(
                    "Acción", "Drama", "Aventura", "Comedia", "Fantasía", "Terror",
                    "Histórica", "Superhéroes", "Biográfica", "Familiar", "Crimen",
                    "Ciencia Ficción", "Psicológico", "Misterio", "Musical", "Infantil",
                    "Suspense", "Sobrenatural", "Deportes", "Social", "Postapocalíptica",
                    "Romance", "Espionaje"
                )

                var expanded by remember { mutableStateOf(false) }
                val visibleGenres = if (expanded) genres else genres.take(3)

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    visibleGenres.forEach { genre ->
                        val isSelected = genre in uiState.selectedGenres
                        val bgColor by animateColorAsState(
                            if (isSelected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                        )
                        val scale by animateFloatAsState(if (isSelected) 1.1f else 1f)

                        Box(
                            modifier = Modifier
                                .scale(scale)
                                .clip(RoundedCornerShape(20.dp))
                                .background(bgColor)
                                .clickable { viewModel.toggleGenre(genre) }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                genre,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )
                        }
                    }

                    // Flecha expandir/contraer
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (expanded) "Mostrar menos" else "Mostrar más",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // 🎬 Resultados
            item {
                Spacer(modifier = Modifier.height(28.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Resultados",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        "Ver todo",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.clickable {
                            navController.navigate("searchViewAll")
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    uiState.query.isBlank() && uiState.selectedGenres.isEmpty() -> {
                        Text(
                            "Aquí aparecerán los resultados",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }

                    uiState.results.isEmpty() -> {
                        Text(
                            "No hay resultados válidos",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }

                    else -> {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(uiState.results) { movie ->
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(movie.imageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = movie.title,
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
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

