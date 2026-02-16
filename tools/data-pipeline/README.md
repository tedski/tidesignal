# TideSignal Data Pipeline

Scripts to fetch tide station data from NOAA and build the SQLite database.

## Overview

The data pipeline consists of two scripts:

1. `fetch_noaa_data.py` - Queries NOAA CO-OPS API for station data
2. `build_database.py` - Converts JSON data to SQLite database

The pipeline supports two modes for different use cases:
- **Test mode** (default): 5 stations, ~30 seconds - for rapid local development
- **Production mode**: All 3,379 stations, ~30-45 minutes - for release builds

## Quick Start (Recommended)

**Using uv** (modern Python package manager):

```bash
# Install uv once (if not already installed)
curl -LsSf https://astral.sh/uv/install.sh | sh

# Run in test mode (default, 5 stations, ~30 seconds)
./run.sh

# Run in production mode (all 3,379 stations, ~30-45 minutes)
./run.sh --mode production

# Copy database to app assets
cp tides-test.db ../../app/src/main/assets/tides.db    # For test mode
cp tides.db ../../app/src/main/assets/tides.db         # For production mode
```

uv automatically manages virtual environments and dependencies - no manual setup needed!

## Usage Modes

### Test Mode (Default)

Fetches only 5 well-distributed test stations for rapid development iteration:
- San Francisco, CA (9414290)
- Providence, RI (8454000)
- The Battery, NY (8518750)
- Wilmington, NC (8658120)
- Cape Hatteras, NC (8636580)

**When to use:**
- Local development and testing
- Iterating on features quickly
- Learning the codebase
- Running unit tests

**Runtime:** ~30 seconds
**Output:** `tides-test.db` (~36 KB)

```bash
./run.sh                    # Test mode (implicit)
./run.sh --mode test        # Test mode (explicit)
```

### Production Mode

Fetches all 3,379 NOAA tide prediction stations with complete harmonic data.

**When to use:**
- Building release APKs
- Creating production databases
- Testing with full dataset
- Validating coverage

**Runtime:** ~30-45 minutes (API rate limiting)
**Output:** `tides.db` (~2.6 MB)

```bash
./run.sh --mode production
```

### Custom Station List

Fetch specific stations by ID (useful for testing specific regions):

```bash
./run.sh --stations 9414290,8454000,8518750
```

## Command Reference

### run.sh

Master script that orchestrates the full pipeline.

```bash
./run.sh [OPTIONS]

Options:
  --mode MODE         Database mode: 'test' or 'production' (default: test)
  --stations IDS      Comma-separated list of station IDs (overrides --mode)
  --verbose           Enable detailed logging
  --help, -h          Show help message

Examples:
  ./run.sh                                    # Test mode (default)
  ./run.sh --mode production                  # Production mode
  ./run.sh --stations 9414290,8454000         # Custom station list
  ./run.sh --mode test --verbose              # Test mode with detailed logging
```

### fetch_noaa_data.py

Fetch station data from NOAA CO-OPS API.

```bash
uv run fetch_noaa_data.py [OPTIONS]

Options:
  --mode {test|production}    Database mode (default: test)
  --stations IDS              Comma-separated station IDs (overrides mode)
  --output FILENAME           Output JSON file (default: stations.json)
  --verbose                   Enable detailed logging

Examples:
  uv run fetch_noaa_data.py                              # Test mode
  uv run fetch_noaa_data.py --mode production            # Production mode
  uv run fetch_noaa_data.py --stations 9414290,8454000   # Custom list
```

### build_database.py

Convert JSON data to SQLite database.

```bash
uv run build_database.py [OPTIONS]

Options:
  --mode {test|production}    Database mode (default: test)
  --input FILENAME            Input JSON file (default: stations.json)
  --output FILENAME           Output database file (overrides mode-based naming)

Examples:
  uv run build_database.py                          # Test mode → tides-test.db
  uv run build_database.py --mode production        # Production → tides.db
  uv run build_database.py --output custom.db       # Custom output
```

