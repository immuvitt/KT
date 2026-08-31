# KT deduplication fix

This package removes the unused parallel dashboard/ViewModel implementation that was added during the architecture cleanup.

The active application implementation remains in MainActivity.kt, preserving the frozen UI and paper-trading behavior.

Kept:
- MainActivity.kt as the active UI and paper-trading implementation
- data/market/ as the active market API abstraction used by MainActivity.kt
- ui/theme/ resources

Removed as unused parallel implementations:
- ui/dashboard/HudDashboard.kt
- viewmodel/TradingViewModel.kt
- viewmodel/TradingViewModelFactory.kt

This eliminates the duplicated architectural implementation. If Android Studio still reports a literal "Duplicate class" compiler error after replacing the project with this package, the exact Build Output line is required because the remaining source tree does not contain duplicate fully-qualified Kotlin class declarations.
