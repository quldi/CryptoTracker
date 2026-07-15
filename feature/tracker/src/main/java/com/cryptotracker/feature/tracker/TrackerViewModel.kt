package com.cryptotracker.feature.tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cryptotracker.domain.CryptoFilter
import com.cryptotracker.domain.CryptoRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TrackerViewModel(
    private val repository: CryptoRepository
) : ViewModel() {

    val activeFilter = MutableStateFlow(CryptoFilter.POPULAR)
    
    private val _uiState = MutableStateFlow(TrackerUiState())
    val uiState: StateFlow<TrackerUiState> = _uiState.asStateFlow()

    private var cachedFng: Pair<Int, String>? = null
    private var currentStreamJob: Job? = null

    init {
        observeFilterChanges()
    }

    private fun observeFilterChanges() {
        viewModelScope.launch {
            activeFilter.collect { filter ->
                loadDataForFilter(filter)
            }
        }
    }

    private fun loadDataForFilter(filter: CryptoFilter) {
        currentStreamJob?.cancel()
        currentStreamJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                val fng = cachedFng ?: repository.getFearAndGreedIndex().also { cachedFng = it }
                
                repository.getCryptoDashboardStream(filter).collect { list ->
                    _uiState.value = TrackerUiState(
                        cryptoList = list,
                        fngScore = fng.first,
                        fngLabel = fng.second,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Network Error"
                )
            }
        }
    }

    fun changeFilter(filter: CryptoFilter) {
        activeFilter.value = filter
    }

    fun retryConnection() {

        cachedFng = null
        
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null
        )
        
        loadDataForFilter(activeFilter.value)
    }
}