## Alternative Methods

### Using pip (traditional)

```bash
pip install -r requirements.txt

# Test mode
python fetch_noaa_data.py
python build_database.py
cp tides-test.db ../../app/src/main/assets/tides.db

# Production mode
python fetch_noaa_data.py --mode production
python build_database.py --mode production
cp tides.db ../../app/src/main/assets/tides.db
```

### Using Python venv (manual isolation)

```bash
python -m venv .venv
source .venv/bin/activate  # On Windows: .venv\Scripts\activate
pip install -r requirements.txt

# Test mode (default)
python fetch_noaa_data.py
python build_database.py
cp tides-test.db ../../app/src/main/assets/tides.db

# Production mode
python fetch_noaa_data.py --mode production
python build_database.py --mode production
cp tides.db ../../app/src/main/assets/tides.db
```

## What Each Step Does

### 1. Fetch NOAA data

```bash
uv run fetch_noaa_data.py [--mode {test|production}]
```

Queries the NOAA CO-OPS API and creates `stations.json` with station metadata and harmonic constituents.

- **Test mode** (default): Fetches 5 test stations (~30 seconds)
- **Production mode**: Fetches all 3,379 stations (~30-45 minutes)

The script includes retry logic with exponential backoff for network resilience.

### 2. Build database

```bash
uv run build_database.py [--mode {test|production}]
```

Converts JSON data to SQLite database:
- **Test mode**: Creates `tides-test.db` (~36 KB)
- **Production mode**: Creates `tides.db` (~2.6 MB)

### 3. Copy to app assets

```bash
# For test database
cp tides-test.db ../../app/src/main/assets/tides.db

# For production database
cp tides.db ../../app/src/main/assets/tides.db
```

The app will copy this database to its data directory on first launch.

## NOAA API Reference

- **Stations API**: https://api.tidesandcurrents.noaa.gov/mdapi/prod/webapi/stations.json
- **Harmonic Constituents**: https://api.tidesandcurrents.noaa.gov/mdapi/prod/webapi/stations/{id}/harcon.json
- **Documentation**: https://api.tidesandcurrents.noaa.gov/api/prod/

## Database Schema

### stations
- `id` (TEXT, PK) - NOAA station ID
- `name` (TEXT) - Station name
- `state` (TEXT) - State/region
- `latitude` (REAL) - Decimal degrees
- `longitude` (REAL) - Decimal degrees
- `type` (TEXT) - "harmonic" or "subordinate"
- `timezoneOffset` (INTEGER) - UTC offset in minutes
- `referenceStationId` (TEXT, nullable) - For subordinate stations

### harmonic_constituents
- `stationId` (TEXT, FK)
- `constituentName` (TEXT) - e.g., "M2", "S2"
- `amplitude` (REAL) - in feet or meters
- `phaseLocal` (REAL) - in degrees

### subordinate_offsets
- `stationId` (TEXT, PK, FK)
- `referenceStationId` (TEXT, FK)
- `timeOffsetHigh` (INTEGER) - minutes
- `timeOffsetLow` (INTEGER) - minutes
- `heightOffsetHigh` (REAL) - multiplier
- `heightOffsetLow` (REAL) - multiplier

## Features

- ✅ **Dual-mode operation**: Test mode (5 stations) for development, production mode (3,379 stations) for releases
- ✅ **Network resilience**: Automatic retry with exponential backoff for failed API requests
- ✅ **Progress tracking**: Smart progress display (detailed for small datasets, summary for large)
- ✅ **Flexible filtering**: Custom station lists via command-line
- ✅ **Harmonic vs subordinate**: Automatic detection and classification (Note: As of 2026, all NOAA stations provide harmonic data due to network modernization)

## Future Enhancements

- Automated monthly updates via GitHub Actions
- Database versioning and migration
- Incremental updates (only changed stations)
- International tide stations (non-NOAA sources)
