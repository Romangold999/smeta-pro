package ru.planmaster3d.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class GeometryTakeoffTest {
    @Test fun aggregatesFloorWallsOpeningsAndRevealsAcrossRooms() {
        val roomOne = Room(
            1L, "Комната", 4.0, 5.0, 2.7, 4.0, 5.0, 4.0, 5.0,
            openingsByWall = mapOf(0 to listOf(
                Opening("door", "Дверь", 0.9, 2.1, depth = 0.15, offsetFromStart = 1.0)
            ))
        )
        val roomTwo = Room(2L, "Коридор", 3.0, 3.0, 2.5, 3.0, 3.0, 3.0, 3.0)
        val result = GeometryTakeoff.calculate(listOf(roomOne, roomTwo))
        assertEquals(29.0, result.floorAreaM2, 1e-8)
        assertEquals(29.0, result.ceilingAreaM2, 1e-8)
        assertEquals(1.89, result.openingAreaM2, 1e-8)
        assertEquals(78.6, result.grossWallAreaM2, 1e-8)
        assertEquals(76.71, result.netWallAreaM2, 1e-8)
        assertEquals(0.765, result.revealAreaM2, 1e-8)
    }

    @Test fun aggregatesWallVolumesUsingIndividualThicknessesAndOpenings() {
        val room = Room(
            3L, "Стена", 4.0, 3.0, 2.7, 4.0, 3.0, 4.0, 3.0,
            wallThicknesses = listOf(0.1, 0.2, 0.15, 0.1),
            openingsByWall = mapOf(0 to listOf(
                Opening("door", "Дверь", 1.0, 1.0, offsetFromStart = 1.0)
            ))
        )
        val result = GeometryTakeoff.calculate(listOf(room))
        assertEquals(6.5, result.grossWallVolumeM3, 1e-8)
        assertEquals(6.4, result.netWallVolumeM3, 1e-8)
    }

    @Test fun emptyProjectIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            GeometryTakeoff.calculate(emptyList())
        }
    }
}
