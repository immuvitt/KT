package com.kt.app.viewmodel

import com.kt.app.data.market.MarketRepository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class TradingViewModelFactory(
    private val repository: MarketRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(TradingViewModel::class.java))
        return TradingViewModel(repository) as T
    }
}
