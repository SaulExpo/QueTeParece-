package com.exmosaul.queteparece.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exmosaul.queteparece.data.auth.MovieRepository
import com.exmosaul.queteparece.data.model.Movie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


data class HomeUiState(
    val featuredMovie: Movie? = null,
    val novedades: List<Movie> = emptyList(),
    val tendencias: List<Movie> = emptyList(),
    val animacion: List<Movie> = emptyList(),
    val liveAction: List<Movie> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class HomeViewModel(
    private val repo: MovieRepository = MovieRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val featured = repo.getFeaturedMovie()
                val novedades = repo.getMoviesByCategory("novedades")
                val tendencias = repo.getMoviesByCategory("tendencias")
                val animacion = repo.getMoviesByType("animada")
                val liveAction = repo.getMoviesByType("live action")
                _uiState.value = HomeUiState(
                    featuredMovie = featured,
                    novedades = novedades,
                    tendencias = tendencias,
                    animacion = animacion,
                    liveAction = liveAction,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }
}
