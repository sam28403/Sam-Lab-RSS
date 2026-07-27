package cc.samlab.rss.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import cc.samlab.rss.R
import cc.samlab.rss.ui.ext.DataStoreKey
import cc.samlab.rss.ui.ext.DataStoreKey.Companion.aiModel
import cc.samlab.rss.ui.ext.dataStore
import cc.samlab.rss.ui.ext.put

val LocalAiModel = compositionLocalOf { AiModelPreference.default }

data class AiModelPreference(val value: String) : Preference() {

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(aiModel, value)
        }
    }

    fun toDesc(context: Context): String = value.ifEmpty { context.getString(R.string.ai_model_default) }

    companion object {
        val default = AiModelPreference("")

        fun fromPreferences(preferences: Preferences): AiModelPreference {
            return AiModelPreference(
                preferences[DataStoreKey.keys[aiModel]?.key as Preferences.Key<String>] ?: default.value
            )
        }
    }
}
