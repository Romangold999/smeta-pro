package ru.planmaster3d.app

import org.junit.Assert.assertEquals
import org.junit.Test

class ConstructionCalculatorSpec {
    @Test fun demolitionRespectsBagVolumeAndWeight() {
        val r = DemolitionCalculator.calculate(DemolitionInput(4.0, 2.5, 10.0, 2300.0, 1.35, 60.0, 25.0, 0.85))
        assertEquals(10.0, r.areaM2, 1e-8)
        assertEquals(230.0, r.solidMassKg, 1e-8)
        assertEquals(10, r.bagsRequired)
    }

    @Test fun aacBlockCountAccountsForOpeningsAndReserve() {
        val r = PartitionCalculator.calculate(PartitionInput(PartitionKind.AAC_BLOCK, 3.0, 2.7, 100,
            openingsAreaM2 = 0.9, reservePercent = 7.0, blockLengthMm = 600,
            blockHeightMm = 250, blockDensityKgM3 = 600.0))
        assertEquals(52, r.blockCount)
        assertEquals(432.0, r.blockMassKg, 1e-8)
        assertEquals(38.52, r.adhesiveKg, 1e-8)
    }

    @Test fun pgpCountUsesPlateFaceArea() {
        val r = PartitionCalculator.calculate(PartitionInput(PartitionKind.PGP, 3.0, 2.7, 80,
            blockLengthMm = 667, blockHeightMm = 500, blockDensityKgM3 = 1100.0))
        assertEquals(26, r.blockCount)
        assertEquals(712.8, r.blockMassKg, 1e-8)
    }

    @Test fun brickCountScalesWithThickness() {
        val half = PartitionCalculator.calculate(PartitionInput(PartitionKind.BRICK, 1.0, 1.0, 120,
            reservePercent = 0.0, blockLengthMm = 250, blockHeightMm = 65, blockDensityKgM3 = 1800.0))
        val full = PartitionCalculator.calculate(PartitionInput(PartitionKind.BRICK, 1.0, 1.0, 250,
            reservePercent = 0.0, blockLengthMm = 250, blockHeightMm = 65, blockDensityKgM3 = 1800.0))
        assertEquals(62, half.blockCount)
        assertEquals(129, full.blockCount)
    }

    @Test fun drywallCountsSheetsStudsAndInsulation() {
        val r = PartitionCalculator.calculate(PartitionInput(PartitionKind.GYPSUM_BOARD, 3.0, 2.7, 100,
            boardLayersPerSide = 1, boardSides = 2, studSpacingMm = 600, insulation = true))
        assertEquals(6, r.boardSheets)
        assertEquals(7, r.studs)
        assertEquals(6.0, r.trackLengthM, 1e-8)
        assertEquals(25.32, r.profileTotalLengthM, 1e-8)
        assertEquals(0.8667, r.insulationVolumeM3, 1e-8)
        assertEquals(30.3345, r.insulationMassKg, 1e-8)
    }
}
