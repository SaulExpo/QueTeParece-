package com.exmosaul.queteparece.ui.screens.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.exmosaul.queteparece.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavController) {

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val user = auth.currentUser ?: return

    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf(user.photoUrl?.toString() ?: "") }

    var isSaving by remember { mutableStateOf(false) }
    var showPhotoOptions by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val doc = db.collection("users").document(user.uid).get().await()
        name = doc.getString("name") ?: ""
        username = doc.getString("username") ?: ""
        imageUrl = doc.getString("photoUrl") ?: user.photoUrl?.toString() ?: ""
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            uploadUriToCloudinary(
                context,
                it,
                user.uid
            ) { url ->
                imageUrl = url
                updateFirebasePhoto(url, user, db)
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            uploadBitmapToCloudinary(
                context,
                it,
                user.uid
            ) { url ->
                imageUrl = url
                updateFirebasePhoto(url, user, db)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_profile_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {

            Box(
                modifier = Modifier.size(140.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = stringResource(R.string.edit_profile_title),
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                IconButton(
                    onClick = { showPhotoOptions = true },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = stringResource(R.string.change_profile_photo),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (showPhotoOptions) {
                AlertDialog(
                    onDismissRequest = { showPhotoOptions = false },
                    title = { Text(stringResource(R.string.change_profile_photo)) },
                    text = {
                        Column {
                            TextButton(onClick = {
                                galleryLauncher.launch("image/*")
                                showPhotoOptions = false
                            }) {
                                Text(stringResource(R.string.choose_gallery))
                            }
                            TextButton(onClick = {
                                cameraLauncher.launch(null)
                                showPhotoOptions = false
                            }) {
                                Text(stringResource(R.string.take_photo))
                            }
                        }
                    },
                    confirmButton = {}
                )
            }

            Text(
                stringResource(R.string.personal_info),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.name)) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text(stringResource(R.string.username)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = {
                    isSaving = true

                    user.updateProfile(
                        userProfileChangeRequest {
                            displayName = username
                        }
                    )

                    val data = mapOf(
                        "name" to name,
                        "username" to username,
                        "photoUrl" to imageUrl
                    )

                    db.collection("users").document(user.uid)
                        .set(data, SetOptions.merge())
                        .addOnSuccessListener {
                            isSaving = false
                            navController.popBackStack()
                        }
                },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(
                    if (isSaving)
                        stringResource(R.string.saving)
                    else
                        stringResource(R.string.save_changes)
                )
            }
        }
    }
}