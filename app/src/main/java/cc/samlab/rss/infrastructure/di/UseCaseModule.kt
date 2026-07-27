package cc.samlab.rss.infrastructure.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import cc.samlab.rss.domain.data.ArticlePagingListUseCase
import cc.samlab.rss.domain.data.DiffMapHolder
import cc.samlab.rss.domain.data.FilterStateUseCase
import cc.samlab.rss.domain.data.GroupWithFeedsListUseCase
import cc.samlab.rss.domain.service.AccountService
import cc.samlab.rss.domain.service.RssService
import cc.samlab.rss.infrastructure.android.AndroidStringsHelper
import cc.samlab.rss.infrastructure.preference.SettingsProvider

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun providesArticlePagingList(
        rssService: RssService,
        androidStringsHelper: AndroidStringsHelper,
        @ApplicationScope applicationScope: CoroutineScope,
        @IODispatcher ioDispatcher: CoroutineDispatcher,
        settingsProvider: SettingsProvider,
        filterStateUseCase: FilterStateUseCase,
        accountService: AccountService,
    ): ArticlePagingListUseCase {
        return ArticlePagingListUseCase(
            rssService,
            androidStringsHelper,
            applicationScope,
            ioDispatcher,
            settingsProvider,
            filterStateUseCase,
            accountService,
        )
    }

    @Provides
    @Singleton
    fun providesGroupWithFeedsList(
        @ApplicationScope applicationScope: CoroutineScope,
        @IODispatcher ioDispatcher: CoroutineDispatcher,
        settingsProvider: SettingsProvider,
        rssService: RssService,
        filterStateUseCase: FilterStateUseCase,
        diffMapHolder: DiffMapHolder,
        accountService: AccountService,
    ): GroupWithFeedsListUseCase {
        return GroupWithFeedsListUseCase(
            applicationScope,
            ioDispatcher,
            settingsProvider,
            rssService,
            filterStateUseCase,
            diffMapHolder,
            accountService,
        )
    }
}
