#!/usr/bin/env python3
"""
Fetch tide station data from NOAA CO-OPS API.

This script queries the NOAA Tides & Currents API to retrieve:
- All harmonic tide prediction stations
- Harmonic constituent data for each station
- Station metadata (location, name, timezone, etc.)

Output: stations.json with all collected data
"""

import argparse
import json
import sys
import time
from typing import Any, Dict, List, Optional
from urllib.parse import urlencode

import requests

# NOAA API endpoints
STATIONS_API = "https://api.tidesandcurrents.noaa.gov/mdapi/prod/webapi/stations.json"
HARCON_API = "https://api.tidesandcurrents.noaa.gov/mdapi/prod/webapi/stations/{station_id}/harcon.json"

# API request settings
REQUEST_TIMEOUT = 30  # seconds
REQUEST_DELAY = 0.5  # seconds between requests to be respectful
MAX_RETRIES = 3  # maximum number of retries for failed requests

# Test mode stations - diverse geographic coverage
TEST_STATIONS = [
    "9414290",  # San Francisco, CA
    "8454000",  # Providence, RI
    "8518750",  # The Battery, NY
    "8658120",  # Wilmington, NC
    "8636580",  # Cape Hatteras, NC
]


def parse_arguments() -> argparse.Namespace:
    """
    Parse command-line arguments.

    Returns:
        Parsed arguments
    """
    parser = argparse.ArgumentParser(
        description="Fetch tide station data from NOAA CO-OPS API",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  %(prog)s                              # Test mode (5 stations, ~30 seconds)
  %(prog)s --mode production            # Production mode (all stations, ~30-45 min)
  %(prog)s --stations 9414290,8454000   # Custom station list
  %(prog)s --mode test --verbose        # Test mode with detailed logging
        """
    )

    parser.add_argument(
        "--mode",
        choices=["test", "production"],
        default="test",
        help="Database mode: 'test' for 5 stations (default), 'production' for all stations"
    )

    parser.add_argument(
        "--stations",
        type=str,
        help="Comma-separated list of station IDs (overrides --mode)"
    )

    parser.add_argument(
        "--output",
        type=str,
        default="stations.json",
        help="Output filename (default: stations.json)"
    )

    parser.add_argument(
        "--verbose",
        action="store_true",
        help="Enable detailed logging"
    )

    return parser.parse_args()


def filter_stations(all_stations: List[Dict[str, Any]], station_ids: List[str], verbose: bool = False) -> List[Dict[str, Any]]:
    """
    Filter stations by ID list.

    Args:
        all_stations: Full list of stations
        station_ids: List of station IDs to include
        verbose: Enable detailed logging

    Returns:
        Filtered list of stations
    """
    # Create lookup dict for fast filtering
    id_set = set(station_ids)
    filtered = [s for s in all_stations if s.get("id") in id_set]

    # Warn about missing stations
    found_ids = {s.get("id") for s in filtered}
    missing_ids = id_set - found_ids

    if missing_ids:
        print(f"Warning: {len(missing_ids)} requested station(s) not found:", file=sys.stderr)
        for station_id in sorted(missing_ids):
            print(f"  - {station_id}", file=sys.stderr)

    if verbose:
        print(f"Filtered {len(all_stations)} stations to {len(filtered)} stations")

    return filtered


def fetch_stations() -> List[Dict[str, Any]]:
    """
    Fetch all tide prediction stations from NOAA.

    Returns:
        List of station dictionaries
    """
    print("Fetching tide stations from NOAA API...")

    params = {
        "type": "tidepredictions",
        "expand": "details"
    }

    url = f"{STATIONS_API}?{urlencode(params)}"

    try:
        response = requests.get(url, timeout=REQUEST_TIMEOUT)
        response.raise_for_status()
        data = response.json()

        stations = data.get("stations", [])
        print(f"Found {len(stations)} tide prediction stations")

        return stations

    except requests.exceptions.RequestException as e:
        print(f"Error fetching stations: {e}", file=sys.stderr)
        sys.exit(1)


def fetch_harmonic_constituents(station_id: str, verbose: bool = False) -> Optional[Dict[str, Any]]:
    """
    Fetch harmonic constituent data for a specific station with retry logic.

    Args:
        station_id: NOAA station ID
        verbose: Enable detailed logging

    Returns:
        Dictionary with harmonic constituents, or None if not available

    Note:
        As of 2026, NOAA's CO-OPS API returns harmonic data for virtually all
        active tide prediction stations. Historically, many stations were
        "subordinate" (using offsets from reference stations), but NOAA has
        upgraded their network. If a station returns None, it's marked as
        subordinate for future compatibility, though this is rare in practice.
    """
    url = HARCON_API.format(station_id=station_id)

    for attempt in range(MAX_RETRIES):
        try:
            response = requests.get(url, timeout=REQUEST_TIMEOUT)

            # Some stations may not have harmonic data (e.g., subordinate stations)
            if response.status_code == 404:
                return None

            response.raise_for_status()
            data = response.json()

            return data

        except requests.exceptions.RequestException as e:
            if attempt < MAX_RETRIES - 1:
                # Exponential backoff: 1s, 2s, 4s
                wait_time = 2 ** attempt
                if verbose:
                    print(f"  Retry {attempt + 1}/{MAX_RETRIES} after {wait_time}s (error: {e})")
                time.sleep(wait_time)
            else:
                print(f"Warning: Could not fetch harmonics for {station_id} after {MAX_RETRIES} attempts: {e}", file=sys.stderr)
                return None

    return None


def main():
    """Main execution function."""
    args = parse_arguments()

    print("=" * 60)
    print("NOAA Tide Data Fetcher")
    print("=" * 60)
    print()

    # Fetch all stations from API
    all_stations = fetch_stations()

    # Determine which stations to process
    if args.stations:
        # Custom station list provided
        station_ids = [s.strip() for s in args.stations.split(",")]
        print(f"Custom mode: Processing {len(station_ids)} specified stations")
        stations = filter_stations(all_stations, station_ids, args.verbose)
    elif args.mode == "test":
        # Test mode - use predefined test stations
        print(f"Test mode: Processing {len(TEST_STATIONS)} test stations")
        stations = filter_stations(all_stations, TEST_STATIONS, args.verbose)
    else:
        # Production mode - use all stations
        print(f"Production mode: Processing all {len(all_stations)} stations")
        stations = all_stations

    if not stations:
        print("Error: No stations to process", file=sys.stderr)
        sys.exit(1)

    # Estimate time
    estimated_minutes = (len(stations) * REQUEST_DELAY) / 60
    if len(stations) < 100:
        print(f"\nFetching harmonic constituents for {len(stations)} stations...")
        print(f"Estimated time: ~{int(estimated_minutes)} minutes")
    else:
        print(f"\nFetching harmonic constituents for {len(stations)} stations...")
        print(f"This will take approximately {int(estimated_minutes)}-{int(estimated_minutes * 1.5)} minutes due to API rate limiting...")

    print()

    enriched_stations = []

    for i, station_data in enumerate(stations):
        station_id = station_data.get("id")
        station_name = station_data.get("name", "Unknown")

        # Show detailed progress for small datasets, sparse for large ones
        if len(stations) < 100 or args.verbose:
            print(f"[{i+1}/{len(stations)}] Processing station {station_id}: {station_name}...")
        elif (i + 1) % 100 == 0:
            print(f"[{i+1}/{len(stations)}] Progress: {int((i+1)/len(stations)*100)}% complete...")

        # Fetch harmonic constituents
        time.sleep(REQUEST_DELAY)  # Be respectful to API
        harmonics = fetch_harmonic_constituents(station_id, args.verbose)

        if harmonics:
            station_data["harmonics"] = harmonics
            station_data["type"] = "harmonic"
            if len(stations) < 100 or args.verbose:
                print(f"  ✓ Found {len(harmonics.get('HarmonicConstituents', []))} constituents")
        else:
            station_data["type"] = "subordinate"
            if len(stations) < 100 or args.verbose:
                print(f"  - No harmonic data (subordinate station)")

        enriched_stations.append(station_data)

    # Save to JSON file
    with open(args.output, "w") as f:
        json.dump(enriched_stations, f, indent=2)

    harmonic_count = sum(1 for s in enriched_stations if s.get('type') == 'harmonic')
    subordinate_count = sum(1 for s in enriched_stations if s.get('type') == 'subordinate')

    print()
    print(f"✓ Data saved to {args.output}")
    print(f"  Total stations: {len(enriched_stations)}")
    print(f"  Harmonic stations: {harmonic_count}")
    print(f"  Subordinate stations: {subordinate_count}")
    if subordinate_count == 0:
        print(f"  Note: All NOAA stations currently provide harmonic data (network modernization)")
    print()
    print("Next step: Run build_database.py to create SQLite database")


if __name__ == "__main__":
    main()
