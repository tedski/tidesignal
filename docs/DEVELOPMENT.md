# TideSignal Development Guide

This guide provides comprehensive information for developers working on TideSignal.

## Table of Contents

- [Getting Started](#getting-started)
- [Environment Setup](#environment-setup)
- [Data Pipeline](#data-pipeline)
- [Building the App](#building-the-app)
- [Testing](#testing)
- [Code Structure](#code-structure)
- [Adding New Features](#adding-new-features)
- [Debugging](#debugging)
- [Performance Profiling](#performance-profiling)

## Getting Started

### Prerequisites

**Required:**
- **Android Studio**: Giraffe (2022.3.1) or later
- **JDK**: Java 17 or higher (Android Studio's bundled JDK recommended)
- **Android SDK**: API 30+ (WearOS 3)
- **Python**: 3.11+ (for data pipeline)
- **Git**: For version control

**Recommended:**
- **uv**: Modern Python package manager ([installation](https://astral.sh/uv/install.sh))
- **Physical WearOS device**: For battery and performance testing
- **WearOS emulator**: Device Manager > Create Device > Wear OS

### Quick Setup

1. **Clone the repository**:
   ```bash
   git clone https://github.com/yourusername/tidesignal.git
   cd tidesignal
   ```

2. **Install uv** (recommended for data pipeline):
   ```bash
   curl -LsSf https://astral.sh/uv/install.sh | sh
   ```

3. **Generate the tide database**:
   ```bash
   cd tools/data-pipeline

   # Test mode (5 stations, ~30 seconds)
   ./run.sh
   cp tides-test.db ../../app/src/main/assets/tides.db
   ```

4. **Open in Android Studio**:
   - File > Open > Select `tidesignal` directory
   - Wait for Gradle sync to complete

5. **Create WearOS emulator** (if no physical device):
   - Tools > Device Manager > Create Device
   - Select a WearOS device profile (e.g., Wear OS Small Round)
   - Download and select a WearOS system image (API 30+)

6. **Run the app**:
   ```bash
   ./gradlew installDebug
   ```
   Or use Android Studio's "Run" button (Shift+F10)

## Environment Setup

### Java Development Kit

TideSignal uses Gradle's Java Toolchain feature with the Foojay Resolver plugin to automatically detect and use the correct JDK version.

**Recommended JDK**: Android Studio's bundled JDK
- Location: `/Applications/Android Studio.app/Contents/jbr/Contents/Home` (macOS)
- Version: Java 17 or higher

**Alternative**: Install via Homebrew (macOS/Linux):
```bash
brew install openjdk@17
```

**No manual JAVA_HOME configuration needed** - Gradle detects the JDK automatically.

### Android SDK

Required SDK components:
- Android SDK Platform 34
- Android SDK Build-Tools 34.0.0
- WearOS system images (API 30+)

Install via Android Studio SDK Manager (Tools > SDK Manager).

### Python Environment

**Option 1: uv (Recommended)**

uv automatically manages dependencies and virtual environments:
```bash
# Install uv
curl -LsSf https://astral.sh/uv/install.sh | sh

# Use directly (no setup needed)
cd tools/data-pipeline
./run.sh
```

**Option 2: pip + venv**

```bash
cd tools/data-pipeline
python -m venv .venv
source .venv/bin/activate  # On Windows: .venv\Scripts\activate
pip install -r requirements.txt
```

### IDE Configuration

**Android Studio:**
- Install Kotlin plugin (usually pre-installed)
- Enable "Auto-import" for Kotlin (Preferences > Editor > General > Auto Import)
- Use Jetpack Compose preview (split view in editor)

**Recommended plugins:**
- Kotlin Multiplatform Mobile
- Compose Multiplatform IDE Support

## Data Pipeline

The data pipeline generates the SQLite database containing NOAA station data and harmonic constituents.

### Quick Reference

```bash
cd tools/data-pipeline

# Test mode (5 stations, ~30 seconds) - DEFAULT
./run.sh
cp tides-test.db ../../app/src/main/assets/tides.db

# Production mode (3,379 stations, ~30-45 minutes)
./run.sh --mode production
cp tides.db ../../app/src/main/assets/tides.db

# Custom station list
./run.sh --stations 9414290,8454000,8518750

# Verbose logging
./run.sh --verbose
```

### Pipeline Scripts

**1. fetch_noaa_data.py**

Queries NOAA CO-OPS API for station metadata and harmonic constituents:
- Handles API rate limiting with exponential backoff
- Supports test/production modes
- Creates `stations.json`

**2. build_database.py**

Converts JSON to SQLite database:
- Creates schema (stations, harmonic_constituents, subordinate_offsets)
- Builds spatial index for location queries
- Creates `tides-test.db` (test) or `tides.db` (production)

### When to Regenerate Database

- **During development**: Use test mode (5 stations) for fast iteration
- **Before release**: Use production mode (all 3,379 stations)
- **After NOAA updates**: Monthly or when constituents change
- **Adding new stations**: Use custom station list to test specific stations

See [tools/data-pipeline/README.md](../tools/data-pipeline/README.md) for detailed pipeline documentation.

## Building the App

### Gradle Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK (requires signing config)
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug

# Clean build artifacts
./gradlew clean

# Rebuild everything
./gradlew clean assembleDebug
```

### Build Variants

- **debug**: Development builds with debugging enabled
- **release**: Production builds, optimized and signed

### Gradle Tasks Reference

```bash
# Build
./gradlew assembleDebug           # Build debug APK
./gradlew assembleRelease         # Build release APK
./gradlew build                   # Build all variants

# Install
./gradlew installDebug            # Install debug on device
./gradlew uninstallAll            # Uninstall from device

# Testing
./gradlew test                    # Run unit tests
./gradlew connectedAndroidTest    # Run instrumented tests (requires device)

# Code Quality
./gradlew lint                    # Run Android lint
./gradlew lintDebug               # Lint debug variant
./gradlew check                   # Run all checks (test + lint)

# Dependencies
./gradlew dependencies            # Show dependency tree
./gradlew dependencyUpdates       # Check for dependency updates

# Clean
./gradlew clean                   # Remove build artifacts
```

### Build Configuration

Build configuration is in `app/build.gradle.kts`:
- Min SDK: 30 (WearOS 3+)
- Target SDK: 34
- Compile SDK: 34
- Kotlin compiler: 2.1.0
- Compose compiler: 1.6.0

## Testing

### Unit Tests

Run unit tests:
```bash
./gradlew test
```

Unit tests are in `app/src/test/kotlin/com/tidesignal/`:
- `tide/HarmonicCalculatorTest.kt` - Tide calculation accuracy
- `tide/AstronomicalCalculatorTest.kt` - Node factors, equilibrium arguments
- `tide/TideCacheTest.kt` - Cache invalidation and refresh

### Writing Unit Tests

Example test for harmonic calculation:
```kotlin
@Test
fun testTideCalculationAccuracy() {
    // Load test station
    val station = repository.getStation("9414290") // San Francisco

    // Calculate tide at known time
    val time = Instant.parse("2024-01-01T12:00:00Z")
    val height = harmonicCalculator.calculateHeight(station, time)

    // Compare against NOAA prediction
    val expected = 4.2f // From NOAA
    assertThat(height).isWithin(0.1f).of(expected)
}
```

### Integration Tests

Run integration tests (requires connected device or emulator):
```bash
./gradlew connectedAndroidTest
```

Integration tests verify:
- Database queries and location search
- UI navigation and interactions
- Cache behavior across station changes

### Validation Tests

Test against NOAA predictions:
1. Fetch NOAA predictions for test stations
2. Compare app calculations against NOAA data
3. Acceptable error: ±0.1 ft for height, ±2 minutes for times

Test stations:
- San Francisco, CA (9414290) - Mixed semidiurnal
- Providence, RI (8454000) - Semidiurnal
- The Battery, NY (8518750) - Semidiurnal

### Manual Testing Checklist

- [ ] Station selection (nearby and browse modes)
- [ ] Location permission flow
- [ ] Tide calculations at midnight (continuity check)
- [ ] Graph rendering on circular screens
- [ ] Navigation (back button, swipe gestures)
- [ ] Settings persistence
- [ ] AOD mode (if available)
- [ ] Battery drain over 1 hour
- [ ] Tile widget updates

## Code Structure

```
app/src/main/kotlin/com/tidesignal/
├── data/                          # Data layer
│   ├── TideDatabase.kt            # Room database
│   ├── StationRepository.kt       # CRUD operations, location search
│   └── models/                    # Data classes
│       ├── Station.kt
│       ├── HarmonicConstituent.kt
│       ├── SubordinateOffset.kt
│       └── TideExtremum.kt
├── tide/                          # Calculation engine
│   ├── Constituents.kt            # 37 NOAA constituent definitions
│   ├── AstronomicalCalculator.kt  # Node factors, equilibrium args
│   ├── HarmonicCalculator.kt      # Core tide prediction
│   └── TideCache.kt               # 7-day extrema pre-computation
├── ui/                            # UI layer
│   ├── app/                       # Main screens
│   │   ├── MainActivity.kt
│   │   ├── TideMainScreen.kt
│   │   ├── StationPickerScreen.kt
│   │   ├── TideDetailScreen.kt
│   │   └── SettingsScreen.kt
│   ├── tile/                      # Tile widget (future)
│   ├── components/                # Reusable components
│   │   ├── TideGraph.kt
│   │   ├── TideDirectionIndicator.kt
│   │   ├── ExtremumCard.kt
│   │   └── StationList.kt
│   └── theme/                     # Material You theme
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
└── TideSignalApp.kt                # Application entry point
```

### Key Modules

**Data Layer** (`data/`):
- Manages station database and queries
- Provides repository pattern for data access
- Room database with bundled SQLite

**Calculation Engine** (`tide/`):
- Harmonic analysis implementation
- Astronomical calculations
- Extrema caching for battery efficiency

**UI Layer** (`ui/`):
- Jetpack Compose for WearOS
- Material You theming
- Reusable components

## Adding New Features

### Adding a New Screen

1. **Create screen composable** in `ui/app/`:
   ```kotlin
   @Composable
   fun MyNewScreen(
       navController: NavController,
       viewModel: MyViewModel = viewModel()
   ) {
       // Screen content
   }
   ```

2. **Add navigation route** in `MainActivity.kt`:
   ```kotlin
   composable("myNewScreen") {
       MyNewScreen(navController)
   }
   ```

3. **Navigate to screen**:
   ```kotlin
   navController.navigate("myNewScreen")
   ```

### Adding a New Calculation

1. **Add to HarmonicCalculator** or create new calculator class
2. **Write unit tests** comparing against known values
3. **Add to cache** if expensive to compute
4. **Document** the algorithm and data sources

### Adding a New UI Component

1. **Create composable** in `ui/components/`
2. **Add preview** for development:
   ```kotlin
   @Preview(device = WearDevices.SMALL_ROUND)
   @Composable
   fun MyComponentPreview() {
       TideSignalTheme {
           MyComponent()
       }
   }
   ```
3. **Test on circular screens** (most WearOS devices)

### Adding Settings

1. **Add setting to data layer** (SharedPreferences or database)
2. **Update SettingsScreen** with new option
3. **Use setting** in relevant code
4. **Add default value** for backward compatibility

## Debugging

### Android Studio Debugger

1. Set breakpoints in code (click line number gutter)
2. Run app in debug mode (Shift+F9)
3. Use Logcat to view logs (View > Tool Windows > Logcat)

### Logging

Use Android's Log class:
```kotlin
import android.util.Log

private const val TAG = "TideSignal"

Log.d(TAG, "Debug message")
Log.i(TAG, "Info message")
Log.w(TAG, "Warning message")
Log.e(TAG, "Error message", exception)
```

Filter Logcat by tag: `tag:TideSignal`

### Compose Debugging

**Layout Inspector**: Tools > Layout Inspector
- View composable hierarchy
- Inspect composable properties
- Debug recomposition issues

**Compose Preview**: Edit any `@Composable` function
- Split view shows preview
- Interactive mode (click 🔍 icon)
- Device preview (WearOS device)

### Common Issues

**Database not found:**
- Ensure database is in `app/src/main/assets/tides.db`
- Check database is copied on first launch

**JDK version errors:**
- Ensure JDK 17+ is installed
- Gradle should auto-detect via Foojay Resolver

**Emulator not starting:**
- Check sufficient RAM allocated (2GB+)
- Enable hardware acceleration (Intel HAXM/AMD Hypervisor)

**Calculation accuracy issues:**
- Verify constituent data is correct
- Check astronomical calculations (node factors)
- Test midnight boundary cases (cache V0 continuity)

## Performance Profiling

### Battery Profiling

1. **Android Profiler**: View > Tool Windows > Profiler
2. Select "Energy" profiler
3. Monitor battery usage over 1 hour
4. Target: <2% per hour with screen on

### CPU Profiling

1. Enable CPU profiler in Android Studio
2. Record trace during tide calculation
3. Identify hot spots (should be <5ms for current height)

### Memory Profiling

1. Enable Memory profiler
2. Check for memory leaks (heap dumps)
3. Database should be ~5-7 MB in memory
4. Cache should be minimal (<1 MB per station)

### Performance Targets

- Station database load: <100ms
- Initial cache computation (7-day extrema): <200ms
- Current height calculation: <5ms
- Full 24-hour curve: <50ms
- Battery: <2% per hour with screen on

## Contributing

See [CONTRIBUTING.md](../CONTRIBUTING.md) for contribution guidelines.

Key areas for contribution:
- UI/UX improvements for small screens
- Battery optimization
- Test coverage
- Documentation
- International stations (non-NOAA)

## Additional Resources

- [TideSignal Architecture](ARCHITECTURE.md) - Technical architecture details
- [Data Pipeline README](../tools/data-pipeline/README.md) - Data pipeline documentation
- [WearOS Design Guidelines](https://developer.android.com/training/wearables/design)
- [Jetpack Compose for WearOS](https://developer.android.com/training/wearables/compose)
- [NOAA CO-OPS API](https://api.tidesandcurrents.noaa.gov/api/prod/)

## Getting Help

- **Issues**: Report bugs or request features on [GitHub Issues](https://github.com/yourusername/tidesignal/issues)
- **Discussions**: Ask questions in [GitHub Discussions](https://github.com/yourusername/tidesignal/discussions)
- **Documentation**: Check docs/ directory for detailed guides
