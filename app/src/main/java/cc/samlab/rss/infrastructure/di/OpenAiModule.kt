package cc.samlab.rss.infrastructure.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import cc.samlab.rss.domain.repository.AiSummaryRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OpenAiModule {
    @Provides
    @Singleton
    fun provideAiSummaryRepository(): AiSummaryRepository = AiSummaryRepository()
}
