package com.exmosaul.queteparece.ui.screens.actor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exmosaul.queteparece.data.auth.ActorRepository
import com.exmosaul.queteparece.data.model.Movie
import com.exmosaul.queteparece.data.model.Actor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ActorDetailViewModel(
    private val repo: ActorRepository = ActorRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActorDetailUiState())
    val uiState: StateFlow<ActorDetailUiState> = _uiState

    fun loadActor(actorId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val actor = repo.getActorById(actorId)
            val movies = repo.getMoviesByActor(actorId)

            _uiState.value = ActorDetailUiState(
                actor = actor,
                movies = movies,
                isLoading = false
            )
        }
    }
}

data class ActorDetailUiState(
    val actor: Actor? = null,
    val movies: List<Movie> = emptyList(),
    val isLoading: Boolean = false
)

