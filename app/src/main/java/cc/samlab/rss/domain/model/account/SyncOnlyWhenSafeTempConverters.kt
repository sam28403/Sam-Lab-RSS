package cc.samlab.rss.domain.model.account

import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import cc.samlab.rss.infrastructure.preference.SyncOnlyWhenSafeTempPreference

/**
 * Provide [TypeConverter] of [SyncOnlyWhenSafeTempPreference] for [RoomDatabase].
 */
class SyncOnlyWhenSafeTempConverters {

    @TypeConverter
    fun toSyncOnlyWhenSafeTemp(syncOnlyWhenSafeTemp: Boolean): SyncOnlyWhenSafeTempPreference {
        return SyncOnlyWhenSafeTempPreference.values.find { it.value == syncOnlyWhenSafeTemp }
            ?: SyncOnlyWhenSafeTempPreference.default
    }

    @TypeConverter
    fun fromSyncOnlyWhenSafeTemp(syncOnlyWhenSafeTemp: SyncOnlyWhenSafeTempPreference): Boolean {
        return syncOnlyWhenSafeTemp.value
    }
}
