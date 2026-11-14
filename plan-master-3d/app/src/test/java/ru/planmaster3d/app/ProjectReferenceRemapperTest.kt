package ru.planmaster3d.app

import org.junit.Assert.assertEquals
import org.junit.Test

class ProjectReferenceRemapperTest {
    private val secondRoom = listOf(
        Vec2(2.0, 0.0), Vec2(4.0, 0.0), Vec2(4.0, 3.0), Vec2(2.0, 3.0)
    )

    private fun record(
        id: String,
        roomIndex: Int,
        start: Vec2,
        end: Vec2,
        region: List<Vec2> = emptyList()
    ) = ConstructionRecord(
        id = id, kind = "DEMOLITION", roomIndex = roomIndex, material = "Тест",
        startX = start.x, startY = start.y, endX = end.x, endY = end.y,
        lengthM = start.distanceTo(end), heightM = 2.7, thicknessMm = 10.0,
        regionPoints = region
    )

    @Test fun movesInteriorRecordsAndShiftsLaterRoomIndexes() {
        val records = listOf(
            record("first", 0, Vec2(0.2, 1.0), Vec2(1.5, 1.0)),
            record("second", 0, Vec2(2.5, 1.0), Vec2(3.5, 1.0)),
            record("later", 2, Vec2(5.0, 1.0), Vec2(6.0, 1.0))
        )
        val result = ProjectReferenceRemapper.afterRoomSplit(records, 0, secondRoom)
        assertEquals(listOf(0, 1, 3), result.map { it.roomIndex })
    }

    @Test fun keepsRecordsOnSplitBoundaryWithOriginalRoom() {
        val boundary = record("boundary", 0, Vec2(2.0, 0.5), Vec2(2.0, 2.5))
        val result = ProjectReferenceRemapper.afterRoomSplit(listOf(boundary), 0, secondRoom)
        assertEquals(0, result.single().roomIndex)
    }

    @Test fun movesDemolitionRegionOnlyWhenItsWholePolygonIsInSecondRoom() {
        val region = listOf(Vec2(2.5, 0.5), Vec2(3.5, 0.5), Vec2(3.5, 2.0), Vec2(2.5, 2.0))
        val outside = listOf(Vec2(1.0, 0.5), Vec2(3.0, 0.5), Vec2(3.0, 2.0))
        val records = listOf(
            record("inside", 0, Vec2(0.0, 0.0), Vec2(0.0, 0.0), region),
            record("crossing", 0, Vec2(0.0, 0.0), Vec2(0.0, 0.0), outside)
        )
        val result = ProjectReferenceRemapper.afterRoomSplit(records, 0, secondRoom)
        assertEquals(listOf(1, 0), result.map { it.roomIndex })
    }
}
