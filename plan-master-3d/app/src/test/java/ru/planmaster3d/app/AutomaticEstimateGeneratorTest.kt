package ru.planmaster3d.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AutomaticEstimateGeneratorTest {
    private val takeoff = ProjectGeometryTakeoff(
        floorAreaM2 = 20.0,
        ceilingAreaM2 = 20.0,
        netWallAreaM2 = 45.0,
        grossWallAreaM2 = 50.0,
        openingAreaM2 = 5.0,
        revealAreaM2 = 1.5
    )

    @Test fun generatesSelectedOperationsUsingCatalogPrices() {
        val result = AutomaticEstimateGenerator.generate(
            takeoff,
            originalCatalog,
            AutomaticEstimateOptions(
                wallPlaster = true,
                wallWaterproofing = true,
                floorScreed = true,
                floorWaterproofing = true,
                floorTile = true,
                ceilingPrimer = true
            )
        )

        assertEquals(6, result.size)
        assertEquals(45.0, result.first { it.name.contains("Штукатурка стен") }.qty, 1e-8)
        assertEquals(20.0, result.first { it.name.contains("Стяжка пола") }.qty, 1e-8)
        assertEquals(2000.0, result.first { it.name.contains("Укладка керамогранита на пол") }.price, 1e-8)
        assertEquals(350.0, result.first { it.name.contains("Гидроизоляция пола") }.price, 1e-8)
        assertTrue(result.all { it.category == "Автообъёмы" })
    }

    @Test fun missingCatalogRateDoesNotCreateZeroPriceLine() {
        val result = AutomaticEstimateGenerator.generate(
            takeoff,
            emptyList(),
            AutomaticEstimateOptions(wallPlaster = true, floorTile = true)
        )
        assertTrue(result.isEmpty())
    }

    @Test fun rejectsInvalidTakeoffValues() {
        val invalid = takeoff.copy(netWallAreaM2 = Double.NaN)
        org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
            AutomaticEstimateGenerator.generate(
                invalid,
                originalCatalog,
                AutomaticEstimateOptions(wallPlaster = true)
            )
        }
    }

    @Test fun customPaintingDecorativeAndFloorFinishesUseTheirOwnCatalogRates() {
        val customCatalog = originalCatalog + listOf(
            CatalogWork("Шпаклёвка стен", 500.0, "м²", "Малярные работы"),
            CatalogWork("Покраска стен", 350.0, "м²", "Малярные работы"),
            CatalogWork("Декоративная штукатурка стен", 1800.0, "м²", "Декоративные покрытия"),
            CatalogWork("Покраска потолка", 400.0, "м²", "Потолки"),
            CatalogWork("Укладка ламината", 900.0, "м²", "Напольные покрытия")
        )
        val result = AutomaticEstimateGenerator.generate(
            takeoff,
            customCatalog,
            AutomaticEstimateOptions(
                wallPutty = true,
                wallPaint = true,
                wallDecorative = true,
                ceilingPaint = true,
                floorLaminate = true
            )
        )
        assertEquals(5, result.size)
        assertEquals(500.0, result.first { it.name.contains("Шпаклёвка стен") }.price, 1e-8)
        assertEquals(350.0, result.first { it.name.contains("Покраска стен") }.price, 1e-8)
        assertEquals(1800.0, result.first { it.name.contains("Декоративная штукатурка") }.price, 1e-8)
        assertEquals(400.0, result.first { it.name.contains("Покраска потолка") }.price, 1e-8)
        assertEquals(900.0, result.first { it.name.contains("Укладка ламината") }.price, 1e-8)
    }

    @Test fun zeroAreaDoesNotCreateEmptyWork() {
        val empty = takeoff.copy(floorAreaM2 = 0.0)
        val result = AutomaticEstimateGenerator.generate(
            empty,
            originalCatalog,
            AutomaticEstimateOptions(floorScreed = true)
        )
        assertTrue(result.isEmpty())
    }
}
