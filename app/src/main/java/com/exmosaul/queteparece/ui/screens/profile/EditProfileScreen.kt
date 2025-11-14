package com.exmosaul.queteparece.ui.screens.profile

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavController) {

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val user = auth.currentUser ?: return

    // ================================
    // CAMPOS DE TEXTO
    // ================================
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf(user.photoUrl?.toString() ?: "") }

    var isSaving by remember { mutableStateOf(false) }
    var showPhotoOptions by remember { mutableStateOf(false) }

    // ================================
    // CARGAR DATOS DEL USUARIO
    // ================================
    LaunchedEffect(Unit) {
        val doc = db.collection("users").document(user.uid).get().await()
        name = doc.getString("name") ?: ""
        username = doc.getString("username") ?: ""
        imageUrl = doc.getString("photoUrl") ?: user.photoUrl?.toString() ?: ""
    }

    // ================================
    // LAUNCHERS CÁMARA + GALERÍA
    // ================================
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            uploadUriToCloudinary(
                context = context,
                uri = it,
                userId = user.uid,
                onUploaded = { url ->
                    imageUrl = url
                    updateFirebasePhoto(url, user, db)
                }
            )
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            uploadBitmapToCloudinary(
                context = context,
                bitmap = it,
                userId = user.uid,
                onUploaded = { url ->
                    imageUrl = url
                    updateFirebasePhoto(url, user, db)
                }
            )
        }
    }

    // ================================
    // UI
    // ================================
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Perfil") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
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

            // ================================
            // FOTO + BOTÓN PEQUEÑO EDITAR
            // ================================
            Box(
                modifier = Modifier.size(140.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Foto de perfil",
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
                        contentDescription = "Editar foto",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // OPCIONES DE FOTO
            if (showPhotoOptions) {
                AlertDialog(
                    onDismissRequest = { showPhotoOptions = false },
                    title = { Text("Cambiar foto de perfil") },
                    text = {
                        Column {
                            TextButton(onClick = {
                                galleryLauncher.launch("image/*")
                                showPhotoOptions = false
                            }) { Text("Elegir de la galería") }

                            TextButton(onClick = {
                                cameraLauncher.launch(null)
                                showPhotoOptions = false
                            }) { Text("Tomar con la cámara") }
                        }
                    },
                    confirmButton = {}
                )
            }

            // ================================
            // CAMPOS DEL PERFIL
            // ================================
            Text(
                "Información personal",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )


            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Nombre de usuario") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            // ================================
            // GUARDAR CAMBIOS
            // ================================
            Button(
                onClick = {
                    isSaving = true

                    // Actualizar en FirebaseAuth (solo username)
                    user.updateProfile(
                        userProfileChangeRequest {
                            displayName = username
                        }
                    )

                    // Actualizar Firestore
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
                Text(if (isSaving) "Guardando..." else "Guardar cambios")
            }
        }
    }
}

fun uploadUriToCloudinary(
    context: android.content.Context,
    uri: Uri,
    userId: String,
    onUploaded: (String) -> Unit
) {
    val tempFile = File(context.cacheDir, "img_$userId.jpg")

    context.contentResolver.openInputStream(uri)?.use { input ->
        FileOutputStream(tempFile).use { output ->
            input.copyTo(output)
        }
    }

    uploadFileToCloudinary(tempFile, onUploaded)
}

fun uploadBitmapToCloudinary(
    context: android.content.Context,
    bitmap: Bitmap,
    userId: String,
    onUploaded: (String) -> Unit
) {
    val file = File(context.cacheDir, "photo_$userId.jpg")
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
    }

    uploadFileToCloudinary(file, onUploaded)
}

fun uploadFileToCloudinary(
    file: File,
    onUploaded: (String) -> Unit
) {
    val cloudName = "dhy9na0gx"
    val preset = "QueTeParece"

    val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart(
            "file",
            file.name,
            file.asRequestBody("image/jpeg".toMediaType())
        )
        .addFormDataPart("upload_preset", preset)
        .build()

    val request = Request.Builder()
        .url("https://api.cloudinary.com/v1_1/$cloudName/image/upload")
        .post(requestBody)
        .build()

    OkHttpClient().newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
        }

        override fun onResponse(call: Call, response: Response) {
            response.body?.string()?.let {
                val secureUrl = JSONObject(it).getString("secure_url")
                onUploaded(secureUrl)
            }
        }
    })
}

fun updateFirebasePhoto(url: String, user: FirebaseUser, db: FirebaseFirestore) {
    user.updateProfile(
        userProfileChangeRequest {
            photoUri = Uri.parse(url)
        }
    )

    db.collection("users").document(user.uid)
        .set(mapOf("photoUrl" to url), SetOptions.merge())
}
