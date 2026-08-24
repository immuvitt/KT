# KT Sprint 1B — API + Robot HUD

## What changed

- Added robot/AI visual using `Icons.Default.SmartToy`.
- Added rotating HUD ring and subtle Iron-Man-inspired grid/telemetry treatment.
- Added market API abstraction.
- Added NIFTY quote model.
- Added repository + ViewModel.
- Added 5-second paper-mode market polling.
- Added online/offline/API-error states.
- Kept trading execution in paper mode.

## Important

The exact market-data provider has NOT been hardcoded because no provider/API key was supplied yet.

Update these constants in `MainActivity.kt`:

    MARKET_BASE_URL = "https://YOUR-MARKET-DATA-API/"
    MARKET_QUOTE_PATH = "v1/quote/NIFTY"

The provider must return JSON equivalent to:

{
  "symbol": "NIFTY 50",
  "price": 24311.80,
  "change": 85.20,
  "changePercent": 0.35,
  "timestamp": 1787570000000
}

Once the provider is selected, only `MarketApi.kt` should need provider-specific mapping.

## Gradle dependency

The existing Sprint 1 project already includes Compose Material Icons Extended. If the current branch does not, add:

    implementation("androidx.compose.material:material-icons-extended")

The current Android project is already configured for Compose/compileSdk 37 in the Sprint 1 patch.
Android's August 2026 documentation lists Compose 1.12 as stable and the 2026.08.00 BOM as current, but this update intentionally does not force a dependency upgrade.
