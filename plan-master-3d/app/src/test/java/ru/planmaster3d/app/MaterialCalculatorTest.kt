package ru.planmaster3d.app

import org.junit.Assert.*
import org.junit.Test

class MaterialCalculatorTest {
    @Test fun dryMixUsesLayerThicknessAndReserve() {
        val result = MaterialCalculator.calculate(
            area = 20.0,
            ratePerM2 = 1.2,
            packageSize = 25.0,
            wastePercent = 10.0,
            layerMm = 10.0,
            perMillimetre = true
        )
        assertEquals(264.0, result.required, 1e-8)
        assertEquals(11, result.packages)
    }

    @Test fun paintUsesAreaRateWithoutLayerMultiplier() {
        val result = MaterialCalculator.calculate(
            area = 30.0,
            ratePerM2 = 0.15,
            packageSize = 5.0,
            wastePercent = 10.0,
            layerMm = 3.0,
            perMillimetre = false
        )
        assertEquals(4.95, result.required, 1e-8)
        assertEquals(1, result.packages)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsInfiniteArea() {
        MaterialCalculator.calculate(Double.POSITIVE_INFINITY, 1.0, 10.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsPackageOverflow() {
        MaterialCalculator.calculate(Double.MAX_VALUE, 1.0, 0.01)
    }
}
