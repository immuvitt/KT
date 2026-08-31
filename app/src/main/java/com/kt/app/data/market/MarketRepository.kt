package com.kt.app.data.market

class MarketRepository(
    private val api: MarketApi
) {
    suspend fun getNiftyQuote(): Result<MarketQuote> = api.getNiftyQuote()
}
