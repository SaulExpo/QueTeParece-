package com.exmosaul.queteparece

import LanguageManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.exmosaul.queteparece.ui.navigation.AppNavHost
import com.exmosaul.queteparece.ui.theme.AppTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context?) {
        val lang = LanguageManager.currentLanguage()
        val localizedContext = LocaleHelper.wrapContext(newBase!!, lang)
        super.attachBaseContext(localizedContext)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        FirebaseApp.initializeApp(this)
        val auth = FirebaseAuth.getInstance()
        super.onCreate(savedInstanceState)
        setContent {
            val lang by LanguageManager.language.collectAsState()

            val localizedContext = remember(lang) {
                LocaleHelper.wrapContext(this, lang)
            }

            CompositionLocalProvider(
                LocalContext provides localizedContext
            ) {
                AppTheme {
                    LaunchedEffect(Unit) {
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val navController = rememberNavController()
                        AppNavHost(navController = navController)
                    }
                }
            }
        }
    }
}






