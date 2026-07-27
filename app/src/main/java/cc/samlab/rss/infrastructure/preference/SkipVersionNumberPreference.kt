package cc.samlab.rss.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import cc.samlab.rss.domain.model.general.Version
import cc.samlab.rss.domain.model.general.toVersion
import cc.samlab.rss.ui.ext.DataStoreKey
import cc.samlab.rss.ui.ext.DataStoreKey.Companion.skipVersionNumber
import cc.samlab.rss.ui.ext.dataStore
import cc.samlab.rss.ui.ext.put

val LocalSkipVersionNumber = compositionLocalOf { SkipVersionNumberPreference.default }

object SkipVersionNumberPreference {

    val default = Version()

    fun put(context: Context, scope: CoroutineScope, value: String) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(DataStoreKey.skipVersionNumber, value)
        }
    }

    fun fromPreferences(preferences: Preferences) =
        preferences[DataStoreKey.keys[skipVersionNumber]?.key as Preferences.Key<String>].toVersion()
}
