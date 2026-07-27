package cc.samlab.rss.infrastructure.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import cc.samlab.rss.domain.repository.AccountDao
import cc.samlab.rss.domain.repository.ArticleDao
import cc.samlab.rss.domain.repository.FeedDao
import cc.samlab.rss.domain.repository.GroupDao
import cc.samlab.rss.domain.service.AccountService
import cc.samlab.rss.domain.service.RssService
import cc.samlab.rss.infrastructure.preference.SettingsProvider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AccountServiceModule {
    @Provides
    @Singleton
    fun provideAccountService(
        @ApplicationContext context: Context,
        accountDao: AccountDao,
        groupDao: GroupDao,
        feedDao: FeedDao,
        articleDao: ArticleDao,
        @ApplicationScope coroutineScope: CoroutineScope,
        settingsProvider: SettingsProvider,
    ): AccountService {
        return AccountService(
            context = context,
            accountDao = accountDao,
            groupDao = groupDao,
            feedDao = feedDao,
            articleDao = articleDao,
            coroutineScope = coroutineScope,
            settingsProvider = settingsProvider,
        )
    }
}