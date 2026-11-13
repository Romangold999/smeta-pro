package ru.planmaster3d.app

import kotlin.math.ceil

data class MaterialCalculationInput(
    val areaM2: Double,
    val layerMm: Double,
    val coveragePerM2: Double,
    val packageSize: Double,
    val reservePercent: Double,
    val multiplyByLayer: Boolean
)

data class MaterialCalculationResult(
    val baseAmount: Double,
    val amountWithReserve: Double,
    val packages: Int
)

object MaterialCalculator {
    fun calculate(input: MaterialCalculationInput): MaterialCalculationResult {
        val values = listOf(input.areaM2, input.layerMm, input.coveragePerM2,
            input.packageSize, input.reservePercent)
        require(values.all { it.isFinite() }) { "Параметры расхода должны быть конечными числами" }
        require(input.areaM2 >= 0.0 && input.layerMm >= 0.0 &&
            input.coveragePerM2 >= 0.0 && input.packageSize > 0.0 &&
            input.reservePercent >= 0.0) { "Площадь, расход, слой и запас должны быть неотрицательными; фасовка — больше нуля" }

        val base = input.areaM2 * input.coveragePerM2 *
            (if (input.multiplyByLayer) input.layerMm else 1.0)
        val withReserve = base * (1.0 + input.reservePercent / 100.0)
        require(base.isFinite() && withReserve.isFinite()) {
            "Расчётный расход превышает допустимый диапазон"
        }
        val packageCount = ceil(withReserve / input.packageSize)
        require(packageCount.isFinite() && packageCount <= Int.MAX_VALUE.toDouble()) {
            "Количество упаковок превышает допустимый диапазон"
        }
        return MaterialCalculationResult(base, withReserve, packageCount.toInt())
    }
}
