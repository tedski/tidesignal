# TideWatch Architecture

This document describes the technical architecture and implementation details of the TideWatch WearOS tide prediction app.

## Overview

TideWatch is built using a layered architecture with clear separation of concerns:
- **UI Layer**: Jetpack Compose for WearOS screens and components
- **Calculation Engine**: Harmonic analysis with astronomical calculations
- **Data Layer**: Room database with bundled SQLite for offline operation

## High-Level Architecture

```
┌─────────────────────────────────────────┐
│          WearOS UI Layer                │
│  ┌──────────────┐    ┌──────────────┐  │
│  │  Watch App   │    │  Tile Widget │  │
│  └──────────────┘    └──────────────┘  │
└────────────┬─────────────────┬──────────┘
             │                 │
┌────────────┴─────────────────┴──────────┐
│       Tide Calculation Engine           │
│  ┌───────────────────────────────────┐  │
│  │  HarmonicCalculator               │  │
│  │  - 37 tidal constituents          │  │
│  │  - Node factors (astronomical)    │  │
│  │  - Newton's method for extrema    │  │
│  └───────────────────────────────────┘  │
│  ┌───────────────────────────────────┐  │
│  │  TideCache (7-day extrema)        │  │
│  └───────────────────────────────────┘  │
└────────────┬────────────────────────────┘
             │
┌────────────┴────────────────────────────┐
│        Station Database (Room)          │
│  - ~3,000 NOAA stations                 │
│  - Harmonic constituents per station    │
│  - Station metadata (location, etc.)    │
└─────────────────────────────────────────┘
```

## Data Layer

### Database Schema

**stations** table:
- `id` (TEXT, primary key) - NOAA station ID
- `name` (TEXT) - Human-readable name
- `state` (TEXT) - State/region
- `latitude` (REAL)
- `longitude` (REAL)
- `type` (TEXT) - "harmonic" or "subordinate"
- `timezone_offset` (INTEGER) - UTC offset in minutes
- `reference_station_id` (TEXT, nullable) - For subordinate stations

**harmonic_constituents** table:
- `station_id` (TEXT, foreign key)
- `constituent_name` (TEXT) - e.g., "M2", "S2", "K1"
- `amplitude` (REAL) - in feet/meters
- `phase_local` (REAL) - in degrees

**subordinate_offsets** table:
- `station_id` (TEXT, foreign key)
- `reference_station_id` (TEXT)
- `time_offset_high` (INTEGER) - minutes
- `time_offset_low` (INTEGER) - minutes
- `height_offset_high` (REAL) - multiplier
- `height_offset_low` (REAL) - multiplier

### Data Pipeline

The data pipeline consists of Python scripts in `tools/data-pipeline/`:

1. **Fetch**: Query NOAA CO-OPS API for all stations
   - Endpoint: `https://api.tidesandcurrents.noaa.gov/mdapi/prod/webapi/stations.json`
   - Get harmonic constituents for each station
   - Get subordinate station relationships

2. **Transform**: Convert JSON to SQLite database
   - Parse constituent amplitudes and phases
   - Store metadata (station names, coordinates)
   - Build spatial index for location-based search

3. **Bundle**: SQLite database included in APK assets
   - Database copied to app storage on first launch
   - Version tracked for future updates

**Database Size**: ~5-7 MB for all 3,379 stations with harmonic constituents

## Calculation Engine

### Harmonic Analysis

TideWatch uses the standard harmonic method employed by NOAA for tide prediction. This method models tides as the sum of harmonic constituents representing different astronomical cycles.

**Core Formula**:
```
h(t) = Σ[A_i × f_i × cos(ω_i × t + φ_i - κ_i)]
```

Where:
- `A_i` = constituent amplitude (from database)
- `f_i` = node factor (from astronomical calculation)
- `ω_i` = angular velocity (constituent speed in degrees/hour)
- `φ_i` = local phase (from database)
- `κ_i` = equilibrium argument (from astronomical position)
- `t` = time (in hours since epoch)

