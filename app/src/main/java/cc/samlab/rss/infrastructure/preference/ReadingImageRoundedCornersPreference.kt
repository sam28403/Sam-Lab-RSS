package cc.samlab.rss.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import cc.samlab.rss.ui.ext.DataStoreKey
import cc.samlab.rss.ui.ext.DataStoreKey.Companion.readingImageRoundedCorners
import cc.samlab.rss.ui.ext.dataStore
import cc.samlab.rss.ui.ext.put

val LocalReadingImageRoundedCorners =
    compositionLocalOf { ReadingImageRoundedCornersPreference.default }

object ReadingImageRoundedCornersPreference {

    const val default = 24

    fun put(context: Context, scope: CoroutineScope, value: Int) {
        scope.launch {
            context.dataStore.put(DataStoreKey.readingImageRoundedCorners, value)
        }
    }

    fun fromPreferences(preferences: Preferences) =
        preferences[DataStoreKey.keys[readingImageRoundedCorners]?.key as Preferences.Key<Int>] ?: default
}
