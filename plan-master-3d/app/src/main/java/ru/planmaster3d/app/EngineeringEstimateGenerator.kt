package ru.planmaster3d.app

/** Converts placed electrical/plumbing points to estimate rows using only explicit catalog rates. */
object EngineeringEstimateGenerator {
    private val rateByKind = mapOf(
        "Розетка" to ("Установка розетки" to "Электрика"),
        "Выключатель" to ("Монтаж выключателя" to "Электрика"),
        "Светильник" to ("Установка светильника" to "Электрика"),
        "Вентилятор" to ("Установка вентилятора" to "Электрика"),
        "Водорозетка" to ("Распайка ПП трубы (1 точка)" to "Сантехника"),
        "Канализация" to ("Канализация" to "Сантехника"),
        "Унитаз" to ("Установка унитаза" to "Сантехника"),
        "Раковина" to ("Установка умывальника" to "Сантехника"),
        "Смеситель" to ("Установка смесителя" to "Сантехника"),
        "Душ" to ("Установка системы тропический душ" to "Сантехника"),
        "Полотенцесушитель" to ("Установка полотенцесушителя" to "Сантехника"),
        "Ванна" to ("Установка, сборка, подключение ванны (акрил, металл)" to "Сантехника"),
        "Водомер" to ("Установка водомера" to "Сантехника"),
        "Инсталляция" to ("Установка, подключение инсталляции" to "Сантехника")
    )

    fun generate(points: List<EngineeringPoint>, catalog: List<CatalogWork>): List<EstimateLine> {
        require(points.all { it.x.isFinite() && it.y.isFinite() && it.roomIndex >= 0 }) {
            "Инженерные точки содержат некорректные координаты"
        }
        val rates = catalog.associateBy { it.n }
        return points.groupBy { it.kind }.mapNotNull { (kind, placed) ->
            val (rateName, category) = rateByKind[kind] ?: return@mapNotNull null
            val rate = rates[rateName] ?: return@mapNotNull null
            require(rate.p.isFinite() && rate.p >= 0.0) { "Некорректная цена для «$rateName»" }
            EstimateLine(
                name = "Инженерия · $kind",
                unit = rate.u,
                price = rate.p,
                qty = placed.size.toDouble(),
                category = "Инженерия · $category",
                material = false
            )
        }
    }
}
