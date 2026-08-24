package com.kt.app

data class MarketQuote(
    val symbol: String,
    val price: Double,
    val change: Double,
    val changePercent: Double,
    val timestamp: Long
)

data class MarketApiConfig(
    val baseUrl: String,
    val quotePath: String = "v1/quote/NIFTY"
)
