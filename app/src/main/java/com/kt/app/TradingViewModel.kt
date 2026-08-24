package com.kt.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TradingUiState(
    val quote: MarketQuote? = null,
    val connected: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null,
    val engineRunning: Boolean = false
)

class TradingViewModel(
    private val repository: MarketRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TradingUiState())
    val state: StateFlow<TradingUiState> = _state.asStateFlow()

    private var pollingJob: Job? = null

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)

            repository.getNiftyQuote()
                .onSuccess { quote ->
                    _state.value = _state.value.copy(
                        quote = quote,
                        connected = true,
                        loading = false,
                        error = null
                    )
                }
                .onFailure { throwable ->
                    _state.value = _state.value.copy(
                        connected = false,
                        loading = false,
                        error = throwable.message ?: "Market API unavailable"
                    )
                }
        }
    }

    fun startEngine() {
        if (pollingJob?.isActive == true) return

        _state.value = _state.value.copy(engineRunning = true)

        pollingJob = viewModelScope.launch {
            while (isActive) {
                refresh()
                delay(5_000)
            }
        }
    }

    fun stopEngine() {
        pollingJob?.cancel()
        pollingJob = null
        _state.value = _state.value.copy(engineRunning = false)
    }

    override fun onCleared() {
        pollingJob?.cancel()
        super.onCleared()
    }
}
