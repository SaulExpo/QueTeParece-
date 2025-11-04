package com.exmosaul.queteparece.ui.screens.auth


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.exmosaul.queteparece.data.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


data class AuthUiState(
    val name: String = "",
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val user: FirebaseUser? = null,
    val mode: AuthMode = AuthMode.SIGN_IN
)


enum class AuthMode { SIGN_IN, SIGN_UP }


class AuthViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {


    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()


    fun onNameChange(v: String) { _uiState.value = _uiState.value.copy(name = v, error = null) }
    fun onUsernameChange(v: String) { _uiState.value = _uiState.value.copy(username = v, error = null) }
    fun onEmailChange(v: String) { _uiState.value = _uiState.value.copy(email = v, error = null) }
    fun onPasswordChange(v: String) { _uiState.value = _uiState.value.copy(password = v, error = null) }
    fun toggleMode() { _uiState.value = _uiState.value.copy(mode = if (_uiState.value.mode==AuthMode.SIGN_IN) AuthMode.SIGN_UP else AuthMode.SIGN_IN, error = null) }


    fun submit() {
        val s = _uiState.value
        if (s.email.isBlank() || s.password.length < 6) {
            _uiState.value = s.copy(error = "Correo o contraseña inválidos (mín. 6 caracteres)")
            return
        }
        _uiState.value = s.copy(isLoading = true, error = null)


        viewModelScope.launch {
            try {
                val user = if (s.mode == AuthMode.SIGN_IN) {
                    repo.signIn(s.email, s.password)
                } else {
                    repo.signUp(s.name, s.username, s.email, s.password)
                }
                _uiState.value = _uiState.value.copy(isLoading = false, user = user)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }
}