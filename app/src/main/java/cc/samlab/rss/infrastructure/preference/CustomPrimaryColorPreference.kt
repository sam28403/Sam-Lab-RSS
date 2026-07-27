package cc.samlab.rss.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import cc.samlab.rss.ui.ext.DataStoreKey
import cc.samlab.rss.ui.ext.DataStoreKey.Companion.customPrimaryColor
import cc.samlab.rss.ui.ext.dataStore
import cc.samlab.rss.ui.ext.put

val LocalCustomPrimaryColor =
    compositionLocalOf { CustomPrimaryColorPreference.default }

object CustomPrimaryColorPreference {

    const val default = ""

    fun put(context: Context, scope: CoroutineScope, value: String) {
        scope.launch {
            context.dataStore.put(DataStoreKey.customPrimaryColor, value)
        }
    }

    fun fromPreferences(preferences: Preferences) =
        preferences[DataStoreKey.keys[customPrimaryColor]?.key as Preferences.Key<String>] ?: default
}
