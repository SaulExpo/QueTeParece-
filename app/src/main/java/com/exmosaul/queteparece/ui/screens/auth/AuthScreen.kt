package com.exmosaul.queteparece.ui.screens.auth

import LanguageManager
import android.app.Activity
import android.content.ContextWrapper
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.exmosaul.queteparece.R
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AuthScreen(
    viewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onAuthenticated: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showPasswordResetDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    // Navegar si está autenticado
    LaunchedEffect(uiState.user) {
        if (uiState.user != null) {
            onAuthenticated()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            LanguageSelectorDropdown()
        }

        Column(
            horizontalAlignment = CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .align(Alignment.Center)
        ) {

            Spacer(modifier = Modifier.height(20.dp))

            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(
                    if (uiState.mode == AuthMode.SIGN_IN)
                        R.string.auth_title_sign_in
                    else
                        R.string.auth_title_sign_up
                ),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedContent(
                targetState = uiState.mode,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                }
            ) { mode ->

                Column {

                    if (mode == AuthMode.SIGN_UP) {
                        OutlinedTextField(
                            value = uiState.name,
                            onValueChange = viewModel::onNameChange,
                            label = { Text(stringResource(R.string.auth_full_name)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = uiState.username,
                            onValueChange = viewModel::onUsernameChange,
                            label = { Text(stringResource(R.string.auth_username)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = viewModel::onEmailChange,
                        label = { Text(stringResource(R.string.auth_email)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = viewModel::onPasswordChange,
                        label = { Text(stringResource(R.string.auth_password)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.submit() },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = stringResource(
                        if (uiState.mode == AuthMode.SIGN_IN)
                            R.string.auth_sign_in
                        else
                            R.string.auth_sign_up
                    ),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.mode == AuthMode.SIGN_IN) {
                TextButton(onClick = { showPasswordResetDialog = true }) {
                    Text(
                        stringResource(R.string.auth_forgot_password),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = { viewModel.toggleMode() }
            ) {
                Text(
                    text = stringResource(
                        if (uiState.mode == AuthMode.SIGN_IN)
                            R.string.auth_toggle_to_signup
                        else
                            R.string.auth_toggle_to_signin
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.Underline)
                )
            }
            if (uiState.verificationSent) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.auth_verification_email_sent),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            uiState.error?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error
                )
            }

            feedbackMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.primary)
            }
        }
    }

    if (showPasswordResetDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordResetDialog = false },
            confirmButton = {
                TextButton(onClick = {

                    viewModel.sendPasswordReset(resetEmail) { success, error ->

                        feedbackMessage = if (success) {
                            context.getString(R.string.auth_reset_email_sent)
                        } else {
                            context.getString(R.string.auth_reset_email_error)
                        }
                    }

                    showPasswordResetDialog = false
                }) {
                    Text("OK")
                }
            },
            title = { Text(stringResource(R.string.auth_forgot_password)) },
            text = {
                OutlinedTextField(
                    value = resetEmail,
                    onValueChange = { resetEmail = it },
                    label = { Text(stringResource(R.string.auth_email)) }
                )
            }
        )
    }
}

@Composable
private fun textFieldColors() = TextFieldDefaults.colors(
    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    cursorColor = MaterialTheme.colorScheme.primary
)

@Composable
fun LanguageSelectorDropdown(modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val currentLang by LanguageManager.language.collectAsState()

    val activity = currentActivity()

    Box(
        modifier = modifier
            .padding(top = 20.dp, end = 12.dp)
    ) {
        IconButton(onClick = { expanded = true }) {

            val flagRes = when (currentLang) {
                "es" -> R.drawable.spain
                "en" -> R.drawable.uk
                "fr" -> R.drawable.france
                "de" -> R.drawable.germany
                else -> R.drawable.spain
            }

            AsyncImage(
                model = flagRes,
                contentDescription = "Idioma",
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            listOf(
                Triple("es", R.string.spanish, R.drawable.spain),
                Triple("en", R.string.english, R.drawable.uk),
                Triple("fr", R.string.french, R.drawable.france),
                Triple("de", R.string.german, R.drawable.germany)
            ).forEach { (lang, labelRes, flagRes) ->

                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = flagRes,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp).clip(CircleShape)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(stringResource(labelRes))
                        }
                    },
                    onClick = {
                        LanguageManager.setLanguage(lang)
                        expanded = false
                        activity?.recreate()
                    }
                )
            }
        }
    }
}

@Composable
fun currentActivity(): Activity? {
    var context = LocalContext.current
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}
