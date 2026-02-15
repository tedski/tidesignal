package com.tidewatch.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.tidewatch.data.models.HarmonicConstituent

/**
 * Data Access Object for HarmonicConstituent entities.
 */
@Dao
interface HarmonicConstituentDao {

    @Query("SELECT * FROM harmonic_constituents WHERE stationId = :stationId")
    suspend fun getConstituentsForStation(stationId: String): List<HarmonicConstituent>

    @Query("""
        SELECT * FROM harmonic_constituents
        WHERE stationId = :stationId
        AND constituentName = :constituentName
        LIMIT 1
    """)
    suspend fun getConstituent(
        stationId: String,
        constituentName: String
    ): HarmonicConstituent?

    @Query("SELECT COUNT(*) FROM harmonic_constituents WHERE stationId = :stationId")
    suspend fun getConstituentCount(stationId: String): Int

    @Query("SELECT DISTINCT constituentName FROM harmonic_constituents ORDER BY constituentName")
    suspend fun getAllConstituentNames(): List<String>

    @Query("SELECT * FROM harmonic_constituents")
    suspend fun getAllConstituents(): List<HarmonicConstituent>
}
