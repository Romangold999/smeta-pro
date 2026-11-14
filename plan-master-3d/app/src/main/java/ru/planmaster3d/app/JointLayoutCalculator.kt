package ru.planmaster3d.app

import kotlin.math.ceil
import kotlin.math.max

data class LayoutCalculationInput(
    val elementAMm: Double,
    val elementBMm: Double,
    val roomAMm: Double,
    val roomBMm: Double,
    val jointMm: Double = 2.0,
    val reservePercent: Double = 10.0
)

data class LayoutCalculationResult(
    val rows: Int,
    val columns: Int,
    val pieces: Int,
    val piecesWithReserve: Int,
    val roomAreaM2: Double,
    val purchaseAreaM2: Double,
    val wasteAreaM2: Double,
    val rotated: Boolean
)

/**
 * Rectangular grid takeoff for tile, laminate and panels.
 * Compares both element orientations and includes joints between adjacent elements.
 * Offcut reuse and non-rectangular rooms are intentionally not guessed.
 */
object JointLayoutCalculator {
    fun calculate(input: LayoutCalculationInput): LayoutCalculationResult {
        val values = listOf(input.elementAMm, input.elementBMm, input.roomAMm,
            input.roomBMm, input.jointMm, input.reservePercent)
        require(values.all { it.isFinite() }) { "Все размеры должны быть конечными числами" }
        require(input.elementAMm > 0.0 && input.elementBMm > 0.0 &&
            input.roomAMm > 0.0 && input.roomBMm > 0.0) {
            "Размеры элементов и помещения должны быть больше нуля"
        }
        require(input.jointMm >= 0.0 && input.reservePercent in 0.0..100.0) {
            "Шов должен быть неотрицательным, запас — от 0 до 100%"
        }
        data class Candidate(val rows: Int, val columns: Int, val count: Long, val rotated: Boolean)
        fun countAcross(room: Double, element: Double): Int {
            val raw = ceil((room + input.jointMm) / (element + input.jointMm))
            require(raw.isFinite() && raw in 1.0..Int.MAX_VALUE.toDouble()) {
                "Количество элементов превышает допустимый диапазон"
            }
            return raw.toInt()
        }
        fun candidate(a: Double, b: Double, rotated: Boolean): Candidate {
            val columns = countAcross(input.roomAMm, a)
            val rows = countAcross(input.roomBMm, b)
            val count = rows.toLong() * columns.toLong()
            require(count <= Int.MAX_VALUE.toLong()) {
                "Количество элементов превышает допустимый диапазон"
            }
            return Candidate(rows, columns, count, rotated)
        }
        val normal = candidate(input.elementAMm, input.elementBMm, false)
        val turned = candidate(input.elementBMm, input.elementAMm, true)
        val best = if (turned.count < normal.count) turned else normal
        val withReserve = ceil(best.count * (1.0 + input.reservePercent / 100.0))
        require(withReserve.isFinite() && withReserve <= Int.MAX_VALUE.toDouble()) {
            "Количество элементов с запасом превышает допустимый диапазон"
        }
        val roomArea = input.roomAMm * input.roomBMm / 1_000_000.0
        val elementArea = input.elementAMm * input.elementBMm / 1_000_000.0
        val purchaseArea = withReserve * elementArea
        val wasteArea = max(0.0, purchaseArea - roomArea)
        require(listOf(roomArea, purchaseArea, wasteArea).all { it.isFinite() }) {
            "Площадь раскладки превышает допустимый диапазон"
        }
        return LayoutCalculationResult(best.rows, best.columns, best.count.toInt(),
            withReserve.toInt(), roomArea, purchaseArea, wasteArea, best.rotated)
    }
}
