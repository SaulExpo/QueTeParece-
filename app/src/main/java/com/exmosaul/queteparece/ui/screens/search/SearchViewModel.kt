package com.exmosaul.queteparece.ui.screens.search

import LanguageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exmosaul.queteparece.data.model.Movie
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
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
        val newGenres =
            if (genre in s.selectedGenres) s.selectedGenres - genre
            else s.selectedGenres + genre

        _uiState.value = s.copy(selectedGenres = newGenres)
        triggerSearch()
    }

    fun selectExclusiveCategory(category: String) {
        val current = _uiState.value
        val newSelection =
            if (category in current.selectedGenres) {
                current.selectedGenres - category
            } else {
                (current.selectedGenres - listOf("novedades", "tendencias", "recomendadas")) + category
            }

        _uiState.value = current.copy(selectedGenres = newSelection)
        triggerSearch()
    }


    fun selectExclusiveType(type: String) {
        val current = _uiState.value
        val newSelection =
            if (type in current.selectedGenres) {
                current.selectedGenres - type
            } else {
                (current.selectedGenres - listOf("animada", "live action")) + type
            }

        _uiState.value = current.copy(selectedGenres = newSelection)
        triggerSearch()
    }

    private fun triggerSearch() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(200)
            searchMovies()
        }
    }

    private fun searchMovies() {
        viewModelScope.launch {

            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val s = _uiState.value

                val filters = s.selectedGenres.toList()
                val categoryFilters = filters.filter { it in listOf("novedades", "tendencias", "recomendadas") }
                val typeFilters = filters.filter { it in listOf("animada", "live action") }
                val genreFilters = filters.filter { it !in categoryFilters && it !in typeFilters }

                val snapshot = db.collection("movies").limit(200).get().await()

                val movies = snapshot.documents.mapNotNull { doc ->
                    val id = doc.id

                    val title = doc.get("title") as? Map<String, String> ?: emptyMap()
                    val description = doc.get("description") as? Map<String, String> ?: emptyMap()

                    val imageUrl = doc.getString("imageUrl") ?: ""
                    val category = doc.getString("category") ?: ""
                    val featured = doc.getBoolean("isFeatured") == true
                    val genres = (doc.get("genres") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                    val type = doc.getString("type") ?: ""

                    Movie(
                        id = id,
                        title = title,
                        description = description,
                        imageUrl = imageUrl,
                        category = category,
                        isFeatured = featured,
                        genres = genres,
                        type = type
                    )
                }
                    .filter { movie ->
                        val state = _uiState.value
                        val lang = LanguageManager.language.value

                        val titleLocalized =
                            movie.title[lang] ?: movie.title["es"] ?: ""

                        val matchesQuery =
                            state.query.isBlank() ||
                                    titleLocalized.contains(state.query, ignoreCase = true)
                        val matchesCategory =
                            categoryFilters.isEmpty() || movie.category in categoryFilters

                        val matchesType =
                            typeFilters.isEmpty() || movie.type in typeFilters

                        val matchesGenres =
                            genreFilters.isEmpty() || genreFilters.all { it in movie.genres }

                        matchesQuery && matchesCategory && matchesType && matchesGenres
                    }

                _uiState.update { current ->
                    current.copy(
                        results = movies,
                        isLoading = false
                    )
                }

            } catch (e: Exception) {
                _uiState.update { current ->
                    current.copy(isLoading = false, error = e.message)
                }
            }
        }
    }
}
