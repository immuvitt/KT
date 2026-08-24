# Kuber Tijori (KT) — Paper Trading Build

This is the actual KT Android project with the paper-trading order workflow added on top of the existing repository.

## What works in this build

- KT / Nidhi splash screen
- Command Center dashboard
- Start / Stop paper engine state
- Virtual capital: ₹1,00,000
- Manual paper BUY / SELL market orders
- Symbol, price and quantity entry
- Cash validation on BUY
- Position validation on SELL
- Average-price position tracking
- Realized P&L calculation
- Persistent order ledger and positions across app restarts
- Orders screen
- Positions screen
- Reset paper account
- GitHub Actions debug APK build

## Important

This is **paper trading only**. No broker credentials, real-money order routing, or live execution is included.

The order service is intentionally local so the Android app can be installed and verified immediately. A live market-data/broker adapter can later feed the same paper-order service without changing the order ledger UI.

## Build on GitHub

Push the project to `main`. The included `.github/workflows/android.yml` builds `app-debug.apk` and uploads it as the `kt-debug-apk` artifact.

## Local build

```bash
./gradlew assembleDebug
```

Windows:

```bat
gradlew.bat assembleDebug
```

## v0.4 — Paper Trading Ledger + Mark-to-Market

This update is built on the working paper-trading project and adds:

- Finalized Kubēr Tijori logo artwork in splash, dashboard branding, and launcher assets.
- Persistent paper orders and positions.
- Manual LTP/mark-price updates for open positions.
- Unrealized, realized, and total paper P&L on the Positions screen.
- Weighted-average entry price when adding to an existing position.
- Safer paper SELL validation and position reduction.
- Clear separation between simulated execution and future live market-data/broker modules.

### Current scope

The app remains paper trading only. No real broker order is sent. The next module can replace the manual mark-price field with a live market-data adapter without changing the order ledger API.
