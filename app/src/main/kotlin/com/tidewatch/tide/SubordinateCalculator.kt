package com.tidewatch.tide

import com.tidewatch.data.models.SubordinateOffset
import com.tidewatch.data.models.TideExtremum
import com.tidewatch.data.models.TideHeight
import java.time.Instant
import kotlin.math.abs

/**
 * Helper class for handling subordinate tide station calculations.
 *
 * Subordinate stations don't have their own harmonic constituents. Instead, they reference
 * a nearby harmonic station and apply time/height offsets to approximate local conditions.
 *
 * This class handles:
 * - Determining if a station is subordinate
 * - Resolving reference station IDs
 * - Applying appropriate height multipliers based on tide phase
 * - Applying time offsets for extrema
 *
 * @property subordinateOffsets Map of station ID to subordinate offset data
 */
internal class SubordinateCalculator(
    private val subordinateOffsets: Map<String, SubordinateOffset>
) {
    /**
     * Check if a station is a subordinate station.
     *
     * @param stationId Station identifier to check
     * @return true if station has subordinate offset data
     */
    fun isSubordinateStation(stationId: String): Boolean {
        return subordinateOffsets.containsKey(stationId)
    }

    /**
     * Get the reference station ID for a subordinate station.
     *
     * @param stationId Subordinate station identifier
     * @return Reference station ID, or null if not a subordinate station
     */
    fun getReferenceStationId(stationId: String): String? {
        return subordinateOffsets[stationId]?.referenceStationId
    }

    /**
     * Determine which height multiplier to use based on tide phase.
     *
     * Uses the rate of change at the reference station to determine if we're
     * approaching high tide or low tide:
     * - Rising tide (positive rate) → use high tide multiplier
     * - Falling tide (negative rate) → use low tide multiplier
     * - Slack tide (near-zero rate) → use average of both multipliers
     *
     * @param stationId Subordinate station identifier
     * @param referenceRate Rate of change at reference station (ft/hr or m/hr)
     * @return Height multiplier to apply
     */
    fun getHeightMultiplier(stationId: String, referenceRate: Double): Double {
        val offset = subordinateOffsets[stationId] ?: return 1.0

        return when {
            // Near slack tide - use average of both multipliers for smooth transition
            abs(referenceRate) < TideHeight.SLACK_THRESHOLD ->
                (offset.heightOffsetHigh + offset.heightOffsetLow) / 2.0

            // Rising tide - approaching high
            referenceRate > 0 -> offset.heightOffsetHigh

            // Falling tide - approaching low
            else -> offset.heightOffsetLow
        }
    }

    /**
     * Apply height offset to reference station calculation.
     *
     * Multiplies the reference station height by the appropriate multiplier
     * based on current tide phase (high vs low).
     *
     * @param stationId Subordinate station identifier
     * @param referenceHeight Height calculated at reference station
     * @param referenceRate Rate of change at reference station
     * @return Adjusted height for subordinate station
     */
    fun applyHeightOffset(
        stationId: String,
        referenceHeight: Double,
        referenceRate: Double
    ): Double {
        val multiplier = getHeightMultiplier(stationId, referenceRate)
        return referenceHeight * multiplier
    }

    /**
     * Apply time offset to an extremum (high or low tide).
     *
     * Subordinate stations have different time offsets for high vs low tides.
     * For example, a station might be +10 minutes for high tide but +15 minutes
     * for low tide compared to its reference station.
     *
     * @param stationId Subordinate station identifier
     * @param extremumType Type of extremum (HIGH or LOW)
     * @param referenceTime Time of extremum at reference station
     * @return Adjusted time for subordinate station
     */
    fun applyTimeOffset(
        stationId: String,
        extremumType: TideExtremum.Type,
        referenceTime: Instant
    ): Instant {
        val offset = subordinateOffsets[stationId] ?: return referenceTime

        val timeOffsetMinutes = when (extremumType) {
            TideExtremum.Type.HIGH -> offset.timeOffsetHigh
            TideExtremum.Type.LOW -> offset.timeOffsetLow
        }

        return referenceTime.plusSeconds(timeOffsetMinutes * 60L)
    }
}
