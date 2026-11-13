package ru.planmaster3d.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class MaterialCalculatorTest {
    @Test fun dryMixUsesLayerThicknessAndReserve() {
        val result = MaterialCalculator.calculate(
            MaterialCalculationInput(10.0, 10.0, 1.2, 25.0, 10.0, true)
        )
        assertEquals(120.0, result.baseAmount, 1e-8)
        assertEquals(132.0, result.amountWithReserve, 1e-8)
        assertEquals(6, result.packages)
    }

    @Test fun paintUsesAreaRateWithoutLayerThickness() {
        val result = MaterialCalculator.calculate(
            MaterialCalculationInput(10.0, 8.0, 0.15, 1.0, 10.0, false)
        )
        assertEquals(1.5, result.baseAmount, 1e-8)
        assertEquals(1.65, result.amountWithReserve, 1e-8)
        assertEquals(2, result.packages)
    }

    @Test fun rejectsOverflowingConsumption() {
        assertThrows(IllegalArgumentException::class.java) {
            MaterialCalculator.calculate(
                MaterialCalculationInput(1.0e308, 1.0, 2.0, 25.0, 0.0, true)
            )
        }
    }

    @Test fun rejectsPackageCountOverflow() {
        assertThrows(IllegalArgumentException::class.java) {
            MaterialCalculator.calculate(
                MaterialCalculationInput(1.0e100, 1.0, 1.0, 1.0, 0.0, false)
            )
        }
    }
}
