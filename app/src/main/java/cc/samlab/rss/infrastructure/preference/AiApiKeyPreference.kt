package cc.samlab.rss.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import cc.samlab.rss.ui.ext.DataStoreKey
import cc.samlab.rss.ui.ext.DataStoreKey.Companion.aiApiKey
import cc.samlab.rss.ui.ext.dataStore
import cc.samlab.rss.ui.ext.put

val LocalAiApiKey = compositionLocalOf { AiApiKeyPreference.default }

data class AiApiKeyPreference(val value: String) : Preference() {

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(aiApiKey, value)
        }
    }

    fun toDesc(context: Context): String = if (value.isNotEmpty()) "••••••••••••" else ""

    companion object {
        val default = AiApiKeyPreference("")

        fun fromPreferences(preferences: Preferences): AiApiKeyPreference {
            return AiApiKeyPreference(
                preferences[DataStoreKey.keys[aiApiKey]?.key as Preferences.Key<String>] ?: default.value
            )
        }
    }
}
