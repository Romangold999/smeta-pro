package ru.planmaster3d.app

import kotlin.math.ceil
import kotlin.math.max

data class DemolitionInput(
    val lengthM: Double,
    val heightM: Double,
    val thicknessMm: Double,
    val densityKgM3: Double,
    val bulkingFactor: Double = 1.35,
    val bagVolumeLitres: Double = 60.0,
    val bagMaxKg: Double = 25.0,
    val bagFillFactor: Double = 0.85
)

data class DemolitionResult(
    val areaM2: Double,
    val solidVolumeM3: Double,
    val solidMassKg: Double,
    val looseVolumeM3: Double,
    val bagsByVolume: Int,
    val bagsByWeight: Int,
    val bagsRequired: Int,
    val estimatedBagMassKg: Double
)

object DemolitionCalculator {
    fun calculate(input: DemolitionInput): DemolitionResult {
        require(input.lengthM > 0.0 && input.heightM > 0.0)
        require(input.thicknessMm > 0.0 && input.densityKgM3 > 0.0)
        require(input.bulkingFactor >= 1.0)
        require(input.bagVolumeLitres > 0.0 && input.bagMaxKg > 0.0)
        require(input.bagFillFactor in 0.1..1.0)
        val area = input.lengthM * input.heightM
        val volume = area * input.thicknessMm / 1000.0
        val mass = volume * input.densityKgM3
        val loose = volume * input.bulkingFactor
        val bagCapacityM3 = input.bagVolumeLitres / 1000.0 * input.bagFillFactor
        val byVolume = ceil(loose / bagCapacityM3).toInt()
        val byWeight = ceil(mass / input.bagMaxKg).toInt()
        val bags = max(byVolume, byWeight)
        return DemolitionResult(area, volume, mass, loose, byVolume, byWeight, bags,
            if (bags == 0) 0.0 else mass / bags)
    }
}

enum class PartitionKind { AAC_BLOCK, GYPSUM_BOARD }

data class PartitionInput(
    val kind: PartitionKind,
    val lengthM: Double,
    val heightM: Double,
    val thicknessMm: Int,
    val openingsAreaM2: Double = 0.0,
    val reservePercent: Double = 7.0,
    // AAC block dimensions: length x height in mm; thickness is selected separately.
    val blockLengthMm: Int = 600,
    val blockHeightMm: Int = 250,
    val blockDensityKgM3: Double = 600.0,
    // Gypsum board configuration
    val boardWidthMm: Int = 1200,
    val boardHeightMm: Int = 2500,
    val boardLayersPerSide: Int = 1,
    val boardSides: Int = 2,
    val studSpacingMm: Int = 600,
    val profileStockLengthM: Double = 3.0,
    val insulation: Boolean = false,
    val insulationDensityKgM3: Double = 35.0
)

data class PartitionResult(
    val grossAreaM2: Double,
    val netAreaM2: Double,
    val wallVolumeM3: Double,
    val blockCount: Int,
    val blockMassKg: Double,
    val adhesiveKg: Double,
    val boardSheets: Int,
    val studs: Int,
    val trackLengthM: Double,
    val trackPieces: Int,
    val profileTotalLengthM: Double,
    val insulationVolumeM3: Double,
    val insulationMassKg: Double
)

object PartitionCalculator {
    fun calculate(input: PartitionInput): PartitionResult {
        require(input.lengthM > 0.0 && input.heightM > 0.0)
        require(input.thicknessMm > 0 && input.openingsAreaM2 >= 0.0)
        require(input.reservePercent >= 0.0)
        val gross = input.lengthM * input.heightM
        val net = (gross - input.openingsAreaM2).coerceAtLeast(0.0)
        val factor = 1.0 + input.reservePercent / 100.0
        val volume = net * input.thicknessMm / 1000.0
        return when (input.kind) {
            PartitionKind.AAC_BLOCK -> {
                require(input.blockLengthMm > 0 && input.blockHeightMm > 0 && input.blockDensityKgM3 > 0.0)
                val blockFaceArea = input.blockLengthMm * input.blockHeightMm / 1_000_000.0
                val blocks = ceil(net / blockFaceArea * factor).toInt()
                val mass = volume * input.blockDensityKgM3
                // Approximate thin-bed adhesive allowance; user can replace this with the product data.
                val adhesive = net * 5.0 * factor
                PartitionResult(gross, net, volume, blocks, mass, adhesive,
                    0, 0, 0.0, 0, 0.0, 0.0, 0.0)
            }
            PartitionKind.GYPSUM_BOARD -> {
                require(input.boardWidthMm > 0 && input.boardHeightMm > 0)
                require(input.boardLayersPerSide >= 1 && input.boardSides in 1..2)
                require(input.studSpacingMm > 0 && input.profileStockLengthM > 0.0)
                val sheetArea = input.boardWidthMm * input.boardHeightMm / 1_000_000.0
                val sheets = ceil(net * input.boardLayersPerSide * input.boardSides / sheetArea * factor).toInt()
                // One stud at each end plus intermediate studs at the selected centre spacing.
                val studs = ceil(input.lengthM * 1000.0 / input.studSpacingMm).toInt() + 1
                val trackLength = input.lengthM * 2.0
                val trackPieces = ceil(trackLength / input.profileStockLengthM * factor).toInt()
                val studLength = studs * input.heightM
                val totalProfile = studLength + trackLength
                val insulationVolume = if (input.insulation) net * input.thicknessMm / 1000.0 else 0.0
                val insulationMass = insulationVolume * input.insulationDensityKgM3
                PartitionResult(gross, net, volume, 0, 0.0, 0.0,
                    sheets, studs, trackLength, trackPieces, totalProfile,
                    insulationVolume, insulationMass)
            }
        }
    }
}

/** Common illustrative densities. All values are editable by the user in the calculator. */
object ConstructionMaterialPresets {
    val demolitionDensities = linkedMapOf(
        "Керамическая плитка" to 2300.0,
        "Цементная стяжка/бетон" to 2200.0,
        "Кирпичная кладка" to 1800.0,
        "Газобетон" to 600.0,
        "Гипсовая штукатурка" to 1000.0,
        "Цементная штукатурка" to 1600.0,
        "ГКЛ" to 800.0
    )
}
