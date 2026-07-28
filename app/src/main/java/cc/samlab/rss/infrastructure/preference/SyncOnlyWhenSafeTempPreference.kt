package cc.samlab.rss.infrastructure.preference

import android.content.Context
import cc.samlab.rss.R
import cc.samlab.rss.ui.page.settings.accounts.AccountViewModel

sealed class SyncOnlyWhenSafeTempPreference(
    val value: Boolean,
) {

    object On : SyncOnlyWhenSafeTempPreference(true)
    object Off : SyncOnlyWhenSafeTempPreference(false)

    fun put(accountId: Int, viewModel: AccountViewModel) {
        viewModel.update(accountId) { copy(syncOnlyWhenSafeTemp = this@SyncOnlyWhenSafeTempPreference) }
    }

    fun toDesc(context: Context): String =
        when (this) {
            On -> context.getString(R.string.on)
            Off -> context.getString(R.string.off)
        }

    companion object {

        val default = On
        val values = listOf(On, Off)
    }
}

operator fun SyncOnlyWhenSafeTempPreference.not(): SyncOnlyWhenSafeTempPreference =
    when (value) {
        true -> SyncOnlyWhenSafeTempPreference.Off
        false -> SyncOnlyWhenSafeTempPreference.On
    }
