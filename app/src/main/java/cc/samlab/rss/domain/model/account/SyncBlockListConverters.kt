package cc.samlab.rss.domain.model.account

import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import cc.samlab.rss.infrastructure.preference.SyncBlockList
import cc.samlab.rss.infrastructure.preference.SyncBlockListPreference

/**
 * Provide [TypeConverter] of [SyncBlockListPreference] for [RoomDatabase].
 */
class SyncBlockListConverters {

    @TypeConverter
    fun toBlockList(syncBlockList: String): SyncBlockList =
        SyncBlockListPreference.of(syncBlockList)

    @TypeConverter
    fun fromBlockList(syncBlockList: SyncBlockList?): String =
        SyncBlockListPreference.toString(syncBlockList ?: emptyList())
}
