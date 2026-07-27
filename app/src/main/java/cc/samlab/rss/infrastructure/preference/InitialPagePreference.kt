package cc.samlab.rss.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import cc.samlab.rss.R
import cc.samlab.rss.ui.ext.DataStoreKey
import cc.samlab.rss.ui.ext.DataStoreKey.Companion.initialPage
import cc.samlab.rss.ui.ext.dataStore
import cc.samlab.rss.ui.ext.put

val LocalInitialPage = compositionLocalOf<InitialPagePreference> { InitialPagePreference.default }

sealed class InitialPagePreference(val value: Int) : Preference() {
    object FeedsPage : InitialPagePreference(0)
    object FlowPage : InitialPagePreference(1)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(
                DataStoreKey.initialPage,
                value
            )
        }
    }

    fun toDesc(context: Context): String =
        when (this) {
            FeedsPage -> context.getString(R.string.feeds_page)
            FlowPage -> context.getString(R.string.flow_page)
        }

    companion object {

        val default = FeedsPage
        val values = listOf(FeedsPage, FlowPage)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKey.keys[initialPage]?.key as Preferences.Key<Int>]) {
                0 -> FeedsPage
                1 -> FlowPage
                else -> default
            }
    }
}
