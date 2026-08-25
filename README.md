# Kuber Tijori (KT) — Frozen UI Final

This is the KT Android paper-trading project updated to follow the **frozen Kuber Tijori / Nidhi Online UI** supplied on 24 Aug 2026.

## Frozen navigation

- **Watchlist** — NIFTY, BANKNIFTY, FINNIFTY and stock quotes with mini charts.
- **Orders** — New Paper Order, Open Orders, Trade History and an inline Positions view.
- **KT Core** — Nidhi Command Center with bot status, P&L, last trade, NIFTY market card, Start/Stop Bot and Refresh Data.
- **AI** — Nidhi Assistant, Smart Actions and Ask Nidhi prompts.
- **Positions** — Open Positions and Performance.

## Paper trading functionality retained

- Virtual capital: ₹1,00,000
- Paper BUY / SELL execution
- Cash validation
- Position validation
- Weighted-average entry price
- Realized and unrealized P&L
- Local persistence across app restarts
- Reset paper account
- No broker connection or real-money execution

## UI direction

The frozen UI is the design source of truth: near-black background, KT blue/cyan highlights, gold secondary actions, dark blue cards, cyan/gold status accents, compact market cards, mini sparkline charts, Nidhi branding, and the five-item bottom navigation.

The manual order form is intentionally moved out of the main Command Center and into **Orders**, matching the frozen architecture.

## Build

```bash
./gradlew assembleDebug
```

Windows:

```bat
gradlew.bat assembleDebug
```

## Important

The current environment used to package this source project could not download Gradle from `services.gradle.org`, so an APK was **not** generated here. The project source and Gradle wrapper are included for local Android Studio/Gradle build.

The market quotes shown in the UI are demo/paper values until the live market-data adapter is connected.


## Sprint 1D UI update
- Uses the supplied KT logo in the splash-screen HUD center.
- Transparent logo treatment prevents a white square on the dark HUD.
- Animated cyan/gold HUD rings surround the KT mark.
- Existing paper-trading and API architecture is preserved.
