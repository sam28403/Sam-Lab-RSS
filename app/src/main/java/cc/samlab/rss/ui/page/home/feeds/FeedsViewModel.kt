package cc.samlab.rss.ui.page.home.feeds

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import cc.samlab.rss.R
import cc.samlab.rss.domain.model.account.Account
import cc.samlab.rss.domain.model.general.Filter
import cc.samlab.rss.domain.model.general.SyncWarning
import cc.samlab.rss.domain.service.AccountService
import cc.samlab.rss.domain.service.RssService
import cc.samlab.rss.infrastructure.android.AndroidStringsHelper
import cc.samlab.rss.domain.data.DiffMapHolder
import cc.samlab.rss.domain.data.FilterState
import cc.samlab.rss.domain.data.FilterStateUseCase
import cc.samlab.rss.domain.data.GroupWithFeedsListUseCase
import cc.samlab.rss.domain.service.AbstractRssRepository
import cc.samlab.rss.domain.service.SyncWorker
import cc.samlab.rss.infrastructure.android.SystemHelper
import cc.samlab.rss.infrastructure.di.ApplicationScope
import cc.samlab.rss.infrastructure.di.DefaultDispatcher
import cc.samlab.rss.infrastructure.di.IODispatcher
import cc.samlab.rss.infrastructure.preference.SettingsProvider
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

