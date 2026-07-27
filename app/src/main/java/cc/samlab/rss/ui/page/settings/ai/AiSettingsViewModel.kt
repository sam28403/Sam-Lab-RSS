package cc.samlab.rss.ui.page.settings.ai 

import androidx.lifecycle.ViewModel 
import androidx.lifecycle.viewModelScope 
import dagger.hilt.android.lifecycle.HiltViewModel 
import kotlinx.coroutines.launch 
import cc.samlab.rss.infrastructure.net.ApiResult 
import cc.samlab.rss.domain.repository.AiSummaryRepository 
import javax.inject.Inject 

@HiltViewModel 
class AiSettingsViewModel @Inject constructor( 
    private val aiSummaryRepository: AiSummaryRepository 
) : ViewModel() { 

    fun fetchModels( 
        baseUrl: String, 
        apiKey: String, 
        onSuccess: (List<String>) -> Unit, 
        onError: (String) -> Unit 
    ) { 
        viewModelScope.launch { 
            when (val result = aiSummaryRepository.fetchAvailableModels(baseUrl, apiKey)) { 
                is ApiResult.Success -> onSuccess(result.data) 
                is ApiResult.BizError -> onError(result.exception.message ?: "Business error") 
                is ApiResult.NetworkError -> onError(result.exception.message ?: "Network error") 
                is ApiResult.UnknownError -> onError(result.throwable.message ?: "Unknown error") 
            } 
        } 
    } 
}
