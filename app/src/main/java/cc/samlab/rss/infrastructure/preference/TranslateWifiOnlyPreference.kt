package cc.samlab.rss.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import cc.samlab.rss.ui.ext.DataStoreKey
import cc.samlab.rss.ui.ext.DataStoreKey.Companion.translateWifiOnly
import cc.samlab.rss.ui.ext.dataStore
import cc.samlab.rss.ui.ext.put

val LocalTranslateWifiOnly =
    compositionLocalOf<TranslateWifiOnlyPreference> { TranslateWifiOnlyPreference.default }

sealed class TranslateWifiOnlyPreference(val value: Boolean) : Preference() {
    data object ON : TranslateWifiOnlyPreference(true)
    data object OFF : TranslateWifiOnlyPreference(false)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(translateWifiOnly, value)
        }
    }

    companion object {

        val default = ON
        val values = listOf(ON, OFF)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKey.keys[translateWifiOnly]?.key as Preferences.Key<Boolean>]) {
                true -> ON
                false -> OFF
                else -> default
            }
    }
}

operator fun TranslateWifiOnlyPreference.not(): TranslateWifiOnlyPreference =
    when (value) {
        true -> TranslateWifiOnlyPreference.OFF
        false -> TranslateWifiOnlyPreference.ON
    }
