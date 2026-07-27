package cc.samlab.rss.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import cc.samlab.rss.ui.ext.DataStoreKey
import cc.samlab.rss.ui.ext.DataStoreKey.Companion.readingImageHorizontalPadding
import cc.samlab.rss.ui.ext.dataStore
import cc.samlab.rss.ui.ext.put

val LocalReadingImageHorizontalPadding =
    compositionLocalOf { ReadingImageHorizontalPaddingPreference.default }

object ReadingImageHorizontalPaddingPreference {

    const val default = 24

    fun put(context: Context, scope: CoroutineScope, value: Int) {
        scope.launch {
            context.dataStore.put(DataStoreKey.readingImageHorizontalPadding, value)
        }
    }

    fun fromPreferences(preferences: Preferences) =
        preferences[DataStoreKey.keys[readingImageHorizontalPadding]?.key as Preferences.Key<Int>] ?: default
}
