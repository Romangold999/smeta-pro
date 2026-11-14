package ru.planmaster3d.app

data class ProjectGeometryTakeoff(
    val floorAreaM2: Double,
    val ceilingAreaM2: Double,
    val netWallAreaM2: Double,
    val grossWallAreaM2: Double,
    val openingAreaM2: Double,
    val revealAreaM2: Double,
    val grossWallVolumeM3: Double = 0.0,
    val netWallVolumeM3: Double = 0.0
)

object GeometryTakeoff {
    fun calculate(rooms: List<Room>): ProjectGeometryTakeoff {
        require(rooms.isNotEmpty()) { "В проекте должно быть хотя бы одно помещение" }
        val metrics = rooms.map { it.geometry }
        val result = ProjectGeometryTakeoff(
            floorAreaM2 = metrics.sumOf { it.floorArea },
            ceilingAreaM2 = metrics.sumOf { it.ceilingArea },
            netWallAreaM2 = metrics.sumOf { it.netWallArea },
            grossWallAreaM2 = metrics.sumOf { it.grossWallArea },
            openingAreaM2 = metrics.sumOf { it.openingArea },
            revealAreaM2 = metrics.sumOf { it.revealArea },
            grossWallVolumeM3 = metrics.sumOf { it.grossWallVolumeM3 },
            netWallVolumeM3 = metrics.sumOf { it.netWallVolumeM3 }
        )
        require(listOf(result.floorAreaM2, result.ceilingAreaM2, result.netWallAreaM2,
            result.grossWallAreaM2, result.openingAreaM2, result.revealAreaM2,
            result.grossWallVolumeM3, result.netWallVolumeM3).all {
            it.isFinite() && it >= 0.0
        }) { "Геометрия проекта дала недопустимые объёмы" }
        return result
    }
}
