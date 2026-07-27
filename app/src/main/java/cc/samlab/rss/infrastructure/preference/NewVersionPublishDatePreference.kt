package cc.samlab.rss.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import cc.samlab.rss.ui.ext.DataStoreKey
import cc.samlab.rss.ui.ext.DataStoreKey.Companion.newVersionPublishDate
import cc.samlab.rss.ui.ext.dataStore
import cc.samlab.rss.ui.ext.put

val LocalNewVersionPublishDate = compositionLocalOf { NewVersionPublishDatePreference.default }

object NewVersionPublishDatePreference {

    const val default = ""

    fun put(context: Context, scope: CoroutineScope, value: String) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(DataStoreKey.newVersionPublishDate, value)
        }
    }

    fun fromPreferences(preferences: Preferences) =
        preferences[DataStoreKey.keys[newVersionPublishDate]?.key as Preferences.Key<String>] ?: default
}
