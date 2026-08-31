package com.kt.app.data.market

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Generic REST market-data adapter.
 *
 * Expected response:
 * {
 *   "symbol": "NIFTY 50",
 *   "price": 24311.80,
 *   "change": 85.20,
 *   "changePercent": 0.35,
 *   "timestamp": 1787570000000
 * }
 *
 * Replace the URL/JSON mapping when the final market-data provider is selected.
 */
class MarketApi(private val config: MarketApiConfig) {

    suspend fun getNiftyQuote(): Result<MarketQuote> = withContext(Dispatchers.IO) {
        runCatching {
            val base = config.baseUrl.trimEnd('/') + "/"
            val url = URL(base + config.quotePath.trimStart('/'))

            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10_000
                readTimeout = 10_000
                setRequestProperty("Accept", "application/json")
            }

            try {
                val status = connection.responseCode
                val stream = if (status in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

                val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()

                if (status !in 200..299) {
                    error("Market API HTTP $status: $body")
                }

                val json = JSONObject(body)

                MarketQuote(
                    symbol = json.optString("symbol", "NIFTY 50"),
                    price = json.getDouble("price"),
                    change = json.optDouble("change", 0.0),
                    changePercent = json.optDouble("changePercent", 0.0),
                    timestamp = json.optLong("timestamp", System.currentTimeMillis())
                )
            } finally {
                connection.disconnect()
            }
        }
    }
}
