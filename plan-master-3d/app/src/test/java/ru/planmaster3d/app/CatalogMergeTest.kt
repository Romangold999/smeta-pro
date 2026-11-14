package ru.planmaster3d.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogMergeTest {
    @Test fun extensionAddsMissingItemsWithoutOverwritingSavedPrices() {
        val custom = CatalogWork("Шпаклёвка стен", 999.0, "м²", "Мои расценки")
        val merged = mergeCatalog(listOf(custom))
        assertEquals(999.0, merged.single { it.n == "Шпаклёвка стен" }.p, 0.0)
        assertTrue(merged.any { it.n == "Декоративное покрытие замша / нубук" })
        assertEquals(merged.size, merged.map { it.n.lowercase() }.distinct().size)
    }
}
