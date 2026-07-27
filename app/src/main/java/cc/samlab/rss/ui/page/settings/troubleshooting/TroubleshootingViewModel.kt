package cc.samlab.rss.ui.page.settings.troubleshooting

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import cc.samlab.rss.domain.data.Log
import cc.samlab.rss.domain.data.SyncLogger
import cc.samlab.rss.domain.service.AccountService
import cc.samlab.rss.domain.service.OpmlService
import cc.samlab.rss.domain.service.RssService
import cc.samlab.rss.infrastructure.di.ApplicationScope
import cc.samlab.rss.infrastructure.di.DefaultDispatcher
import cc.samlab.rss.infrastructure.di.IODispatcher
import cc.samlab.rss.infrastructure.di.MainDispatcher
import cc.samlab.rss.ui.ext.fromDataStoreToJSONString
import cc.samlab.rss.ui.ext.fromJSONStringToDataStore
import cc.samlab.rss.ui.ext.isProbableProtobuf

@HiltViewModel
class TroubleshootingViewModel
@Inject
constructor(
    private val accountService: AccountService,
    private val rssService: RssService,
    private val opmlService: OpmlService,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher,
    @ApplicationScope private val applicationScope: CoroutineScope,
    val workManager: WorkManager,
    private val syncLogger: SyncLogger,
) : ViewModel() {

    private val _troubleshootingUiState = MutableStateFlow(TroubleshootingUiState())
    val troubleshootingUiState: StateFlow<TroubleshootingUiState> =
        _troubleshootingUiState.asStateFlow()

    fun showWarningDialog() {
        _troubleshootingUiState.update { it.copy(warningDialogVisible = true) }
    }

    fun hideWarningDialog() {
        _troubleshootingUiState.update { it.copy(warningDialogVisible = false) }
    }

    fun tryImport(context: Context, byteArray: ByteArray) {
        importPreferencesFromJSON(context, byteArray)
    }

    fun importPreferencesFromJSON(context: Context, byteArray: ByteArray) {
        viewModelScope.launch(ioDispatcher) { String(byteArray).fromJSONStringToDataStore(context) }
    }

    fun exportPreferencesAsJSON(context: Context, callback: (ByteArray) -> Unit = {}) {
        viewModelScope.launch(ioDispatcher) {
            callback(context.fromDataStoreToJSONString().toByteArray())
        }
    }

    suspend fun getSyncLogs(): List<Log> = syncLogger.list()

    fun clearSyncLogs() = viewModelScope.launch { syncLogger.clear() }
}

data class TroubleshootingUiState(
    val isLoading: Boolean = false,
    val warningDialogVisible: Boolean = false,
)
