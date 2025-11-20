import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

object LanguageManager {

    private val Context.dataStore by preferencesDataStore(name = "settings")

    private lateinit var appContext: Context

    private val LANGUAGE_KEY = stringPreferencesKey("app_language")

    private val _language = MutableStateFlow("es")
    val language: StateFlow<String> = _language

    fun init(context: Context) {
        appContext = context.applicationContext

        CoroutineScope(Dispatchers.IO).launch {
            appContext.dataStore.data.collect { prefs ->
                val lang = prefs[LANGUAGE_KEY] ?: "es"
                _language.value = lang
            }
        }
    }

    fun setLanguage(lang: String) {
        CoroutineScope(Dispatchers.IO).launch {
            appContext.dataStore.edit { prefs ->
                prefs[LANGUAGE_KEY] = lang
            }
            _language.value = lang
        }
    }
    fun currentLanguage(): String = _language.value
}
