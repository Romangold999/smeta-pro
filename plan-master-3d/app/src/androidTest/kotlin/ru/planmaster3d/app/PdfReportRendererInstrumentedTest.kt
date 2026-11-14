package ru.planmaster3d.app

import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class PdfReportRendererInstrumentedTest {
    @Test
    fun technicalAndProposalReportsAreValidMultiPagePdfs() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val room = Room(
            id = 1L,
            name = "Гостиная с длинным названием для проверки переноса",
            width = 5.0,
            length = 4.0,
            height = 2.7,
            wall1 = 5.0,
            wall2 = 4.0,
            wall3 = 5.0,
            wall4 = 4.0,
            openingsByWall = mapOf(
                0 to listOf(Opening("door-1", "Дверь", width = 0.9, height = 2.1, offsetFromStart = 1.2))
            )
        )
        val estimate = (1..24).map { index ->
            EstimateLine(
                name = if (index == 1) "Сверхдлинноенаименованиебезпробелов".repeat(8)
                    else "Позиция сметы №$index с подробным описанием работ и материала",
                unit = "м²",
                price = 1250.0 + index,
                qty = 2.5 + index,
                category = if (index <= 18) "Материалы" else "Работы",
                material = index <= 18
            )
        }
        val construction = (1..16).map { index ->
            ConstructionRecord(
                id = "construction-$index",
                kind = "Перегородка ГКЛ",
                roomIndex = 0,
                material = "ГКЛ в два слоя, металлический каркас и минеральная вата",
                lengthM = 3.2,
                heightM = 2.7,
                thicknessMm = 100.0,
                quantity = 8.64
            )
        }
        val engineering = (1..15).map { index ->
            EngineeringPoint(
                id = "point-$index",
                kind = "Электрическая розетка",
                roomIndex = 0,
                x = 0.25 * index,
                y = 0.5,
                note = "Тестовая точка с длинным примечанием для проверки границ таблицы и переноса текста"
            )
        }
        val data = PdfReportData(
            projectName = "Тестовый объект PLAN MASTER 3D",
            rooms = listOf(room),
            estimate = estimate,
            construction = construction,
            engineeringPoints = engineering,
            customerName = "Заказчик с длинным именем для проверки титульного листа",
            executorName = "Исполнитель: строительная компания",
            executorContact = "+7 900 000-00-00, дополнительный контакт",
            objectAddress = "Краснодар, тестовый адрес объекта, корпус и помещение"
        )
        val outputs = listOf(
            PdfReportMode.TECHNICAL to "PLAN-MASTER-3D-technical-test.pdf",
            PdfReportMode.PROPOSAL to "PLAN-MASTER-3D-proposal-test.pdf"
        )
        outputs.forEach { (mode, fileName) ->
            val bytes = PdfReportRenderer.render(data, mode)
            assertTrue("PDF header missing for $fileName", bytes.size > 5 && bytes.copyOfRange(0, 5).decodeToString() == "%PDF-")
            val file = File(context.filesDir, fileName)
            file.writeBytes(bytes)
            assertTrue("PDF output missing or too small: $fileName", file.exists() && file.length() > 5_000)
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
                PdfRenderer(descriptor).use { renderer ->
                    assertTrue("Expected multiple report pages in $fileName, got ${renderer.pageCount}", renderer.pageCount >= 8)
                }
            }
        }
    }
}
