package ru.planmaster3d.app

import org.junit.Assert.*
import org.junit.Test

class ConstructionCalculatorTest {
    @Test fun demolitionUsesBothBagVolumeAndWeightLimits() {
        val result = DemolitionCalculator.calculate(
            DemolitionInput(
                lengthM = 4.0,
                heightM = 2.5,
                thicknessMm = 10.0,
                densityKgM3 = 2300.0,
                bulkingFactor = 1.35,
                bagVolumeLitres = 60.0,
                bagMaxKg = 25.0,
                bagFillFactor = 0.85
            )
        )
        assertEquals(10.0, result.areaM2, 1e-8)
        assertEquals(0.1, result.solidVolumeM3, 1e-8)
        assertEquals(230.0, result.solidMassKg, 1e-8)
        assertEquals(0.135, result.looseVolumeM3, 1e-8)
        assertEquals(10, result.bagsRequired)
        assertEquals(10, result.bagsByWeight)
    }

    @Test fun aacPartitionCountsBlocksAndAdhesiveAfterOpenings() {
        val result = PartitionCalculator.calculate(
            PartitionInput(
                kind = PartitionKind.AAC_BLOCK,
                lengthM = 3.0,
                heightM = 2.7,
                thicknessMm = 100,
                openingsAreaM2 = 0.9,
                reservePercent = 7.0,
                blockLengthMm = 600,
                blockHeightMm = 250,
                blockDensityKgM3 = 600.0
            )
        )
        assertEquals(8.1, result.grossAreaM2, 1e-8)
        assertEquals(7.2, result.netAreaM2, 1e-8)
        assertEquals(52, result.blockCount)
        assertEquals(432.0, result.blockMassKg, 1e-8)
        assertEquals(38.52, result.adhesiveKg, 1e-8)
    }


    @Test fun pgpPartitionUsesSelectedPlateFormat() {
        val result = PartitionCalculator.calculate(
            PartitionInput(
                kind = PartitionKind.PGP,
                lengthM = 3.0,
                heightM = 2.7,
                thicknessMm = 80,
                blockLengthMm = 667,
                blockHeightMm = 500,
                blockDensityKgM3 = 1100.0
            )
        )
        assertEquals(25, result.blockCount)
        assertEquals(712.8, result.blockMassKg, 1e-8)
    }

    @Test fun brickCountScalesWithWallThickness() {
        val halfBrick = PartitionCalculator.calculate(
            PartitionInput(PartitionKind.BRICK, 1.0, 1.0, 120, reservePercent = 0.0,
                blockLengthMm = 250, blockHeightMm = 65, blockDensityKgM3 = 1800.0)
        )
        val fullBrick = PartitionCalculator.calculate(
            PartitionInput(PartitionKind.BRICK, 1.0, 1.0, 250, reservePercent = 0.0,
                blockLengthMm = 250, blockHeightMm = 65, blockDensityKgM3 = 1800.0)
        )
        assertEquals(62, halfBrick.blockCount)
        assertEquals(129, fullBrick.blockCount)
    }

    @Test fun drywallPartitionCalculatesBoardsStudsAndInsulation() {
        val result = PartitionCalculator.calculate(
            PartitionInput(
                kind = PartitionKind.GYPSUM_BOARD,
                lengthM = 3.0,
                heightM = 2.7,
                thicknessMm = 100,
                reservePercent = 7.0,
                boardLayersPerSide = 1,
                boardSides = 2,
                studSpacingMm = 600,
                insulation = true
            )
        )
        assertEquals(6, result.boardSheets)
        assertEquals(6, result.studs)
        assertEquals(6.0, result.trackLengthM, 1e-8)
        assertEquals(3, result.trackPieces)
        assertEquals(0.81, result.insulationVolumeM3, 1e-8)
        assertEquals(28.35, result.insulationMassKg, 1e-8)
    }
}
