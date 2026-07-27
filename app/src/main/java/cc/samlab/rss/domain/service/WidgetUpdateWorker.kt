package cc.samlab.rss.domain.service

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit
import cc.samlab.rss.infrastructure.preference.SyncIntervalPreference
import cc.samlab.rss.infrastructure.preference.SyncOnlyOnWiFiPreference
import cc.samlab.rss.infrastructure.preference.SyncOnlyWhenChargingPreference
import cc.samlab.rss.ui.widget.ArticleCardWidget
import cc.samlab.rss.ui.widget.ArticleCardWidgetReceiver
import cc.samlab.rss.ui.widget.ArticleListWidget
import cc.samlab.rss.ui.widget.ArticleListWidgetReceiver
import cc.samlab.rss.ui.widget.WidgetRepository

@HiltWorker
class WidgetUpdateWorker
@AssistedInject
constructor(
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val repository: WidgetRepository,
) : CoroutineWorker(context, workerParams) {
    var haveSetPreviews = false

    override suspend fun doWork(): Result {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            generatePreviews()
        }

        ArticleListWidget().updateAll(applicationContext)
        ArticleCardWidget().updateAll(applicationContext)
        return Result.success()
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    private suspend fun generatePreviews() {
        if (haveSetPreviews) return
        val glanceManager = GlanceAppWidgetManager(context)
        val list = glanceManager.setWidgetPreviews(ArticleCardWidgetReceiver::class) == GlanceAppWidgetManager.SET_WIDGET_PREVIEWS_RESULT_SUCCESS
        val card = glanceManager.setWidgetPreviews(ArticleListWidgetReceiver::class) == GlanceAppWidgetManager.SET_WIDGET_PREVIEWS_RESULT_SUCCESS
        haveSetPreviews = list and card
    }

    companion object {
        private const val WORK_NAME_PERIODIC = "WidgetUpdateWorker"

        fun enqueueOneTimeWork(workManager: WorkManager) =
            workManager.enqueue(OneTimeWorkRequestBuilder<WidgetUpdateWorker>().build())

        fun enqueuePeriodicWork(
            workManager: WorkManager,
            syncInterval: SyncIntervalPreference,
            syncOnlyWhenCharging: SyncOnlyWhenChargingPreference,
            syncOnlyOnWiFi: SyncOnlyOnWiFiPreference,
        ) {
            workManager.enqueueUniquePeriodicWork(
                WORK_NAME_PERIODIC,
                ExistingPeriodicWorkPolicy.UPDATE,
                PeriodicWorkRequestBuilder<WidgetUpdateWorker>(syncInterval.value, TimeUnit.MINUTES)
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiresCharging(syncOnlyWhenCharging.value)
                            .setRequiredNetworkType(
                                if (syncOnlyOnWiFi.value) NetworkType.UNMETERED
                                else NetworkType.CONNECTED
                            )
                            .build()
                    )
                    .setInitialDelay(syncInterval.value, TimeUnit.MINUTES)
                    .build(),
            )
        }

        fun cancelPeriodicWork(workManager: WorkManager) =
            workManager.cancelUniqueWork(WORK_NAME_PERIODIC)
    }
}