**Key Constituents** (37 standard NOAA constituents):
- Principal lunar: M2, N2, 2N2
- Principal solar: S2, K2
- Diurnal: K1, O1, P1, Q1
- Long period: Mm, Mf, Ssa
- Compound: M4, M6, MS4, etc.

All constituent definitions are in `app/src/main/kotlin/com/tidewatch/tide/Constituents.kt`.

### Astronomical Calculations

The `AstronomicalCalculator` computes time-dependent astronomical factors:

1. **Node Factors (f_i)**: Account for the 18.6-year lunar nodal cycle and other long-period variations
2. **Equilibrium Arguments (κ_i)**: Account for the astronomical position of the sun and moon at time t

These calculations are based on NOAA's Special Publication NOS CO-OPS 3 and use simplified astronomical formulas optimized for tide prediction.

### Extrema Finding

High and low tides (extrema) are found using Newton's method:
1. Start with a rough estimate (search for sign change in derivative)
2. Iteratively refine using Newton-Raphson: `t_new = t - f'(t) / f''(t)`
3. Converges in 3-5 iterations to within ±1 minute accuracy

### Subordinate Stations

For subordinate stations (those without harmonic constituents):
1. Calculate tide at reference station
2. Apply time offsets to high/low tide times
3. Apply height multipliers to tide heights
4. No performance difference from harmonic stations after cache

## Battery Optimization Strategy

TideWatch uses a hybrid caching strategy to minimize battery impact while maintaining accuracy:

### Caching Strategy

**On station selection or date change** (expensive, runs once):
1. Calculate astronomical node factors for current year (~10ms)
2. Pre-compute 7 days of high/low tide times and heights (~200ms)
   - Use Newton's method to find extrema
   - Store in `TideCache` memory cache
   - Cache invalidated daily at midnight

**On UI update** (frequent, must be fast):
1. Check cache for next high/low times (instant lookup)
2. Calculate current height using full harmonic sum (~5ms)
3. Calculate rate of change (derivative) for tide direction (~5ms)

**For detail views** (occasional):
1. Generate full 24-hour tide curve
2. Calculate height every 10 minutes
3. Total: ~50ms for 144 data points

### Update Frequency

- **Active screen**: Calculate current height on each UI frame (composable)
- **Tile updates**: Every 5 minutes or on screen wake
- **AOD mode**: Reduce to 15-minute updates with simplified rendering
- **Background**: No updates, no battery drain

### Performance Targets

- Station database load: <100ms
- Initial cache computation (7-day extrema): <200ms
- Current height calculation: <5ms
- Full 24-hour curve generation: <50ms
- **Battery impact**: <2% per hour with screen on

## UI Layer

### Technology Stack

- **Framework**: Jetpack Compose for WearOS
- **Material Design**: Material You with WearOS adaptations
- **Navigation**: Simple stack-based navigation
- **Theme**: Dynamic colors, high contrast for outdoor readability

### Key Screens

1. **TideMainScreen**: Primary tide display with current height, direction, and next extrema
2. **StationPickerScreen**: Location-based and browse modes for station selection
3. **TideDetailScreen**: Full 24-hour curve and 7-day extrema list
4. **SettingsScreen**: Units, preferences, about

### Reusable Components

- **TideGraph**: 24-hour tide curve visualization with time axis
- **TideDirectionIndicator**: Rising/falling/slack indicator with rate
- **ExtremumCard**: High/low tide display card
- **StationList**: Scrollable station picker with location info

### AOD (Always-On Display) Support

- Simplified rendering in AOD mode
- Show only essential info: current height and next extrema
- High contrast, minimal colors
- Reduced update frequency (15 minutes)

## Code Structure

