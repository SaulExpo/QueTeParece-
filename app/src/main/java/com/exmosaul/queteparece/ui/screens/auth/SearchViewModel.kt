package com.exmosaul.queteparece.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exmosaul.queteparece.data.model.Movie
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class SearchUiState(
    val query: String = "",
    val selectedGenres: Set<String> = emptySet(),
    val results: List<Movie> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class SearchViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState

    private var searchJob: Job? = null

    fun onQueryChange(newQuery: String) {
        _uiState.value = _uiState.value.copy(query = newQuery)
        triggerSearch()
    }

    fun toggleGenre(genre: String) {
        val s = _uiState.value
        val newGenres = if (genre in s.selectedGenres) s.selectedGenres - genre else s.selectedGenres + genre
        _uiState.value = s.copy(selectedGenres = newGenres)
        triggerSearch()
    }

    private fun triggerSearch() {
        // Debounce 300ms
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            searchMovies()
        }
    }
    fun selectExclusiveCategory(category: String) {
        val current = _uiState.value
        val newSelection = if (category in current.selectedGenres) {
            // Si la vuelves a tocar, se desactiva
            current.selectedGenres - category
        } else {
            // Se activa solo esta y se eliminan otras categorías
            (current.selectedGenres - listOf("novedades", "tendencias")) + category
        }
        _uiState.value = current.copy(selectedGenres = newSelection)
        searchMovies()
    }

    fun selectExclusiveType(type: String) {
        val current = _uiState.value
        val newSelection = if (type in current.selectedGenres) {
            current.selectedGenres - type
        } else {
            (current.selectedGenres - listOf("animada", "live action")) + type
        }
        _uiState.value = current.copy(selectedGenres = newSelection)
        searchMovies()
    }

    private fun searchMovies() {
        viewModelScope.launch {
            val s = _uiState.value
            _uiState.value = s.copy(isLoading = true, error = null)

            try {
                val allFilters = s.selectedGenres.toList()
                val categoryFilters = allFilters.filter { it in listOf("novedades", "tendencias") }
                val typeFilters = allFilters.filter { it in listOf("animada", "live action") }
                val genreFilters = allFilters.filter { it !in categoryFilters && it !in typeFilters }

                // 🔍 1️⃣ Cargar todas las películas (hasta 100)
                val snapshot = db.collection("movies").limit(100).get().await()

                val movies = snapshot.documents.mapNotNull { doc ->
                    val id = doc.id
                    val title = doc.getString("title") ?: return@mapNotNull null
                    val description = doc.getString("description") ?: ""
                    val imageUrl = doc.getString("imageUrl") ?: ""
                    val category = doc.getString("category") ?: ""
                    val featured = doc.getBoolean("isFeatured") == true
                    val genres = (doc.get("genres") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                    val type = doc.getString("type") ?: ""
                    val cast = (doc.get("cast") as? List<*>)?.filterIsInstance<String>() ?: emptyList()


                    Movie(id, title, description, imageUrl, category, featured, genres, type, cast)
                }.filter { movie ->
                    // 🔎 2️⃣ Aplicar todos los filtros combinados (modo AND)
                    val matchesQuery = s.query.isBlank() || movie.title.contains(s.query, ignoreCase = true)
                    val matchesCategory = categoryFilters.isEmpty() || movie.category in categoryFilters
                    val matchesType = typeFilters.isEmpty() || movie.type in typeFilters
                    val matchesGenres = genreFilters.isEmpty() || genreFilters.all { it in movie.genres }

                    matchesQuery && matchesCategory && matchesType && matchesGenres
                }

                _uiState.value = s.copy(results = movies, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = s.copy(isLoading = false, error = e.message)
            }
        }
    }


}
