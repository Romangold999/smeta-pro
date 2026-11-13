package ru.planmaster3d.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EngineeringEstimateGeneratorTest {
    @Test fun groupsPointsAndUsesOnlyCatalogRates() {
        val points = listOf(
            EngineeringPoint("1", "Розетка", 0, 1.0, 1.0),
            EngineeringPoint("2", "Розетка", 0, 2.0, 1.0),
            EngineeringPoint("3", "Светильник", 0, 2.0, 2.0),
            EngineeringPoint("4", "Выключатель", 0, 0.2, 0.2)
        )
        val catalog = listOf(
            CatalogWork("Установка розетки", 250.0, "шт", "Электрика"),
            CatalogWork("Установка светильника", 800.0, "шт", "Электрика")
        )
        val result = EngineeringEstimateGenerator.generate(points, catalog)
        assertEquals(2, result.size)
        assertEquals(2.0, result.first { it.name.endsWith("Розетка") }.qty, 0.0)
        assertEquals(250.0, result.first { it.name.endsWith("Розетка") }.price, 0.0)
        assertTrue(result.none { it.name.endsWith("Выключатель") })
    }

    @Test fun rejectsInvalidCoordinates() {
        org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
            EngineeringEstimateGenerator.generate(
                listOf(EngineeringPoint("bad", "Розетка", 0, Double.NaN, 1.0)),
                originalCatalog
            )
        }
    }
}
