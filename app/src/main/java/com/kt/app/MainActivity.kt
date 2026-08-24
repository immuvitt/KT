package com.kt.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState

private const val MARKET_BASE_URL = "https://YOUR-MARKET-DATA-API/"
private const val MARKET_QUOTE_PATH = "v1/quote/NIFTY"

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val api = MarketApi(
            MarketApiConfig(
                baseUrl = MARKET_BASE_URL,
                quotePath = MARKET_QUOTE_PATH
            )
        )
        val repository = MarketRepository(api)

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme()
            ) {
                val tradingViewModel = viewModel<TradingViewModel>(
                    factory = TradingViewModelFactory(repository)
                )

                val state = tradingViewModel.state.collectAsState().value

                HudDashboard(
                    state = state,
                    onRefresh = tradingViewModel::refresh,
                    onStart = tradingViewModel::startEngine,
                    onStop = tradingViewModel::stopEngine
                )
            }
        }
    }
}
