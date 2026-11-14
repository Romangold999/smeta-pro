package ru.planmaster3d.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream

class XlsxReportExporterTest {
    @Test fun createsWorkbookWithSummaryRoomsWorksAndMaterialsSheets() {
        val room = Room(
            1L, "Кухня", 4.0, 3.0, 2.7, 4.0, 3.0, 4.0, 3.0,
            wallThicknesses = listOf(0.1, 0.2, 0.15, 0.1),
            wallHeightOverrides = listOf(2.0, 3.0, 4.0, 5.0),
            openingsByWall = mapOf(0 to listOf(
                Opening("door", "Дверь", 1.0, 1.0, offsetFromStart = 1.0)
            ))
        )
        val estimate = listOf(
            EstimateLine("Штукатурка", "м²", 750.0, 12.0, "Работы"),
            EstimateLine("Грунт", "л", 300.0, 2.0, "Материалы", true)
        )
        val bytes = XlsxReportExporter.export("Тестовый объект", listOf(room), estimate)
        val entries = mutableMapOf<String, String>()
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                entries[entry.name] = zip.readBytes().toString(Charsets.UTF_8)
            }
        }
        assertTrue(entries.containsKey("[Content_Types].xml"))
        assertTrue(entries.containsKey("xl/workbook.xml"))
        assertTrue(entries["xl/workbook.xml"]!!.contains("Помещения"))
        assertTrue(entries["xl/workbook.xml"]!!.contains("Работы"))
        assertTrue(entries["xl/workbook.xml"]!!.contains("Материалы"))
        val summary = entries["xl/worksheets/sheet1.xml"]!!
        val rooms = entries["xl/worksheets/sheet2.xml"]!!
        assertTrue(summary.contains("Тестовый объект"))
        assertTrue(summary.contains("Черновой объём стен, м³"))
        assertTrue(summary.contains("Чистый объём стен, м³"))
        assertTrue(summary.contains("<v>6.5</v>"))
        assertTrue(summary.contains("<v>6.4</v>"))
        assertTrue(rooms.contains("Черновой объём стен, м³"))
        assertTrue(rooms.contains("Чистый объём стен, м³"))
        assertTrue(rooms.contains("<v>6.5</v>"))
        assertTrue(rooms.contains("<v>6.4</v>"))
        assertTrue(entries["xl/worksheets/sheet3.xml"]!!.contains("Штукатурка"))
        assertTrue(entries["xl/worksheets/sheet4.xml"]!!.contains("Грунт"))
    }
}
