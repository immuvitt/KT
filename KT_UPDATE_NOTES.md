# KT UI/API update

Changes in this build:
- Removed the dedicated NIDHI TELEMETRY card from the Command Center.
- Changed the Nidhi robot icon in Bot Status to a fully visible white icon.
- Kept the Nidhi boot sequence and binary initialization screen.
- Kept the full paper-trading engine, orders, positions, P&L and local persistence.
- Added the market-data API abstraction and repository.
- Added optional 5-second NIFTY polling while the paper bot is running.
- Added API standby/error state.
- Shows `DEMO` on NIFTY while the provider URL is still a placeholder, instead of incorrectly showing `LIVE`.
- Added Android INTERNET permission.
- No broker/real-money execution and no API keys are stored in source.

Build note: this environment cannot download the Gradle wrapper distribution because external network access is unavailable. Please build with the existing Android Studio/Gradle environment on the development machine.
