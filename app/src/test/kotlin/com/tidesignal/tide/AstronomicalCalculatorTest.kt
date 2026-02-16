package com.tidesignal.tide

import org.junit.Test
import java.time.Instant
import kotlin.math.abs
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for AstronomicalCalculator.
 *
 * Tests validate astronomical factor calculations (node factors and equilibrium
 * arguments) against known values from NOAA publications and physical constraints.
 *
 * TODO: Add validation tests comparing against published NOAA astronomical argument
 * tables from Special Publication 98 for known epochs (J2000, etc.) to verify
 * absolute accuracy, not just internal consistency.
 */
class AstronomicalCalculatorTest {

    @Test
    fun `test M2 node factor is within valid range`() {
        // M2 (principal lunar semidiurnal) should have node factor between 0.95 and 1.05
        // over the 18.6-year lunar node cycle
        val time = Instant.parse("2026-02-12T00:00:00Z")
        val m2 = Constituents.getConstituent("M2")!!

        val nodeFactor = AstronomicalCalculator.calculateNodeFactor(m2, time)

        assertTrue(nodeFactor in 0.95..1.05,
            "M2 node factor should be in range 0.95-1.05, got $nodeFactor")
    }

    @Test
    fun `test S2 node factor is always 1_0`() {
        // S2 (principal solar semidiurnal) should always have node factor = 1.0
        // because it has no lunar modulation
        val times = listOf(
            Instant.parse("2020-01-01T00:00:00Z"),
            Instant.parse("2026-02-12T00:00:00Z"),
            Instant.parse("2030-06-15T12:00:00Z")
        )

        val s2 = Constituents.getConstituent("S2")!!

        for (time in times) {
            val nodeFactor = AstronomicalCalculator.calculateNodeFactor(s2, time)
            assertEquals(1.0, nodeFactor, 0.0001,
                "S2 node factor should always be 1.0 at $time")
        }
    }

    @Test
    fun `test K1 node factor is within valid range`() {
        // K1 (lunisolar diurnal) should have node factor between 0.9 and 1.2
        val time = Instant.parse("2026-02-12T00:00:00Z")
        val k1 = Constituents.getConstituent("K1")!!

        val nodeFactor = AstronomicalCalculator.calculateNodeFactor(k1, time)

        assertTrue(nodeFactor in 0.9..1.2,
            "K1 node factor should be in range 0.9-1.2, got $nodeFactor")
    }

    @Test
    fun `test O1 node factor is within valid range`() {
        // O1 (principal lunar diurnal) should have node factor between 0.9 and 1.2
        val time = Instant.parse("2026-02-12T00:00:00Z")
        val o1 = Constituents.getConstituent("O1")!!

        val nodeFactor = AstronomicalCalculator.calculateNodeFactor(o1, time)

        assertTrue(nodeFactor in 0.9..1.2,
            "O1 node factor should be in range 0.9-1.2, got $nodeFactor")
    }

    @Test
    fun `test node factors for M2 and N2 are similar`() {
        // M2 and N2 have similar node factor formulas
        val time = Instant.parse("2026-02-12T00:00:00Z")

        val m2 = Constituents.getConstituent("M2")!!
        val n2 = Constituents.getConstituent("N2")!!

        val m2NodeFactor = AstronomicalCalculator.calculateNodeFactor(m2, time)
        val n2NodeFactor = AstronomicalCalculator.calculateNodeFactor(n2, time)

        // M2 and N2 should have similar node factors (both use 2*(N-xi) term)
        assertTrue(abs(m2NodeFactor - n2NodeFactor) < 0.01,
            "M2 and N2 node factors should be similar, got M2=$m2NodeFactor, N2=$n2NodeFactor")
    }

    @Test
    fun `test equilibrium argument is normalized to 0-360 degrees`() {
        val time = Instant.parse("2026-02-12T12:00:00Z")

        // Test several major constituents
        val constituents = listOf("M2", "S2", "K1", "O1", "N2")

        for (name in constituents) {
            val constituent = Constituents.getConstituent(name)!!
            val vPlusU = AstronomicalCalculator.calculateEquilibriumArgument(constituent, time)

            assertTrue(vPlusU in 0.0..360.0,
                "$name equilibrium argument should be 0-360°, got $vPlusU")
        }
    }

    @Test
    fun `test different constituents have different equilibrium arguments`() {
        val time = Instant.parse("2026-02-12T12:00:00Z")

        // Get equilibrium arguments for different constituents
        val m2 = Constituents.getConstituent("M2")!!
        val s2 = Constituents.getConstituent("S2")!!
        val k1 = Constituents.getConstituent("K1")!!

        val m2VPlusU = AstronomicalCalculator.calculateEquilibriumArgument(m2, time)
        val s2VPlusU = AstronomicalCalculator.calculateEquilibriumArgument(s2, time)
        val k1VPlusU = AstronomicalCalculator.calculateEquilibriumArgument(k1, time)

        // Different constituents should have different equilibrium arguments
        // (unless by coincidence they're the same at this specific time)
        val diff1 = abs(m2VPlusU - s2VPlusU)
        val diff2 = abs(m2VPlusU - k1VPlusU)
        val diff3 = abs(s2VPlusU - k1VPlusU)

        // Check that not all three are nearly identical
        val allSame = (diff1 < 1.0 && diff2 < 1.0 && diff3 < 1.0)
        assertFalse(allSame,
            "M2, S2, and K1 should have different equilibrium arguments at $time. " +
            "Got M2=$m2VPlusU°, S2=$s2VPlusU°, K1=$k1VPlusU°")
    }

