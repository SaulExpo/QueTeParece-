package com.exmosaul.queteparece.ui.screens.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.exmosaul.queteparece.data.model.Movie
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.draw.clip

@Composable
fun EditRecommendationsScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser!!
    var movieList by remember { mutableStateOf<List<Movie>>(emptyList()) }
    val selectedMovies = remember { mutableStateListOf<String>() }

    LaunchedEffect(Unit) {
        val snapshot = db.collection("movies").get().await()
        movieList = snapshot.documents.mapNotNull { doc ->
            doc.toObject(Movie::class.java)?.copy(id = doc.id)
        }.sortedBy { it.title.lowercase() }


        val userDoc = db.collection("users").document(user.uid).get().await()
        selectedMovies.clear()
        selectedMovies.addAll(userDoc.get("recommendedMovies") as? List<String> ?: emptyList())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recomendar películas") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    TextButton(onClick = {
                        db.collection("users").document(user.uid)
                            .update("recommendedMovies", selectedMovies.take(4))
                        navController.popBackStack()
                    }) {
                        Text("Guardar")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(movieList) { movie ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (selectedMovies.contains(movie.id)) selectedMovies.remove(movie.id)
                            else if (selectedMovies.size < 4) selectedMovies.add(movie.id)
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        movie.imageUrl,
                        movie.title,
                        Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(movie.title)
                    Spacer(Modifier.weight(1f))
                    Checkbox(
                        checked = selectedMovies.contains(movie.id),
                        onCheckedChange = {
                            if (selectedMovies.contains(movie.id)) selectedMovies.remove(movie.id)
                            else if (selectedMovies.size < 4) selectedMovies.add(movie.id)
                        }
                    )
                }
            }
        }
    }
}
