#!/bin/bash
# Run TideWatch data pipeline with uv
# Supports test mode (default, 5 stations) and production mode (all 3,379 stations)

set -e

# Default values
MODE="test"
STATIONS=""
VERBOSE=""

# Parse command-line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --mode)
            MODE="$2"
            shift 2
            ;;
        --stations)
            STATIONS="$2"
            shift 2
            ;;
        --verbose)
            VERBOSE="--verbose"
            shift
            ;;
        --help|-h)
            cat << EOF
TideWatch Data Pipeline

Usage: $0 [OPTIONS]

Options:
  --mode MODE         Database mode: 'test' or 'production' (default: test)
  --stations IDS      Comma-separated list of station IDs (overrides --mode)
  --verbose           Enable detailed logging
  --help, -h          Show this help message

Modes:
  test                Fetch 5 test stations (~30 seconds)
                      Output: tides-test.db
  production          Fetch all 3,379 NOAA stations (~30-45 minutes)
                      Output: tides.db

Examples:
  $0                                    # Test mode (default)
  $0 --mode test                        # Test mode (explicit)
  $0 --mode production                  # Production mode
  $0 --stations 9414290,8454000         # Custom station list
  $0 --mode test --verbose              # Test mode with detailed logging

Next Steps:
  After running, copy the database to app assets:
  cp tides-test.db ../../app/src/main/assets/tides.db    # For test database
  cp tides.db ../../app/src/main/assets/tides.db         # For production database

EOF
            exit 0
            ;;
        *)
            echo "Error: Unknown option $1"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

# Validate mode
if [[ "$MODE" != "test" && "$MODE" != "production" ]]; then
    echo "Error: Invalid mode '$MODE'. Must be 'test' or 'production'"
    echo "Use --help for usage information"
    exit 1
fi

# Display configuration
echo "=========================================="
echo "TideWatch Data Pipeline"
echo "=========================================="
echo ""

if [[ -n "$STATIONS" ]]; then
    echo "Mode: custom"
    echo "Stations: $STATIONS"
elif [[ "$MODE" == "test" ]]; then
    echo "Mode: test (5 stations, ~30 seconds)"
    echo "Output: tides-test.db"
else
    echo "Mode: production (3,379 stations, ~30-45 minutes)"
    echo "Output: tides.db"
fi

echo ""
echo "=========================================="
echo ""

# Build argument strings for Python scripts
FETCH_ARGS="--mode $MODE"
BUILD_ARGS="--mode $MODE"

if [[ -n "$STATIONS" ]]; then
    FETCH_ARGS="$FETCH_ARGS --stations $STATIONS"
fi

if [[ -n "$VERBOSE" ]]; then
    FETCH_ARGS="$FETCH_ARGS $VERBOSE"
fi

# Run data pipeline
echo "Step 1: Fetching NOAA station data..."
echo "Running: uv run fetch_noaa_data.py $FETCH_ARGS"
echo ""
uv run fetch_noaa_data.py $FETCH_ARGS

echo ""
echo "Step 2: Building SQLite database..."
echo "Running: uv run build_database.py $BUILD_ARGS"
echo ""
uv run build_database.py $BUILD_ARGS

echo ""
echo "=========================================="
echo "Pipeline complete!"
echo "=========================================="
echo ""

# Show appropriate copy command based on mode
if [[ "$MODE" == "test" ]]; then
    DB_FILE="tides-test.db"
    if [[ -n "$STATIONS" ]]; then
        echo "Test database created with custom station list."
    else
        echo "Test database created with 5 stations."
    fi
    echo ""
    echo "To use in app, copy to assets:"
    echo "  cp $DB_FILE ../../app/src/main/assets/tides.db"
else
    DB_FILE="tides.db"
    if [[ -n "$STATIONS" ]]; then
        echo "Production database created with custom station list."
    else
        echo "Production database created with all stations."
    fi
    echo ""
    echo "To use in app, copy to assets:"
    echo "  cp $DB_FILE ../../app/src/main/assets/tides.db"
fi

echo ""