    @Test
    fun `test equilibrium argument is continuous across midnight`() {
        val m2 = Constituents.getConstituent("M2")!!

        val beforeMidnight = Instant.parse("2026-02-12T23:55:00Z")
        val afterMidnight = Instant.parse("2026-02-13T00:05:00Z")

        val vPlusUBefore = AstronomicalCalculator.calculateEquilibriumArgument(m2, beforeMidnight)
        val vPlusUAfter = AstronomicalCalculator.calculateEquilibriumArgument(m2, afterMidnight)

        // Allow for 360° wraparound
        val diff = abs(vPlusUAfter - vPlusUBefore)
        val adjustedDiff = if (diff > 180) 360 - diff else diff

        // Should change gradually over 10 minutes (< 30 degrees)
        assertTrue(adjustedDiff < 30.0,
            "Equilibrium argument should be continuous across midnight")
    }

    @Test
    fun `test all major constituents have valid node factors`() {
        val time = Instant.parse("2026-02-12T00:00:00Z")

        // Test all 37 NOAA constituents
        val constituents = Constituents.ALL

        for (constituent in constituents) {
            val nodeFactor = AstronomicalCalculator.calculateNodeFactor(constituent, time)

            // Node factor should be positive and reasonably close to 1.0
            assertTrue(nodeFactor > 0.0,
                "${constituent.name} node factor should be positive")
            assertTrue(nodeFactor < 2.0,
                "${constituent.name} node factor should be < 2.0, got $nodeFactor")
        }
    }

    @Test
    fun `test all major constituents have valid equilibrium arguments`() {
        val time = Instant.parse("2026-02-12T00:00:00Z")

        // Test all 37 NOAA constituents
        val constituents = Constituents.ALL

        for (constituent in constituents) {
            val vPlusU = AstronomicalCalculator.calculateEquilibriumArgument(constituent, time)

            // Should be normalized to 0-360°
            assertTrue(vPlusU >= 0.0,
                "${constituent.name} equilibrium argument should be >= 0")
            assertTrue(vPlusU < 360.0,
                "${constituent.name} equilibrium argument should be < 360, got $vPlusU")
        }
    }

    @Test
    fun `test compound constituent node factors are products`() {
        val time = Instant.parse("2026-02-12T00:00:00Z")

        val m2 = Constituents.getConstituent("M2")!!
        val m4 = Constituents.getConstituent("M4")!! // M4 = 2*M2

        val m2NodeFactor = AstronomicalCalculator.calculateNodeFactor(m2, time)
        val m4NodeFactor = AstronomicalCalculator.calculateNodeFactor(m4, time)

        // M4 node factor should be M2^2 (approximately)
        val expectedM4 = m2NodeFactor * m2NodeFactor

        assertEquals(expectedM4, m4NodeFactor, 0.001,
            "M4 node factor should be square of M2 node factor")
    }

    @Test
    fun `test solar constituents have node factor 1_0`() {
        val time = Instant.parse("2026-02-12T00:00:00Z")

        // All purely solar constituents should have node factor = 1.0
        val solarConstituents = listOf("S2", "S4", "S6", "Ssa", "Sa", "T2", "R2", "S1")

        for (name in solarConstituents) {
            val constituent = Constituents.getConstituent(name) ?: continue
            val nodeFactor = AstronomicalCalculator.calculateNodeFactor(constituent, time)

            assertEquals(1.0, nodeFactor, 0.0001,
                "$name (solar) should have node factor = 1.0")
        }
    }

    @Test
    fun `test equilibrium argument consistency`() {
        // Same time should give same result every time
        val time = Instant.parse("2026-02-12T12:00:00Z")
        val m2 = Constituents.getConstituent("M2")!!

        val vPlusU1 = AstronomicalCalculator.calculateEquilibriumArgument(m2, time)
        val vPlusU2 = AstronomicalCalculator.calculateEquilibriumArgument(m2, time)

        assertEquals(vPlusU1, vPlusU2, 0.0001,
            "Same time should give same equilibrium argument")
    }

    @Test
    fun `test node factor consistency`() {
        // Same time should give same result every time
        val time = Instant.parse("2026-02-12T12:00:00Z")
        val m2 = Constituents.getConstituent("M2")!!

        val nodeFactor1 = AstronomicalCalculator.calculateNodeFactor(m2, time)
        val nodeFactor2 = AstronomicalCalculator.calculateNodeFactor(m2, time)

        assertEquals(nodeFactor1, nodeFactor2, 0.0001,
            "Same time should give same node factor")
    }

    @Test
    fun `test leap year handling in astronomical calculations`() {
        // Test on leap day
        val leapDay = Instant.parse("2024-02-29T12:00:00Z")
        val m2 = Constituents.getConstituent("M2")!!

        val nodeFactor = AstronomicalCalculator.calculateNodeFactor(m2, leapDay)
        val vPlusU = AstronomicalCalculator.calculateEquilibriumArgument(m2, leapDay)

        // Should not crash and should return valid values
        assertTrue(nodeFactor in 0.95..1.05, "Node factor should be valid on leap day")
        assertTrue(vPlusU in 0.0..360.0, "Equilibrium argument should be valid on leap day")
    }

    @Test
    fun `test year boundary handling in astronomical calculations`() {
        // Test across New Year
        val beforeNewYear = Instant.parse("2025-12-31T23:30:00Z")
        val afterNewYear = Instant.parse("2026-01-01T00:30:00Z")

        val m2 = Constituents.getConstituent("M2")!!

        val nodeFactorBefore = AstronomicalCalculator.calculateNodeFactor(m2, beforeNewYear)
        val nodeFactorAfter = AstronomicalCalculator.calculateNodeFactor(m2, afterNewYear)

        // Should be continuous across year boundary
        assertTrue(abs(nodeFactorBefore - nodeFactorAfter) < 0.001,
            "Node factor should be continuous across year boundary")
    }
}
