package com.tidesignal.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.tidesignal.data.models.Station
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Station entities.
 */
@Dao
interface StationDao {

    @Query("SELECT * FROM stations WHERE id = :stationId LIMIT 1")
    suspend fun getStationById(stationId: String): Station?

    @Query("SELECT * FROM stations WHERE id = :stationId LIMIT 1")
    fun getStationByIdFlow(stationId: String): Flow<Station?>

    @Query("SELECT * FROM stations ORDER BY name ASC")
    suspend fun getAllStations(): List<Station>

    @Query("SELECT * FROM stations ORDER BY name ASC")
    fun getAllStationsFlow(): Flow<List<Station>>

    @Query("SELECT * FROM stations WHERE state = :state ORDER BY name ASC")
    suspend fun getStationsByState(state: String): List<Station>

    @Query("SELECT DISTINCT state FROM stations WHERE state != '' ORDER BY state ASC")
    suspend fun getAllStates(): List<String>

    @Query("""
        SELECT * FROM stations
        WHERE name LIKE '%' || :query || '%'
        ORDER BY name ASC
        LIMIT :limit
    """)
    suspend fun searchStationsByName(query: String, limit: Int = 50): List<Station>

    /**
     * Get stations within a geographic bounding box, sorted by distance from center.
     * Used for location-based search with haversine ordering.
     */
    @Query("""
        SELECT * FROM stations
        WHERE latitude BETWEEN :minLat AND :maxLat
        AND longitude BETWEEN :minLon AND :maxLon
        ORDER BY
            ((latitude - :centerLat) * (latitude - :centerLat) +
             (longitude - :centerLon) * (longitude - :centerLon))
        LIMIT :limit
    """)
    suspend fun getStationsInBounds(
        minLat: Double,
        maxLat: Double,
        minLon: Double,
        maxLon: Double,
        centerLat: Double,
        centerLon: Double,
        limit: Int = 20
    ): List<Station>

    @Query("SELECT * FROM stations WHERE type = 'harmonic' ORDER BY name ASC")
    suspend fun getHarmonicStations(): List<Station>

    @Query("SELECT * FROM stations WHERE type = 'subordinate' ORDER BY name ASC")
    suspend fun getSubordinateStations(): List<Station>

    @Query("""
        SELECT * FROM stations
        WHERE type = 'subordinate'
        AND referenceStationId = :referenceStationId
        ORDER BY name ASC
    """)
    suspend fun getSubordinateStationsByReference(referenceStationId: String): List<Station>

    @Query("SELECT COUNT(*) FROM stations")
    suspend fun getStationCount(): Int
}
