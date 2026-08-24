package com.kt.app

class MarketRepository(
    private val api: MarketApi
) {
    suspend fun getNiftyQuote(): Result<MarketQuote> = api.getNiftyQuote()
}
