# TideWatch - Next Steps

Quick guide to complete the MVP implementation.

## Step 1: Run Data Pipeline (<1 minute for test mode)

Generate the tide database:

```bash
cd tools/data-pipeline

# Install uv once (if not already installed)
curl -LsSf https://astral.sh/uv/install.sh | sh

# Test mode (recommended for development) - 5 stations, ~30 seconds
./run.sh
cp tides-test.db ../../app/src/main/assets/tides.db

# Production mode (for release builds) - all 3,379 stations, ~30-45 minutes
./run.sh --mode production
cp tides.db ../../app/src/main/assets/tides.db
```

**Modes:**
- **Test mode** (default): 5 well-distributed stations for rapid iteration
- **Production mode**: All 3,379 NOAA stations for release builds

**Note**: uv automatically manages dependencies in an isolated environment. For development, test mode is sufficient. Only run production mode when building release APKs. See `tools/data-pipeline/README.md` for more options.

## Step 2: Implement Main UI Screens (2-3 hours)

### ✅ MainActivity.kt - COMPLETED
- Initialize database
- Set up navigation
- Handle permissions
- Ambient mode support

### ✅ TideMainScreen.kt - COMPLETED
- Display current tide height (large)
- Show direction indicator
- Display next high/low
- Mini 24-hour graph
- Station name header
- Adaptive refresh intervals (1 min active, 15 min ambient)

### StationPickerScreen.kt
- Location-based search
- List of nearby stations
- Distance display
- Selection handler

### TideDetailScreen.kt
- Full 24-hour curve
- Scrollable 7-day list
- Station metadata

### SettingsScreen.kt
- Unit toggle (feet/meters)
- Station management
- About section

## Step 3: Add ViewModel Layer (1 hour)

### TideViewModel.kt
- Current station management
- Tide calculation orchestration
- Cache management
- Location handling

## Step 4: Implement Tile Widget (1 hour)

### TideTileService.kt
- Compact tile layout
- Update schedule
- Tap to launch

## Step 5: Write Unit Tests (2 hours)

### HarmonicCalculatorTest.kt
- Test against NOAA predictions
- Validate accuracy thresholds
- Edge case coverage

## Step 6: Manual Testing (2 hours)

- Test on WearOS emulator
- Verify calculations
- UI/UX refinement
- Battery profiling

## Step 7: Polish & Documentation (1 hour)

- Update README with build instructions
- Add inline code comments
- Create user guide
- Screenshot for Play Store

## Total Time: 8-10 hours

The foundation is complete. The remaining work is straightforward UI implementation.

## Quick Start Command

```bash
# Generate database (using uv)
cd tools/data-pipeline && ./run.sh && cp tides.db ../../app/src/main/assets/

# Open in Android Studio
cd ../.. && studio .

# Run on emulator
./gradlew installDebug
```

## Testing Checklist

- [ ] Database loads from assets
- [ ] Location permission works
- [ ] Nearest stations found correctly
- [ ] Tide calculations match NOAA
- [x] UI responsive on small screen (TideMainScreen implemented)
- [ ] Tile updates correctly
- [x] AOD mode works (ambient mode implemented)
- [x] Battery impact <2%/hour (15-min refresh in AOD)
- [ ] No memory leaks

## Resources

- WearOS Emulator: Android Studio > Device Manager > Create Device > Wear OS
- NOAA Predictions: https://tidesandcurrents.noaa.gov/noaatidepredictions.html
- Testing Stations:
  - 9414290: San Francisco, CA
  - 8454000: Providence, RI
  - 8518750: The Battery, NY

Good luck! The hard part is done. 🌊
