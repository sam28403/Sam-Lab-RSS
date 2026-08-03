package cc.samlab.rss.domain.data

import androidx.compose.ui.util.fastFilteredMap
import androidx.compose.ui.util.fastMap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import cc.samlab.rss.domain.model.general.Filter
import cc.samlab.rss.domain.model.group.GroupWithFeed
import cc.samlab.rss.domain.service.AbstractRssRepository
import cc.samlab.rss.domain.service.AccountService
import cc.samlab.rss.domain.service.RssService
import cc.samlab.rss.infrastructure.di.ApplicationScope
import cc.samlab.rss.infrastructure.di.IODispatcher
import cc.samlab.rss.infrastructure.preference.SettingsProvider
import cc.samlab.rss.ui.ext.getDefaultGroupId
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class GroupWithFeedsListUseCase @Inject constructor(
    @ApplicationScope private val applicationScope: CoroutineScope,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    private val settingsProvider: SettingsProvider,
    private val rssService: RssService,
    private val filterStateUseCase: FilterStateUseCase,
    private val diffMapHolder: DiffMapHolder,
    private val accountService: AccountService,
) {

    init {
        val accountFlow = accountService.currentAccountFlow.mapNotNull { it }

        applicationScope.launch {
            accountFlow.combine(
                filterStateUseCase.filterStateFlow.map { it.filter }.distinctUntilChanged()
            ) { account, filter ->
                account to filter
            }.flatMapLatest { (account, filter) ->
                val service = rssService.get(account.type.id)
                service.pullFeeds().flatMapLatest { feeds ->
                    when (filter) {
                        Filter.Unread -> pullUnreadFeeds(feeds, service)
                        Filter.Starred -> pullStarredFeeds(feeds, service)
                        else -> pullAllFeeds(feeds, service)
                    }
                }
            }.flowOn(ioDispatcher).collect {
                _groupWithFeedsListFlow.value = it
            }
        }
    }

    private val _groupWithFeedsListFlow: MutableStateFlow<List<GroupWithFeed>> =
        MutableStateFlow<List<GroupWithFeed>>(emptyList())
    val groupWithFeedListFlow: StateFlow<List<GroupWithFeed>> = _groupWithFeedsListFlow
        .combine(filterStateUseCase.filterStateFlow.map { it.searchContent }) { list, searchContent ->
            if (searchContent.isNullOrBlank()) list
            else {
                list.mapNotNull { groupWithFeed ->
                    val filteredFeeds = groupWithFeed.feeds.filter { feed ->
                        feed.name.contains(searchContent, ignoreCase = true) ||
                                groupWithFeed.group.name.contains(searchContent, ignoreCase = true)
                    }
                    if (filteredFeeds.isNotEmpty() || groupWithFeed.group.name.contains(searchContent, ignoreCase = true)) {
                        groupWithFeed.copy(feeds = filteredFeeds.toMutableList())
                    } else null
                }
            }
        }.stateIn(applicationScope, SharingStarted.Eagerly, emptyList())


    private val defaultGroupId get() = accountService.getCurrentAccountId().getDefaultGroupId()

    private val hideEmptyGroups get() = settingsProvider.settings.hideEmptyGroups.value

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun pullAllFeeds(
        feeds: List<GroupWithFeed>,
        service: AbstractRssRepository
    ): Flow<List<GroupWithFeed>> {
        val articleCountMapFlow = service.pullImportant(isStarred = false, isUnread = false)

        return articleCountMapFlow.map { articleCountMap ->
            feeds.fastFilteredMap(predicate = {
                it.group.id != defaultGroupId || it.feeds.isNotEmpty()
            }, transform = {
                val feedList = it.feeds.map { feed ->
                    feed.copy(important = articleCountMap[feed.id] ?: 0)
                }
                it.copy(feeds = feedList.toMutableList())
            })
        }
    }

    private fun pullStarredFeeds(
        feeds: List<GroupWithFeed>,
        service: AbstractRssRepository
    ): Flow<List<GroupWithFeed>> {
        val starredCountMapFlow = service.pullImportant(isStarred = true, isUnread = false)

        return starredCountMapFlow.map { starredCountMap ->
            val result = mutableListOf<GroupWithFeed>()
            for (groupItem in feeds) {
                val feedList = groupItem.feeds.fastMap { feed ->
                    val feedCount = (starredCountMap[feed.id] ?: 0)
                    feed.copy(important = feedCount)
                }

                val processedGroupItem = if (hideEmptyGroups) {
                    val filteredFeeds = feedList.filterNot { it.important == 0 }
                    if (filteredFeeds.isEmpty()) {
                        null
                    } else {
                        groupItem.copy(feeds = filteredFeeds.toMutableList())
                    }
                } else {
                    groupItem.copy(feeds = feedList.toMutableList())
                }

                if (processedGroupItem != null && (processedGroupItem.group.id != defaultGroupId || processedGroupItem.feeds.isNotEmpty())) {
                    result.add(processedGroupItem)
                }
            }
            result
        }
    }

    @OptIn(FlowPreview::class)
    private fun pullUnreadFeeds(
        feeds: List<GroupWithFeed>,
        service: AbstractRssRepository
    ): Flow<List<GroupWithFeed>> {
        val unreadCountMapFlow = service.pullImportant(isStarred = false, isUnread = true)
        return unreadCountMapFlow.combine(
            diffMapHolder.diffMapSnapshotFlow
        ) { unreadCountMap, diffMap ->
            val result = mutableListOf<GroupWithFeed>()
            val unreadDiffs = diffMap.values.filter { it.isUnread }
            val readDiffs = diffMap.values.filterNot { it.isUnread }

            for (groupItem in feeds) {
                val feedList = groupItem.feeds.map { feed ->
                    val feedId = feed.id
                    val feedCount = unreadCountMap[feedId] ?: 0
                    val combinedFeedCount =
                        feedCount + unreadDiffs.count { it.feedId == feedId } - readDiffs.count { it.feedId == feedId }
                    feed.copy(important = combinedFeedCount.coerceAtLeast(0))
                }

                val processedGroupItem = if (hideEmptyGroups) {
                    val filteredFeeds = feedList.filterNot { it.important == 0 }
                    if (filteredFeeds.isEmpty()) {
                        null
                    } else {
                        groupItem.copy(feeds = filteredFeeds.toMutableList())
                    }
                } else {
                    groupItem.copy(feeds = feedList.toMutableList())
                }

                if (processedGroupItem != null && (processedGroupItem.group.id != defaultGroupId || processedGroupItem.feeds.isNotEmpty())) {
                    result.add(processedGroupItem)
                }
            }
            result
        }.debounce(200L)
    }

}