```
app/src/main/kotlin/com/tidewatch/
├── data/
│   ├── TideDatabase.kt          # Room database
│   ├── StationRepository.kt     # CRUD operations
│   └── models/                  # Data classes
│       ├── Station.kt
│       ├── HarmonicConstituent.kt
│       ├── SubordinateOffset.kt
│       └── TideExtremum.kt
├── tide/
│   ├── Constituents.kt          # 37 constituent definitions
│   ├── AstronomicalCalculator.kt # Node factors, equilibrium args
│   ├── HarmonicCalculator.kt    # Core tide prediction
│   └── TideCache.kt             # Extrema pre-computation
├── ui/
│   ├── app/                     # Main screens
│   ├── tile/                    # Tile widget
│   ├── components/              # Reusable widgets
│   └── theme/                   # Material You theme
└── TideWatchApp.kt              # Application entry point
```

## Design Decisions

### Why Room + Bundled SQLite?

- **Offline-first**: All data bundled in APK, no network required
- **Read-only**: Station data doesn't change during app runtime
- **Spatial queries**: SQLite supports efficient location-based search
- **Size**: 5-7 MB compressed is acceptable for APK size

### Why Pre-compute Extrema?

- **Battery**: Finding extrema is expensive (Newton's method requires iteration)
- **UX**: Users primarily care about next high/low, which should be instant
- **Accuracy**: Still calculate current height on-demand for real-time precision
- **Trade-off**: 200ms one-time cost vs. continuous recalculation

### Why 37 Constituents?

- **NOAA Standard**: NOAA uses 37 constituents for U.S. stations
- **Accuracy**: Achieves ±0.1 ft accuracy vs. NOAA predictions
- **Performance**: 37 terms sum in <5ms on modern hardware
- **Completeness**: Covers all major astronomical cycles

### Why Newton's Method for Extrema?

- **Fast convergence**: 3-5 iterations to ±1 minute accuracy
- **Simple**: No need for complex optimization algorithms
- **Deterministic**: Always finds local extrema given good starting point
- **Robust**: Handles complex tidal regimes (mixed semidiurnal, etc.)

## Testing Strategy

### Unit Tests

- Harmonic calculation accuracy vs. NOAA predictions
- Astronomical calculations (node factors, equilibrium arguments)
- Subordinate station offset application
- Cache invalidation and refresh logic

### Integration Tests

- Database queries and station selection
- Location-based search
- Multi-station management

### Validation

- Compare against NOAA published predictions
- Acceptable error: ±0.1 ft for height, ±2 minutes for times
- Test stations: San Francisco (9414290), Providence (8454000), NYC (8518750)

## Security & Privacy

- **No tracking**: Zero analytics or tracking
- **No accounts**: No user authentication or cloud sync
- **Minimal permissions**: Only coarse location (optional)
- **Offline-first**: No network requests after installation
- **Open source**: GPL-3.0 licensed, auditable code

## Data Attribution

- **NOAA Data**: Public domain (U.S. Government work)
- **API**: https://api.tidesandcurrents.noaa.gov/
- **Constituents**: NOAA Special Publication NOS CO-OPS 3
- **NOAA Logo**: Not used (requires permission)

## Future Architectural Considerations

- **Watch face complications**: Different API, very limited space
- **Tidal currents**: Different calculation method (velocity vs. height)
- **International stations**: May require additional constituent data sources
- **Offline maps**: Large data size concern for station picker
- **Multi-day calendar view**: Requires expanded cache strategy

## References

- [NOAA CO-OPS API Documentation](https://api.tidesandcurrents.noaa.gov/api/prod/)
- [Harmonic Analysis of Tides (Schureman, 1958)](https://tidesandcurrents.noaa.gov/publications/)
- [WearOS Design Guidelines](https://developer.android.com/training/wearables/design)
- [Jetpack Compose for WearOS](https://developer.android.com/training/wearables/compose)
- [Room Persistence Library](https://developer.android.com/training/data-storage/room)
