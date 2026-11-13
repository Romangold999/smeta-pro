package ru.ral.masterpro

import android.content.SharedPreferences
import androidx.compose.ui.platform.LocalContext
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONObject

private val bg = Color(0xFF11151B)
private val panel = Color(0xFF202731)
private val gold = Color(0xFFFFC247)
private val muted = Color(0xFF9AA5B3)

data class RalColor(val code: String, val name: String, val hex: Long)
private val colors = ralClassicColors
private val materials = listOf("Краска и эмаль","Декоративная штукатурка","Бетон","Микроцемент")
private val systems = listOf("Водно-дисперсионная","Растворительная","Минеральная / цементная","Универсальная","Своя система")
private val initialPigments = listOf("Белый","Чёрный","Жёлтый","Охра","Оранжевый","Красный","Оксид красный","Синий","Фталоцианиновый синий","Зелёный","Фталоцианиновый зелёный","Фиолетовый","Коричневый","Умбра","Оксид жёлтый")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { RalMasterApp(getSharedPreferences("ral_master", MODE_PRIVATE)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RalMasterApp(prefs: SharedPreferences) {
    val context = LocalContext.current
    var page by remember { mutableIntStateOf(0) }
    var search by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(colors.first { it.code == "7016" }) }
    var material by remember { mutableStateOf(materials.first()) }
    var system by remember { mutableStateOf(systems.first()) }
    var base by remember { mutableStateOf("A") }
    var volume by remember { mutableStateOf("1") }
    var pigments by remember { mutableStateOf(prefs.getString("pigments", initialPigments.joinToString("|"))!!.split("|")) }
    var savedText by remember { mutableStateOf(prefs.getString("formulas", "{}") ?: "{}") }
    var entries by remember { mutableStateOf(mapOf<String, String>()) }
    var showAdd by remember { mutableStateOf(false) }
    var newPigment by remember { mutableStateOf("") }
    var favorites by remember { mutableStateOf(prefs.getString("favorites", "7016")!!.split("|").filter { it.isNotBlank() }.toSet()) }
    var designHue by remember { mutableStateOf("270") }
    var designLightness by remember { mutableStateOf("50") }
    var designChroma by remember { mutableStateOf("40") }
    var dataMessage by remember { mutableStateOf("") }
    val exportFormulas = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            runCatching {
                prefs.edit().putString("formulas", savedText).apply()
                // The ActivityResult callback writes through the activity content resolver below.
                uri
            }.onSuccess { target ->
                context.contentResolver.openOutputStream(target)?.use {
                    it.write(savedText.toByteArray(Charsets.UTF_8))
                }
                dataMessage = "Экспорт рецептур выполнен"
            }.onFailure { dataMessage = "Ошибка экспорта: ${it.message}" }
        }
    }
    val importFormulas = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                val imported = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    ?: throw IllegalStateException("Не удалось открыть файл")
                val checked = JSONObject(imported)
                savedText = checked.toString()
                prefs.edit().putString("formulas", savedText).apply()
                entries = emptyMap()
                dataMessage = "Импортировано рецептур: ${checked.length()}"
            }.onFailure { dataMessage = "Файл не подходит: ${it.message}" }
        }
    }
    val formulas = remember(savedText) { JSONObject(savedText) }
    val key = listOf(material, system, base, selected.code).joinToString("|")
    val formula = formulas.optJSONObject(key)
    val liters = volume.replace(',', '.').toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0

    MaterialTheme(colorScheme = darkColorScheme(primary = gold, background = bg, surface = panel, onPrimary = bg)) {
        Scaffold(
            containerColor = bg,
            topBar = {
                TopAppBar(
                    title = { Column {
                        Text("RAL MASTER PRO", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        Text("COLOR & TINTING WORKSHOP", color = muted, fontSize = 9.sp, letterSpacing = 1.sp)
                    } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = bg, titleContentColor = Color.White)
                )
            },
            bottomBar = {
                NavigationBar(containerColor = panel) {
                    val labels = listOf("Каталог", "Расчёт", "Пигменты", "Рецепты", "Design", "Избранное")
                    val icons = listOf(Icons.Default.Palette, Icons.Default.Calculate, Icons.Default.Science, Icons.Default.MenuBook, Icons.Default.ColorLens, Icons.Default.Favorite)
                    labels.forEachIndexed { i, label ->
                        NavigationBarItem(selected = page == i, onClick = { page = i },
                            icon = { Icon(icons[i], contentDescription = null) }, label = { Text(label) })
                    }
                }
            }
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)) {
                when (page) {
                    0 -> {
                        OutlinedTextField(search, { search = it }, Modifier.fillMaxWidth(), singleLine = true,
                            placeholder = { Text("Поиск: 7016 или антрацит") },
                            leadingIcon = { Icon(Icons.Default.Search, null) }, colors = fieldColors())
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("RAL CLASSIC · " + colors.size + " оттенков", Modifier.weight(1f), color = gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            IconButton(onClick = {
                                favorites = if (favorites.contains(selected.code)) favorites - selected.code else favorites + selected.code
                                prefs.edit().putString("favorites", favorites.joinToString("|")).apply()
                            }) { Icon(if (favorites.contains(selected.code)) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null, tint = gold) }
                        }
                        LazyVerticalGrid(columns = GridCells.Adaptive(100.dp), modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(colors.filter {
                                val q = search.trim().removePrefix("#").uppercase()
                                it.code.contains(q, true) || it.name.contains(search.trim(), true) ||
                                    "%06X".format(it.hex and 0xFFFFFF).contains(q, true)
                            }) { shade ->
                                val textColor = if (shade.hex == 0xFF0A0A0A) Color.White else Color.Black
                                Column(Modifier.height(90.dp).fillMaxWidth().background(Color(shade.hex), RoundedCornerShape(10.dp))
                                    .border(if (shade == selected) 3.dp else 1.dp, if (shade == selected) gold else Color.White.copy(.3f), RoundedCornerShape(10.dp))
                                    .clickable { selected = shade; page = 1 }.padding(8.dp), verticalArrangement = Arrangement.Bottom) {
                                    Text("RAL " + shade.code, color = textColor, fontWeight = FontWeight.Black)
                                    Text(shade.name, color = textColor, fontSize = 9.sp)
                                }
                            }
                        }
                        Text("Цвета на экране приблизительные. Эталон — физический образец RAL.", color = muted, fontSize = 10.sp)
                    }
                    1 -> {
                        Card(colors = CardDefaults.cardColors(containerColor = panel)) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                Text("RAL " + selected.code + " · " + selected.name, color = gold, fontWeight = FontWeight.Bold)
                                Box(Modifier.fillMaxWidth().height(42.dp).background(Color(selected.hex), RoundedCornerShape(8.dp)))
                                ChoiceField("Материал", material, materials) { material = it }
                                ChoiceField("Система", system, systems) { system = it }
                                ChoiceField("База", base, listOf("A", "C", "Тонируемая", "Прозрачная")) { base = it }
                                OutlinedTextField(volume, { volume = it }, Modifier.fillMaxWidth(),
                                    label = { Text("Объём материала, л") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true, colors = fieldColors())
                            }
                        }
                        if (formula == null || formula.length() == 0) {
                            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF3A2B1A))) {
                                Column(Modifier.padding(12.dp)) {
                                    Text("Подтверждённой рецептуры нет", color = gold, fontWeight = FontWeight.Bold)
                                    Text("Для этого сочетания материала, системы, базы и цвета формула не внесена. Приложение не подставляет выдуманные дозировки.",
                                        color = Color.White, fontSize = 12.sp)
                                    TextButton(onClick = { page = 3 }) { Text("Добавить свою рецептуру") }
                                }
                            }
                        } else {
                            Text("СОХРАНЁННАЯ ПОЛЬЗОВАТЕЛЬСКАЯ ФОРМУЛА", color = gold, fontWeight = FontWeight.Bold)
                            pigments.filter { formula.has(it) }.forEach { pigment ->
                                Text(pigment + " · " + "%.2f".format(formula.optDouble(pigment) * liters) + " мл")
                            }
                            Text("Масштабирование линейное; совместимость и технологию нужно проверить.", color = muted, fontSize = 11.sp)
                        }
                    }
                    2 -> {
                        Text("БИБЛИОТЕКА ПИГМЕНТОВ", color = gold, fontWeight = FontWeight.Bold)
                        Text("Типы пигментов. Совместимость зависит от связующего и конкретной системы производителя.",
                            color = muted, fontSize = 11.sp)
                        Column(Modifier.weight(1f)) {
                            pigments.forEach { pigment ->
                                Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(pigment, Modifier.weight(1f))
                                    IconButton(onClick = {
                                        pigments = pigments - pigment
                                        prefs.edit().putString("pigments", pigments.joinToString("|")).apply()
                                    }) { Icon(Icons.Default.Delete, null, tint = muted) }
                                }
                            }
                        }
                        Button(onClick = { showAdd = true }, Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Add, null); Text("Добавить пигмент")
                        }
                    }
                    4 -> {
                        Text("RAL DESIGN SYSTEM PLUS · LAB EXPLORER", color = gold, fontWeight = FontWeight.Bold)
                        Text("Код RAL Design задаёт H° / L* / C*. Введите параметры, чтобы получить математическую экранную визуализацию. Это НЕ официальный образец и не подтверждение наличия такого оттенка в каталоге RAL.", color = muted, fontSize = 11.sp)
                        OutlinedTextField(designHue, { designHue = it.filter(Char::isDigit).take(3) }, Modifier.fillMaxWidth(), label = { Text("Hue H° (000–360)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, colors = fieldColors())
                        OutlinedTextField(designLightness, { designLightness = it.filter(Char::isDigit).take(2) }, Modifier.fillMaxWidth(), label = { Text("Lightness L* (0–100)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, colors = fieldColors())
                        OutlinedTextField(designChroma, { designChroma = it.filter(Char::isDigit).take(2) }, Modifier.fillMaxWidth(), label = { Text("Chroma C* (0–100)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, colors = fieldColors())
                        val h = designHue.toDoubleOrNull()?.coerceIn(0.0, 360.0) ?: 270.0
                        val l = designLightness.toDoubleOrNull()?.coerceIn(0.0, 100.0) ?: 50.0
                        val chroma = designChroma.toDoubleOrNull()?.coerceIn(0.0, 100.0) ?: 40.0
                        val preview = labToColor(l, chroma * kotlin.math.cos(Math.toRadians(h)), chroma * kotlin.math.sin(Math.toRadians(h)))
                        Card(colors = CardDefaults.cardColors(containerColor = panel)) {
                            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(Modifier.fillMaxWidth().height(150.dp).background(preview, RoundedCornerShape(12.dp)))
                                Text("Расчётный код: %03d %02d %02d".format(h.toInt(), l.toInt(), chroma.toInt()), color = gold, fontWeight = FontWeight.Bold)
                                Text("RGB: %02X %02X %02X".format((preview.red * 255).toInt(), (preview.green * 255).toInt(), (preview.blue * 255).toInt()), color = Color.White)
                            }
                        }
                        Text("Для точного соответствия нужен официальный веер RAL Design и спектрофотометрическая проверка.", color = muted, fontSize = 11.sp)
                    }
                    5 -> {
                        Text("ИЗБРАННЫЕ ОТТЕНКИ", color = gold, fontWeight = FontWeight.Bold)
                        LazyVerticalGrid(columns = GridCells.Adaptive(110.dp), modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(colors.filter { favorites.contains(it.code) }) { shade ->
                                Column(Modifier.height(90.dp).fillMaxWidth().background(Color(shade.hex), RoundedCornerShape(10.dp)).clickable { selected = shade; page = 1 }.padding(8.dp), verticalArrangement = Arrangement.Bottom) {
                                    Text("RAL " + shade.code, color = if (shade.hex == 0xFF0A0A0A) Color.White else Color.Black, fontWeight = FontWeight.Bold)
                                    Text(shade.name, color = if (shade.hex == 0xFF0A0A0A) Color.White else Color.Black, fontSize = 9.sp)
                                }
                            }
                        }
                    }
                    else -> {
                        Text("РЕДАКТОР СОБСТВЕННЫХ РЕЦЕПТУР", color = gold, fontWeight = FontWeight.Bold)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { exportFormulas.launch("ral-master-pro-formulas.json") }, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.FileUpload, null); Text("Экспорт")
                            }
                            OutlinedButton(onClick = { importFormulas.launch(arrayOf("application/json", "text/*")) }, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.FileDownload, null); Text("Импорт")
                            }
                        }
                        if (dataMessage.isNotBlank()) Text(dataMessage, color = gold, fontSize = 11.sp)
                        Text("Введите дозировку в мл на 1 литр материала. Вносите только формулы из техкарты или проверенного подбора.",
                            color = muted, fontSize = 11.sp)
                        ChoiceField("Материал", material, materials) { material = it }
                        ChoiceField("Система", system, systems) { system = it }
                        ChoiceField("База", base, listOf("A", "C", "Тонируемая", "Прозрачная")) { base = it }
                        Text("RAL " + selected.code + " · мл/л", color = gold, fontWeight = FontWeight.Bold)
                        Column(Modifier.weight(1f)) {
                            pigments.forEach { pigment ->
                                OutlinedTextField(entries[pigment] ?: formula?.optDouble(pigment)?.takeIf { it > 0 }?.toString().orEmpty(),
                                    { value -> entries = entries.toMutableMap().apply { put(pigment, value) } },
                                    Modifier.fillMaxWidth().padding(vertical = 2.dp), label = { Text(pigment) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true, colors = fieldColors())
                            }
                        }
                        Button(onClick = {
                            val obj = JSONObject()
                            pigments.forEach { pigment ->
                                val amount = entries[pigment]?.replace(',', '.')?.toDoubleOrNull() ?: 0.0
                                if (amount > 0) obj.put(pigment, amount)
                            }
                            formulas.put(key, obj)
                            savedText = formulas.toString()
                            prefs.edit().putString("formulas", savedText).apply()
                            entries = emptyMap()
                            page = 1
                        }, Modifier.fillMaxWidth()) { Icon(Icons.Default.Save, null); Text("Сохранить формулу") }
                    }
                }
            }
        }
        if (showAdd) {
            AlertDialog(onDismissRequest = { showAdd = false }, title = { Text("Новый пигмент / колорант") },
                text = { OutlinedTextField(newPigment, { newPigment = it }, label = { Text("Название или код производителя") }) },
                confirmButton = {
                    TextButton(onClick = {
                        if (newPigment.isNotBlank()) {
                            pigments = pigments + newPigment.trim()
                            prefs.edit().putString("pigments", pigments.joinToString("|")).apply()
                        }
                        newPigment = ""; showAdd = false
                    }) { Text("Добавить") }
                },
                dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Отмена") } })
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = gold, unfocusedBorderColor = muted.copy(alpha = .5f),
    focusedLabelColor = gold, cursorColor = gold
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChoiceField(label: String, value: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(value, {}, Modifier.menuAnchor().fillMaxWidth(), readOnly = true,
            label = { Text(label) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            colors = fieldColors(), singleLine = true)
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = { onSelect(option); expanded = false })
            }
        }
    }
}

private fun labToColor(l: Double, a: Double, b: Double): Color {
    val fy = (l + 16.0) / 116.0
    val fx = fy + a / 500.0
    val fz = fy - b / 200.0
    fun inv(t: Double): Double = if (t * t * t > 0.008856) t * t * t else (t - 16.0 / 116.0) / 7.787
    val x = inv(fx) * 0.95047
    val y = inv(fy)
    val z = inv(fz) * 1.08883
    var rr = x * 3.2406 + y * -1.5372 + z * -0.4986
    var gg = x * -0.9689 + y * 1.8758 + z * 0.0415
    var bb = x * 0.0557 + y * -0.2040 + z * 1.0570
    fun gamma(v: Double): Double {
        val clamped = v.coerceIn(0.0, 1.0)
        return if (clamped <= 0.0031308) 12.92 * clamped else 1.055 * Math.pow(clamped, 1.0 / 2.4) - 0.055
    }
    rr = gamma(rr); gg = gamma(gg); bb = gamma(bb)
    return Color(rr.toFloat(), gg.toFloat(), bb.toFloat(), 1f)
}
