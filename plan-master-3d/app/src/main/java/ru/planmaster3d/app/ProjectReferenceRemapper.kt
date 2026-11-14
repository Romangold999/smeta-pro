package ru.planmaster3d.app

/** Keeps construction records attached to the correct room after a polygon split. */
object ProjectReferenceRemapper {
    fun afterRoomSplit(
        records: List<ConstructionRecord>,
        splitRoomIndex: Int,
        secondRoom: List<Vec2>
    ): List<ConstructionRecord> {
        require(splitRoomIndex >= 0)
        require(secondRoom.size >= 3)
        return records.map { record ->
            when {
                record.roomIndex > splitRoomIndex -> record.copy(roomIndex = record.roomIndex + 1)
                record.roomIndex < splitRoomIndex -> record
                belongsToSecondRoom(record, secondRoom) ->
                    record.copy(roomIndex = splitRoomIndex + 1)
                else -> record
            }
        }
    }

    private fun belongsToSecondRoom(record: ConstructionRecord, polygon: List<Vec2>): Boolean {
        if (record.regionPoints.size >= 3) {
            return record.regionPoints.all { GeometryEngine.containsPoint(polygon, it) } &&
                record.regionPoints.any { isStrictlyInside(it, polygon) }
        }
        val start = Vec2(record.startX, record.startY)
        val end = Vec2(record.endX, record.endY)
        if (!listOf(start.x, start.y, end.x, end.y).all(Double::isFinite)) return false
        return isStrictlyInside((start + end) * 0.5, polygon)
    }

    private fun isStrictlyInside(point: Vec2, polygon: List<Vec2>): Boolean {
        if (!GeometryEngine.containsPoint(polygon, point)) return false
        for (i in polygon.indices) {
            val a = polygon[i]
            val b = polygon[(i + 1) % polygon.size]
            val dx = b.x - a.x
            val dy = b.y - a.y
            val cross = dx * (point.y - a.y) - dy * (point.x - a.x)
            val scale = maxOf(1.0, kotlin.math.hypot(dx, dy))
            if (kotlin.math.abs(cross) <= 1e-7 * scale &&
                point.x >= minOf(a.x, b.x) - 1e-7 &&
                point.x <= maxOf(a.x, b.x) + 1e-7 &&
                point.y >= minOf(a.y, b.y) - 1e-7 &&
                point.y <= maxOf(a.y, b.y) + 1e-7
            ) return false
        }
        return true
    }
}
