package com.exmosaul.queteparece.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.exmosaul.queteparece.data.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth

data class AuthUiState(
    val name: String = "",
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val user: FirebaseUser? = null,
    val mode: AuthMode = AuthMode.SIGN_IN,
    val verificationSent: Boolean = false
)

enum class AuthMode { SIGN_IN, SIGN_UP }

class AuthViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()


    fun onNameChange(v: String) { _uiState.value = _uiState.value.copy(name = v, error = null) }
    fun onUsernameChange(v: String) { _uiState.value = _uiState.value.copy(username = v, error = null) }
    fun onEmailChange(v: String) { _uiState.value = _uiState.value.copy(email = v, error = null) }
    fun onPasswordChange(v: String) { _uiState.value = _uiState.value.copy(password = v, error = null) }

    fun toggleMode() {
        _uiState.value = _uiState.value.copy(
            mode = if (_uiState.value.mode == AuthMode.SIGN_IN) AuthMode.SIGN_UP else AuthMode.SIGN_IN,
            error = null,
            verificationSent = false
        )
    }

    fun submit() {
        val s = _uiState.value

        if (s.email.isBlank() || s.password.length < 6) {
            _uiState.value = s.copy(error = "invalid_credentials")
            return
        }

        _uiState.value = s.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                if (s.mode == AuthMode.SIGN_IN) {
                    val user = repo.signIn(s.email, s.password)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        user = user
                    )

                } else {
                    repo.signUp(s.name, s.username, s.email, s.password)

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        verificationSent = true,
                        user = null // No iniciamos sesión
                    )
                }
            } catch (e: Exception) {

                val message = when (e.message) {
                    "EMAIL_NOT_VERIFIED" -> "email_not_verified"
                    "The email address is badly formatted." -> "invalid_email_format"
                    else -> e.message ?: "unknown_error"
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = message
                )
            }
        }
    }

    fun sendPasswordReset(email: String, onResult: (Boolean, String?) -> Unit) {
        val trimmed = email.trim()

        if (trimmed.isBlank()) {
            onResult(false, "empty_email")
            return
        }

        auth.sendPasswordResetEmail(trimmed)
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { e ->
                onResult(false, e.message ?: "unknown_error")
            }
    }
}
