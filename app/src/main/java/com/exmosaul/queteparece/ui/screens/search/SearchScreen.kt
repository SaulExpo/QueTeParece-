package com.exmosaul.queteparece.ui.screens.search

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.exmosaul.queteparece.R
import com.exmosaul.queteparece.data.model.Movie
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

            val isSearching = uiState.query.isNotBlank()

            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_clapper_placeholder),
                        contentDescription = stringResource(R.string.logo),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(12.dp)
                            .size(32.dp)
                    )
                }
            }

            item {
                Text(
                    stringResource(R.string.search_screen_title),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = uiState.query,
                    onValueChange = viewModel::onQueryChange,
                    placeholder = { Text(stringResource(R.string.search_placeholder)) },
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

            if (isSearching) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))

                    when {
                        uiState.isLoading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        uiState.results.isEmpty() -> {
                            Text(
                                stringResource(R.string.no_results),
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }

                        else -> {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                uiState.results.forEach { movie ->
                                    MovieSearchRow(movie) {
                                        navController.navigate("movieDetail/${movie.id}")
                                    }
                                }
                            }
                        }
                    }
                }

                return@LazyColumn
            }

            item {
                SectionTitle(R.string.select_category)

                val categories = listOf("novedades", "tendencias", "recomendadas")
                val categoryLabels = mapOf(
                    "novedades" to stringResource(R.string.cat_new_releases),
                    "tendencias" to stringResource(R.string.cat_trending),
                    "recomendadas" to stringResource(R.string.cat_recommended)
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    categories.forEach { category ->
                        val isSelected = uiState.selectedGenres.contains(category)
                        val bgColor by animateColorAsState(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                        )
                        val scale by animateFloatAsState(if (isSelected) 1.1f else 1f)

                        CategoryChip(
                            label = categoryLabels[category] ?: category,
                            selected = isSelected,
                            bgColor = bgColor,
                            scale = scale
                        ) {
                            viewModel.selectExclusiveCategory(category)
                        }
                    }
                }
            }

            item {
                SectionTitle(R.string.select_type)

                val types = listOf("animada", "live_action")
                val typeLabels = mapOf(
                    "animada" to stringResource(R.string.type_animated),
                    "live_action" to stringResource(R.string.type_live_action)
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    types.forEach { type ->
                        val isSelected = uiState.selectedGenres.contains(type)
                        val bgColor by animateColorAsState(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                        )
                        val scale by animateFloatAsState(if (isSelected) 1.1f else 1f)

                        CategoryChip(
                            label = typeLabels[type] ?: type,
                            selected = isSelected,
                            bgColor = bgColor,
                            scale = scale
                        ) {
                            viewModel.selectExclusiveType(type)
                        }
                    }
                }
            }

            item {
                SectionTitle(R.string.select_genre)

                val genres = listOf(
                    "Acción", "Drama", "Aventura", "Comedia", "Fantasía", "Terror",
                    "Histórica", "Superhéroes", "Biográfica", "Familiar", "Crimen",
                    "Ciencia_Ficción", "Psicológico", "Misterio", "Musical", "Infantil",
                    "Suspense", "Sobrenatural", "Deportes", "Social", "Postapocalíptica",
                    "Romance", "Espionaje"
                )
                val genreLabels = mapOf(
                    "Acción" to stringResource(R.string.gen_action),
                    "Drama" to stringResource(R.string.gen_drama),
                    "Aventura" to stringResource(R.string.gen_adventure),
                    "Comedia" to stringResource(R.string.gen_comedy),
                    "Fantasía" to stringResource(R.string.gen_fantasy),
                    "Terror" to stringResource(R.string.gen_horror),
                    "Histórica" to stringResource(R.string.gen_historical),
                    "Superhéroes" to stringResource(R.string.gen_superheroes),
                    "Biográfica" to stringResource(R.string.gen_biographical),
                    "Familiar" to stringResource(R.string.gen_family),
                    "Crimen" to stringResource(R.string.gen_crime),
                    "Ciencia_Ficción" to stringResource(R.string.gen_sci_fi),
                    "Psicológico" to stringResource(R.string.gen_psychological),
                    "Misterio" to stringResource(R.string.gen_mystery),
                    "Musical" to stringResource(R.string.gen_musical),
                    "Infantil" to stringResource(R.string.gen_children),
                    "Suspense" to stringResource(R.string.gen_thriller),
                    "Sobrenatural" to stringResource(R.string.gen_supernatural),
                    "Deportes" to stringResource(R.string.gen_sports),
                    "Social" to stringResource(R.string.gen_social),
                    "Postapocalíptica" to stringResource(R.string.gen_post_apoc),
                    "Romance" to stringResource(R.string.gen_romance),
                    "Espionaje" to stringResource(R.string.gen_spy)
                )

                var expanded by remember { mutableStateOf(false) }
                val visibleGenres = if (expanded) genres else genres.take(3)

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    visibleGenres.forEach { genre ->
                        val isSelected = uiState.selectedGenres.contains(genre)
                        val bgColor by animateColorAsState(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                        )
                        val scale by animateFloatAsState(if (isSelected) 1.1f else 1f)

                        CategoryChip(
                            label = genreLabels[genre] ?: genre,
                            selected = isSelected,
                            bgColor = bgColor,
                            scale = scale
                        ) {
                            viewModel.toggleGenre(genre)
                        }
                    }

                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (expanded)
                                stringResource(R.string.show_less)
                            else
                                stringResource(R.string.show_more),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(28.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.results),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        stringResource(R.string.view_all),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable {
                            navController.navigate("searchViewAll")
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                when {
                    uiState.isLoading -> LoadingBox()
                    uiState.query.isBlank() && uiState.selectedGenres.isEmpty() ->
                        SimpleInfoText(R.string.results_will_appear)

                    uiState.results.isEmpty() ->
                        SimpleInfoText(R.string.no_valid_results)

                    else -> {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(uiState.results) { movie ->
                                MoviePosterSmall(movie) {
                                    navController.navigate("movieDetail/${movie.id}")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun SectionTitle(textRes: Int) {
    Spacer(modifier = Modifier.height(24.dp))
    Text(
        stringResource(textRes),
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
    )
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
fun MovieSearchRow(movie: Movie, onClick: () -> Unit) {
    val currentLang by LanguageManager.language.collectAsState()
    val localizedTitle = movie.title[currentLang] ?: movie.title["es"] ?: ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = movie.imageUrl,
            contentDescription = localizedTitle,
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(12.dp))
        Text(
            localizedTitle,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}

@Composable
fun CategoryChip(label: String, selected: Boolean, bgColor: Color, scale: Float, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (selected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
        )
    }
}

@Composable
fun LoadingBox() {
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

@Composable
fun SimpleInfoText(textRes: Int) {
    Text(
        stringResource(textRes),
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
    )
}

@Composable
fun MoviePosterSmall(movie: Movie, onClick: () -> Unit) {
    val currentLang by LanguageManager.language.collectAsState()
    val localizedTitle = movie.title[currentLang] ?: movie.title["es"] ?: ""

    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(movie.imageUrl)
            .crossfade(true)
            .build(),
        contentDescription = localizedTitle,
        modifier = Modifier
            .width(120.dp)
            .height(160.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentScale = ContentScale.Crop
    )
}