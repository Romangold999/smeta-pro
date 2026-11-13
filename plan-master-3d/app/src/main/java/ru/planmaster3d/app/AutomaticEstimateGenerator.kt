package ru.planmaster3d.app

/**
 * Selects only operations that have an explicit rate in the current work catalog.
 * No placeholder prices are introduced into the estimate.
 */
data class AutomaticEstimateOptions(
    val wallPlaster: Boolean = false,
    val wallPrimer: Boolean = false,
    val floorScreed: Boolean = false,
    val ceilingPrimer: Boolean = false,
    val wallWaterproofing: Boolean = false,
    val floorWaterproofing: Boolean = false,
    val wallTile: Boolean = false,
    val floorTile: Boolean = false
)

object AutomaticEstimateGenerator {
    private const val AUTO_CATEGORY = "Автообъёмы"

    fun generate(
        takeoff: ProjectGeometryTakeoff,
        catalog: List<CatalogWork>,
        options: AutomaticEstimateOptions
    ): List<EstimateLine> {
        val areas = listOf(
            takeoff.floorAreaM2, takeoff.ceilingAreaM2, takeoff.netWallAreaM2,
            takeoff.grossWallAreaM2, takeoff.openingAreaM2, takeoff.revealAreaM2
        )
        require(areas.all { it.isFinite() && it >= 0.0 }) {
            "Площади для автоматической сметы должны быть конечными и неотрицательными"
        }

        val rates = catalog.associateBy { it.n }
        val generated = mutableListOf<EstimateLine>()

        fun add(enabled: Boolean, catalogName: String, title: String, quantity: Double) {
            if (!enabled || quantity <= 0.0) return
            val source = rates[catalogName] ?: return
            require(source.p.isFinite() && source.p >= 0.0) {
                "В каталоге указана некорректная цена для «$catalogName»"
            }
            generated += EstimateLine(
                name = "Авто · $title",
                unit = source.u,
                price = source.p,
                qty = quantity,
                category = AUTO_CATEGORY,
                material = false
            )
        }

        add(options.wallPlaster, "Штукатурка ЦПС до 3 см", "Штукатурка стен по геометрии", takeoff.netWallAreaM2)
        add(options.wallPrimer, "Грунтование", "Грунтование стен по геометрии", takeoff.netWallAreaM2)
        add(options.wallWaterproofing, "Гидроизоляция (2 слоя)", "Гидроизоляция стен по геометрии", takeoff.netWallAreaM2)
        add(options.floorScreed, "Стяжка до 3 см", "Стяжка пола по геометрии", takeoff.floorAreaM2)
        add(options.floorWaterproofing, "Гидроизоляция (2 слоя)", "Гидроизоляция пола по геометрии", takeoff.floorAreaM2)
        add(options.floorTile, "Укладка керамогранита 60×60, 60×20, 120×20", "Укладка керамогранита на пол", takeoff.floorAreaM2)
        add(options.wallTile, "Укладка керамогранита 60×60, 60×20, 120×20", "Облицовка стен керамогранитом", takeoff.netWallAreaM2)
        add(options.ceilingPrimer, "Грунтование", "Грунтование потолка по геометрии", takeoff.ceilingAreaM2)

        return generated
    }
}