private const val TAG = "FeedsViewModel"

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FeedsViewModel @Inject constructor(
    private val accountService: AccountService,
    private val rssService: RssService,
    private val workManager: WorkManager,
    private val androidStringsHelper: AndroidStringsHelper,
    @DefaultDispatcher
    private val defaultDispatcher: CoroutineDispatcher,
    @IODispatcher
    private val ioDispatcher: CoroutineDispatcher,
    @ApplicationScope
    private val applicationScope: CoroutineScope,
    private val settingsProvider: SettingsProvider,
    private val diffMapHolder: DiffMapHolder,
    private val filterStateUseCase: FilterStateUseCase,
    private val groupWithFeedsListUseCase: GroupWithFeedsListUseCase,
    private val systemHelper: SystemHelper,
) : ViewModel() {

    private val _feedsUiState =
        MutableStateFlow(FeedsUiState())
    val feedsUiState: StateFlow<FeedsUiState> = _feedsUiState.asStateFlow()

    val syncWorkLiveData = workManager.getWorkInfosByTagLiveData(SyncWorker.SYNC_TAG)

    val filterStateFlow = filterStateUseCase.filterStateFlow
    val groupWithFeedsListFlow = groupWithFeedsListUseCase.groupWithFeedListFlow

    fun sync(force: Boolean = false) {
        val account = feedsUiState.value.account
        if (account != null && !force) {
            val isMetered = systemHelper.isMetered()
            val isCharging = systemHelper.isCharging()
            val currentTemp = systemHelper.getBatteryTemperature()
            
            val onlyOnWifi = account.syncOnlyOnWiFi.value
            val onlyWhenCharging = account.syncOnlyWhenCharging.value
            val onlyWhenSafeTemp = account.syncOnlyWhenSafeTemp.value
            val maxTemp = account.syncMaxTemp

            val meteredWarning = onlyOnWifi && isMetered
            val chargingWarning = onlyWhenCharging && !isCharging
            val overheatWarning = onlyWhenSafeTemp && currentTemp > maxTemp

            val warning = when {
                meteredWarning && chargingWarning && overheatWarning -> SyncWarning.All
                meteredWarning && chargingWarning -> SyncWarning.MeteredNotCharging
                meteredWarning && overheatWarning -> SyncWarning.MeteredOverheat
                chargingWarning && overheatWarning -> SyncWarning.NotChargingOverheat
                meteredWarning -> SyncWarning.Metered
                chargingWarning -> SyncWarning.NotCharging
                overheatWarning -> SyncWarning.Overheat
                else -> SyncWarning.None
            }

            if (warning != SyncWarning.None) {
                _feedsUiState.update { it.copy(syncWarning = warning) }
                return
            }
        }

        dismissSyncWarning()
        applicationScope.launch(ioDispatcher) {
            rssService.get().doSyncOneTime()
        }
    }

    fun dismissSyncWarning() {
        _feedsUiState.update { it.copy(syncWarning = SyncWarning.None) }
    }

    fun commitDiffs() = diffMapHolder.commitDiffsToDb()

    fun changeFilter(filterState: FilterState) {
        filterStateUseCase.updateFilterState(filterState)
    }

    fun inputSearchContent(content: String? = null) {
        if (content != filterStateUseCase.filterStateFlow.value.searchContent)
            filterStateUseCase.updateFilterState(searchContent = content)
    }

    init {
        val accountFlow = accountService.currentAccountFlow
        viewModelScope.launch {
            accountFlow.collect { account ->
                _feedsUiState.update { it.copy(account = account) }
            }
        }
        viewModelScope.launch {
            accountFlow.mapNotNull { it }.combine(
                filterStateUseCase.filterStateFlow.map { it.filter }.distinctUntilChanged()
            ) { account, filter ->
                account to filter
            }.flatMapLatest { (account, filter) ->
                val service = rssService.get(account.type.id)
                when (filter) {
                    Filter.Unread -> pullUnreadFeeds(service)
                    Filter.Starred -> pullStarredFeeds(service)
                    else -> pullAllFeeds(service)
                }
            }.flowOn(defaultDispatcher).collect { text ->
                _feedsUiState.update { it.copy(importantSum = text) }
            }
        }
    }

    private fun pullAllFeeds(service: AbstractRssRepository): Flow<String> {
        return service.pullImportant(isStarred = false, isUnread = false)
            .mapLatest {
                val sum = it.values.sum()
                androidStringsHelper.getQuantityString(R.plurals.all_desc, sum, sum)
            }
    }

    private fun pullStarredFeeds(service: AbstractRssRepository): Flow<String> {
        return service.pullImportant(isStarred = true, isUnread = false)
            .mapLatest {
                val sum = it.values.sum()
                androidStringsHelper.getQuantityString(R.plurals.starred_desc, sum, sum)
            }
    }

    @OptIn(FlowPreview::class)
    private fun pullUnreadFeeds(service: AbstractRssRepository): Flow<String> {
        return diffMapHolder.diffMapSnapshotFlow
            .combine(
                service.pullImportant(isStarred = false, isUnread = true)
            ) { diffMap, unreadCountMap ->
                val sum = unreadCountMap.values.sum()
                val combinedSum =
                    sum + diffMap.values.sumOf { if (it.isUnread) 1.toInt() else -1 } // KT-46360
                androidStringsHelper.getQuantityString(
                    R.plurals.unread_desc,
                    combinedSum,
                    combinedSum
                )
            }.debounce(200L)
    }

//    @OptIn(ExperimentalCoroutinesApi::class)
//    fun pullFeeds(filterState: FilterState, hideEmptyGroups: Boolean) {
//        val isStarred = filterState.filter.isStarred()
//        val isUnread = filterState.filter.isUnread()
//        _feedsUiState.update {
//            val important = rssService.get().pullImportant(isStarred, isUnread)
//            it.copy(
////                importantSum = important
////                    .mapLatest {
////                        (it["sum"] ?: 0).run {
////                            androidStringsHelper.getQuantityString(
////                                when {
////                                    isStarred -> R.plurals.starred_desc
////                                    isUnread -> R.plurals.unread_desc
////                                    else -> R.plurals.all_desc
////                                },
////                                this,
////                                this
////                            )
////                        }
////                    }.flowOn(defaultDispatcher),
//                groupWithFeedList = combine(
//                    important,
//                    rssService.get().pullFeeds()
//                ) { importantMap, groupWithFeedList ->
//                    val groupIterator = groupWithFeedList.iterator()
//                    while (groupIterator.hasNext()) {
//                        val groupWithFeed = groupIterator.next()
//                        val groupImportant = importantMap[groupWithFeed.group.id] ?: 0
//                        if (hideEmptyGroups && (isStarred || isUnread) && groupImportant == 0) {
//                            groupIterator.remove()
//                            continue
//                        }
//                        groupWithFeed.group.important = groupImportant
//                        val feedIterator = groupWithFeed.feeds.iterator()
//                        while (feedIterator.hasNext()) {
//                            val feed = feedIterator.next()
//                            val feedImportant = importantMap[feed.id] ?: 0
//                            groupWithFeed.group.feeds++
//                            if (hideEmptyGroups && (isStarred || isUnread) && feedImportant == 0) {
//                                feedIterator.remove()
//                                continue
//                            }
//                            feed.important = feedImportant
//                        }
//                    }
//                    groupWithFeedList
//                }.mapLatest { list ->
//                    list.filter { (group, feeds) ->
//                        group.id != feedsUiState.value.account?.id?.getDefaultGroupId() || feeds.isNotEmpty()
//                    }
//                }.flowOn(defaultDispatcher),
//            )
//        }
//    }
}

data class FeedsUiState(
    val account: Account? = null,
    val importantSum: String = "",
    val listState: LazyListState = LazyListState(),
    val groupsVisible: SnapshotStateMap<String, Boolean> = mutableStateMapOf(),
    val syncWarning: SyncWarning = SyncWarning.None,
)
