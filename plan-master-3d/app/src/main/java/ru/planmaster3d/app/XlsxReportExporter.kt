package ru.planmaster3d.app

import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/** Creates a dependency-free OOXML workbook that can be opened by Excel and LibreOffice. */
object XlsxReportExporter {
    private data class Cell(val value: String, val numeric: Boolean = false)

    fun export(projectName: String, rooms: List<Room>, estimate: List<EstimateLine>): ByteArray {
        require(projectName.isNotBlank()) { "Название проекта не должно быть пустым" }
        require(rooms.isNotEmpty()) { "Для отчёта нужно хотя бы одно помещение" }
        require(estimate.all { listOf(it.qty, it.price, it.total).all(Double::isFinite) }) {
            "Смета содержит некорректные числовые значения"
        }

        // Validate project-wide totals as well as each room: individually finite room
        // values can still overflow when aggregated across a large project.
        val takeoff = GeometryTakeoff.calculate(rooms)
        val total = estimate.sumOf { it.total }
        require(total.isFinite()) { "Итоговая сумма сметы превышает допустимый диапазон" }
        val sheets = listOf(
            "Сводка" to listOf(
                listOf(Cell("PLAN MASTER 3D — $projectName")),
                listOf(Cell("Показатель"), Cell("Значение")),
                listOf(Cell("Помещений"), Cell(rooms.size.toString(), true)),
                listOf(Cell("Площадь пола, м²"), Cell(takeoff.floorAreaM2.toString(), true)),
                listOf(Cell("Чистая площадь стен, м²"), Cell(takeoff.netWallAreaM2.toString(), true)),
                listOf(Cell("Черновой объём стен, м³"), Cell(takeoff.grossWallVolumeM3.toString(), true)),
                listOf(Cell("Чистый объём стен, м³"), Cell(takeoff.netWallVolumeM3.toString(), true)),
                listOf(Cell("Позиций сметы"), Cell(estimate.size.toString(), true)),
                listOf(Cell("Итого, ₽"), Cell(total.toString(), true))
            ),
            "Помещения" to buildList {
                add(listOf(Cell("Помещение"), Cell("Площадь пола, м²"), Cell("Площадь стен, м²"), Cell("Периметр, м"), Cell("Проёмы, м²"), Cell("Откосы, м²"), Cell("Черновой объём стен, м³"), Cell("Чистый объём стен, м³")))
                rooms.forEach { room ->
                    add(listOf(Cell(room.name), Cell(room.floor.toString(), true), Cell(room.walls.toString(), true),
                        Cell(room.perimeter.toString(), true), Cell(room.geometry.openingArea.toString(), true),
                        Cell(room.geometry.revealArea.toString(), true), Cell(room.geometry.grossWallVolumeM3.toString(), true),
                        Cell(room.geometry.netWallVolumeM3.toString(), true)))
                }
            },
            "Работы" to estimate.filterNot { it.material }.toSheetRows(),
            "Материалы" to estimate.filter { it.material }.toSheetRows()
        )

        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            zip.putText("[Content_Types].xml", contentTypes(sheets.size))
            zip.putText("_rels/.rels", rootRelationships())
            zip.putText("xl/workbook.xml", workbook(sheets.map { it.first }))
            zip.putText("xl/_rels/workbook.xml.rels", workbookRelationships(sheets.size))
            sheets.forEachIndexed { index, sheet ->
                zip.putText("xl/worksheets/sheet${index + 1}.xml", worksheet(sheet.second))
            }
        }
        return output.toByteArray()
    }

    private fun List<EstimateLine>.toSheetRows(): List<List<Cell>> {
        val estimateLines = this
        return buildList {
            add(listOf(Cell("Раздел"), Cell("Наименование"), Cell("Ед."), Cell("Количество"), Cell("Цена, ₽"), Cell("Сумма, ₽")))
            estimateLines.forEach { line ->
                add(listOf(Cell(line.category), Cell(line.name), Cell(line.unit), Cell(line.qty.toString(), true),
                    Cell(line.price.toString(), true), Cell(line.total.toString(), true)))
            }
        }
    }

    private fun ZipOutputStream.putText(path: String, value: String) {
        putNextEntry(ZipEntry(path))
        write(value.toByteArray(Charsets.UTF_8))
        closeEntry()
    }

    private fun esc(value: String): String = value.replace("&", "&amp;").replace("<", "&lt;")
        .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;")

    private fun columnName(index: Int): String {
        var n = index + 1
        val result = StringBuilder()
        while (n > 0) {
            val rem = (n - 1) % 26
            result.insert(0, ('A'.code + rem).toChar())
            n = (n - 1) / 26
        }
        return result.toString()
    }

    private fun worksheet(rows: List<List<Cell>>): String = buildString {
        append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?><worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetData>""")
        rows.forEachIndexed { rowIndex, row ->
            append("<row r=\"${rowIndex + 1}\">")
            row.forEachIndexed { colIndex, cell ->
                val ref = columnName(colIndex) + (rowIndex + 1)
                if (cell.numeric) {
                    append("<c r=\"$ref\" t=\"n\"><v>${esc(cell.value)}</v></c>")
                } else {
                    append("<c r=\"$ref\" t=\"inlineStr\"><is><t xml:space=\"preserve\">${esc(cell.value)}</t></is></c>")
                }
            }
            append("</row>")
        }
        append("</sheetData></worksheet>")
    }

    private fun contentTypes(count: Int) = buildString {
        append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/><Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>""")
        for (i in 1..count) append("<Override PartName=\"/xl/worksheets/sheet$i.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>")
        append("</Types>")
    }

    private fun rootRelationships() = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/></Relationships>"""

    private fun workbook(names: List<String>) = buildString {
        append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?><workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"><sheets>""")
        names.forEachIndexed { i, name -> append("<sheet name=\"${esc(name)}\" sheetId=\"${i + 1}\" r:id=\"rId${i + 1}\"/>") }
        append("</sheets></workbook>")
    }

    private fun workbookRelationships(count: Int) = buildString {
        append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">""")
        for (i in 1..count) append("<Relationship Id=\"rId$i\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet$i.xml\"/>")
        append("</Relationships>")
    }
}
