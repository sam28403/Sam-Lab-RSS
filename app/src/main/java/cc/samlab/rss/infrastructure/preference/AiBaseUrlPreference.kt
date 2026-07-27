package cc.samlab.rss.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import cc.samlab.rss.R
import cc.samlab.rss.ui.ext.DataStoreKey
import cc.samlab.rss.ui.ext.DataStoreKey.Companion.aiBaseUrl
import cc.samlab.rss.ui.ext.dataStore
import cc.samlab.rss.ui.ext.put

val LocalAiBaseUrl = compositionLocalOf { AiBaseUrlPreference.default }

data class AiBaseUrlPreference(val value: String) : Preference() {

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(aiBaseUrl, value)
        }
    }

    fun toDesc(context: Context): String = value.ifEmpty { context.getString(R.string.ai_base_url_default) }

    companion object {
        val default = AiBaseUrlPreference("")

        fun fromPreferences(preferences: Preferences): AiBaseUrlPreference {
            return AiBaseUrlPreference(
                preferences[DataStoreKey.keys[aiBaseUrl]?.key as Preferences.Key<String>] ?: default.value
            )
        }
    }
}
