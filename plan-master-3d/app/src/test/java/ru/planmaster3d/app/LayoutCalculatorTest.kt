package ru.planmaster3d.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LayoutCalculatorTest {
    @Test fun calculatesGridAndReserve() {
        val result = LayoutCalculator.calculate(LayoutCalculationInput(
            600.0, 600.0, 4000.0, 5000.0, reservePercent = 10.0
        ))
        assertEquals(7, result.columns)
        assertEquals(9, result.rows)
        assertEquals(63, result.pieces)
        assertEquals(70, result.piecesWithReserve)
        assertEquals(20.0, result.roomAreaM2, 1e-8)
    }

    @Test fun choosesBetterOrientation() {
        val result = LayoutCalculator.calculate(LayoutCalculationInput(
            1200.0, 200.0, 200.0, 1200.0, jointMm = 0.0, reservePercent = 0.0
        ))
        assertTrue(result.rotated)
        assertEquals(1, result.pieces)
    }

    @Test fun rejectsInvalidDimensionsAndReserve() {
        org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
            LayoutCalculator.calculate(LayoutCalculationInput(0.0, 100.0, 1000.0, 1000.0))
        }
        org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
            LayoutCalculator.calculate(LayoutCalculationInput(100.0, 100.0, 1000.0, 1000.0, reservePercent = 101.0))
        }
    }
}