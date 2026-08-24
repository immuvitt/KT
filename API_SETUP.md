# KT Sprint 1B — Integrated Robot HUD + Market API

This build is based on the existing paper-trading implementation and integrates the new UI/API layer without replacing the paper engine.

## Included
- Nidhi robot icon in Bot Status
- subtle Iron-Man-inspired HUD grid/ring telemetry
- binary telemetry treatment
- market API abstraction (`MarketApi`, `MarketRepository`, `MarketModels`)
- optional 5-second polling while the paper bot is running
- API error/standby state
- existing paper orders, positions, P&L, persistence and screens retained
- no broker order API
- no API keys hardcoded

## Configure market API
Edit `MainActivity.kt`:

    private const val MARKET_BASE_URL = "https://YOUR-MARKET-DATA-API/"
    private const val MARKET_QUOTE_PATH = "v1/quote/NIFTY"

Expected JSON:

{
  "symbol": "NIFTY 50",
  "price": 24311.80,
  "change": 85.20,
  "changePercent": 0.35,
  "timestamp": 1787570000000
}

Until a real provider URL is configured, the app remains in API standby and uses the existing paper-mode sample quote.
