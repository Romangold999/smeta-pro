package ru.planmaster3d.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import java.text.DecimalFormat
import kotlin.math.*

data class CatalogWork(val n:String,val p:Double,val u:String,val c:String,val from:Boolean=false)
val originalCatalog=listOf(
CatalogWork("Укладка керамогранита 60×60, 60×20, 120×20",2000.0,"м²","Плитка"),CatalogWork("Укладка КГ 120×60, 80×80",2300.0,"м²","Плитка"),CatalogWork("Укладка керамогранита 160×80",3000.0,"м²","Плитка"),CatalogWork("Мозаика",3000.0,"м²","Плитка",true),
CatalogWork("Штукатурка ЦПС до 3 см",750.0,"м²","Подготовка"),CatalogWork("Контактный слой плиточным клеем",200.0,"м²","Подготовка"),CatalogWork("Грунтование",100.0,"м²","Подготовка"),CatalogWork("Поддон в строительном исполнении",40000.0,"шт","Сантехника",true),CatalogWork("Гидроизоляция (2 слоя)",350.0,"м²","Подготовка"),CatalogWork("Гидроизоляционная лента",200.0,"м.п.","Подготовка"),CatalogWork("Стяжка до 3 см",750.0,"м²","Подготовка"),
CatalogWork("Короб инсталляции ГКЛ",4500.0,"шт","ГКЛ"),CatalogWork("ГКЛ короб до 30 см",1300.0,"м.п.","ГКЛ"),CatalogWork("ГКЛ экран под ванну",2000.0,"шт","ГКЛ",true),CatalogWork("Облицовка керамогранитом погонаж до 40 см",1500.0,"м.п.","Плитка"),CatalogWork("Отверстия в керамограните до 70 мм",500.0,"шт","Плитка"),CatalogWork("Отверстия в керамограните более 70 мм",650.0,"шт","Плитка"),CatalogWork("Запил керамогранита 45°",1500.0,"м.п.","Плитка"),CatalogWork("Г-образная подрезка",800.0,"м.п.","Плитка"),CatalogWork("Заводской рез",500.0,"м.п.","Плитка"),
CatalogWork("Установка потайного люка с облицовкой (до 60×60 см)",4000.0,"шт","Монтаж"),CatalogWork("Установка ревизионного люка на магнитах",3000.0,"шт","Монтаж"),CatalogWork("Установка светильника",800.0,"шт","Электрика"),CatalogWork("Установка вентилятора",1000.0,"шт","Электрика"),
CatalogWork("Установка, сборка, подключение ванны (акрил, металл)",4500.0,"шт","Сантехника"),CatalogWork("Установка, сборка чугунной ванны",6000.0,"шт","Сантехника"),CatalogWork("Сборка, установка стеклянной ширмы душевой кабины",6000.0,"шт","Сантехника",true),CatalogWork("Установка унитаза",2500.0,"шт","Сантехника"),CatalogWork("Установка умывальника",1700.0,"шт","Сантехника"),CatalogWork("Установка бойлера",2500.0,"шт","Сантехника"),CatalogWork("Установка полотенцесушителя",3000.0,"шт","Сантехника"),CatalogWork("Установка, подключение инсталляции",4500.0,"шт","Сантехника"),CatalogWork("Установка унитаза инсталляции",2500.0,"шт","Сантехника"),CatalogWork("Установка кнопки инсталляции",500.0,"шт","Сантехника"),
CatalogWork("Установка зеркала",1300.0,"шт","Монтаж"),CatalogWork("Установка зеркала с подсветкой",1500.0,"шт","Электрика"),CatalogWork("Установка аксессуаров",800.0,"шт","Монтаж"),CatalogWork("Установка смесителя",1800.0,"шт","Сантехника"),CatalogWork("Установка крана умывальника",800.0,"шт","Сантехника"),CatalogWork("Установка тумбы умывальника",1700.0,"шт","Сантехника"),CatalogWork("Установка встраиваемого смесителя",3500.0,"шт","Сантехника"),CatalogWork("Установка гигиенического душа",1400.0,"шт","Сантехника"),CatalogWork("Установка встраиваемого гигиенического душа",3500.0,"шт","Сантехника"),CatalogWork("Установка смесителя + штанга",3000.0,"шт","Сантехника"),CatalogWork("Установка системы тропический душ",3500.0,"шт","Сантехника"),
CatalogWork("Распайка ПП трубы (1 точка)",2000.0,"точка","Сантехника"),CatalogWork("Канализация",2000.0,"точка","Сантехника"),CatalogWork("Установка водомера",800.0,"шт","Сантехника"),CatalogWork("Штроба в бетоне 20 мм",1000.0,"м.п.","Электрика"),CatalogWork("Штроба в бетоне 50 мм",1500.0,"м.п.","Электрика"),CatalogWork("Штроба в газоблоке 20 мм",700.0,"м.п.","Электрика"),CatalogWork("Заделка штробы",200.0,"м.п.","Подготовка"),CatalogWork("Отверстие в бетоне для подрозетника",400.0,"шт","Электрика"),CatalogWork("Монтаж подрозетника",100.0,"шт","Электрика"),CatalogWork("Установка розетки",250.0,"шт","Электрика"),CatalogWork("Демонтаж старой штукатурки",450.0,"м²","Демонтаж"),CatalogWork("Демонтаж плитки",1000.0,"м²","Демонтаж"),CatalogWork("Изготовление раковины из керамогранита (до 80 см)",25000.0,"шт","Сантехника"),CatalogWork("Монтаж раковины",7000.0,"шт","Сантехника",true)
)
val additionalCatalog=listOf(
    CatalogWork("Шпаклёвка стен",550.0,"м²","Малярные работы",true),
    CatalogWork("Шпаклёвка потолка",650.0,"м²","Малярные работы",true),
    CatalogWork("Покраска стен",450.0,"м²","Малярные работы",true),
    CatalogWork("Покраска потолка",550.0,"м²","Малярные работы",true),
    CatalogWork("Шлифовка шпаклёвки",180.0,"м²","Малярные работы",true),
    CatalogWork("Армирование стен стеклохолстом",250.0,"м²","Малярные работы",true),
    CatalogWork("Оклейка стен стеклохолстом",300.0,"м²","Малярные работы",true),
    CatalogWork("Декоративная штукатурка стен",1800.0,"м²","Декоративная отделка",true),
    CatalogWork("Венецианская штукатурка",3500.0,"м²","Декоративная отделка",true),
    CatalogWork("Декоративное покрытие замша / нубук",2200.0,"м²","Декоративная отделка",true),
    CatalogWork("Декоративное покрытие микроцемент",3000.0,"м²","Декоративная отделка",true),
    CatalogWork("Декоративное покрытие травертин",2500.0,"м²","Декоративная отделка",true),
    CatalogWork("Декоративное покрытие под бетон",2000.0,"м²","Декоративная отделка",true),
    CatalogWork("Нанесение защитного лака",350.0,"м²","Декоративная отделка",true),
    CatalogWork("Поклейка флизелиновых обоев",500.0,"м²","Обои",true),
    CatalogWork("Поклейка виниловых обоев",600.0,"м²","Обои",true),
    CatalogWork("Поклейка фотообоев",900.0,"м²","Обои",true),
    CatalogWork("Подготовка стен под обои",500.0,"м²","Обои",true),
    CatalogWork("Демонтаж обоев",180.0,"м²","Демонтаж",true),
    CatalogWork("Укладка ламината",650.0,"м²","Полы",true),
    CatalogWork("Укладка инженерной доски",1500.0,"м²","Полы",true),
    CatalogWork("Укладка паркетной доски",1200.0,"м²","Полы",true),
    CatalogWork("Монтаж подложки",100.0,"м²","Полы",true),
    CatalogWork("Монтаж плинтуса МДФ",450.0,"м.п.","Полы",true),
    CatalogWork("Монтаж напольного плинтуса ПВХ",250.0,"м.п.","Полы",true),
    CatalogWork("Наливной пол",550.0,"м²","Полы",true),
    CatalogWork("Выравнивание пола самонивелирующейся смесью",450.0,"м²","Полы",true),
    CatalogWork("Монтаж потолка ГКЛ одноуровневого",1800.0,"м²","Потолки",true),
    CatalogWork("Монтаж потолка ГКЛ многоуровневого",2600.0,"м²","Потолки",true),
    CatalogWork("Монтаж натяжного потолка",900.0,"м²","Потолки",true),
    CatalogWork("Монтаж перегородки ГКЛ",1800.0,"м²","Перегородки",true),
    CatalogWork("Обшивка стен ГКЛ на каркас",1200.0,"м²","ГКЛ",true),
    CatalogWork("Кладка перегородки из газобетона",1800.0,"м²","Перегородки",true),
    CatalogWork("Кладка перегородки из ПГП",1500.0,"м²","Перегородки",true),
    CatalogWork("Кладка кирпичной перегородки",2500.0,"м²","Перегородки",true),
    CatalogWork("Монтаж тепло- и шумоизоляции",350.0,"м²","Перегородки",true),
    CatalogWork("Монтаж дверного блока",4500.0,"шт","Двери",true),
    CatalogWork("Установка межкомнатной двери",5000.0,"шт","Двери",true),
    CatalogWork("Монтаж электрокабеля открыто",120.0,"м.п.","Электрика",true),
    CatalogWork("Прокладка кабеля в штробе",180.0,"м.п.","Электрика",true),
    CatalogWork("Сборка электрощита",5000.0,"шт","Электрика",true),
    CatalogWork("Установка автомата в щит",450.0,"шт","Электрика",true),
    CatalogWork("Монтаж электрического тёплого пола",900.0,"м²","Электрика",true),
    CatalogWork("Монтаж водорозетки",1200.0,"шт","Сантехника",true),
    CatalogWork("Монтаж коллектора водоснабжения",3500.0,"шт","Сантехника",true),
    CatalogWork("Монтаж фильтра грубой очистки",700.0,"шт","Сантехника",true),
    CatalogWork("Монтаж канализационной трубы",450.0,"м.п.","Сантехника",true),
    CatalogWork("Опрессовка системы водоснабжения",2500.0,"система","Сантехника",true)
)
fun mergeCatalog(saved:List<CatalogWork>):List<CatalogWork> {
    val merged=linkedMapOf<String,CatalogWork>()
    (additionalCatalog+originalCatalog).forEach { merged[it.n.lowercase()]=it }
    saved.forEach { merged[it.n.lowercase()]=it }
    return merged.values.toList()
}

data class Room(val id:Long,val name:String,val width:Double,val length:Double,val height:Double,val wall1:Double,val wall2:Double,val wall3:Double,val wall4:Double,val vertices:List<Vec2> = listOf(Vec2(0.0,0.0),Vec2(width,0.0),Vec2(width,length),Vec2(0.0,length)),val wallHeights:List<Double> = emptyList(),val wallThicknesses:List<Double> = emptyList(),val openingsByWall:Map<Int,List<Opening>> = emptyMap(),val wallHeightOverrides:List<Double> = emptyList(),val wallMeasurements:List<WallVectorMeasurement> = emptyList(),val wallHeightEndOverrides:List<Double> = emptyList()) {
    val floor get() = GeometryEngine.area(vertices)
    val perimeter get() = GeometryEngine.perimeter(vertices)
    val geometry get() = GeometryEngine.metrics(RoomGeometry(name,vertices,wallHeights,wallThicknesses,openingsByWall,wallHeightOverrides,wallHeightEndOverrides=wallHeightEndOverrides),height)
    val walls get() = geometry.netWallArea
}
data class EstimateLine(val name:String,val unit:String,val price:Double,val qty:Double,val category:String,val material:Boolean=false) {
    val total get()=price*qty
}
private val navy=Color(0xFF101827)
private val panel=Color(0xFF1B293B)
private val accent=Color(0xFF48C7D9)
private val df=DecimalFormat("#,##0.##")
class MainActivity:ComponentActivity(){
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState); setContent { MaterialTheme(colorScheme=darkColorScheme(primary=accent,background=navy,surface=panel)) { PlanApp(this) } }}
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun PlanApp(ctx:Context) {
 val prefs=remember{ctx.getSharedPreferences("plan_master_3d",0)}
 var showPdfOptions by remember { mutableStateOf(false) }
 var pendingPdfBytes by remember { mutableStateOf<ByteArray?>(null) }
 var pendingXlsxBytes by remember { mutableStateOf<ByteArray?>(null) }
 var pdfCustomer by remember { mutableStateOf(prefs.getString("pdf_customer","") ?: "") }
 var pdfExecutor by remember { mutableStateOf(prefs.getString("pdf_executor","") ?: "") }
 var pdfContact by remember { mutableStateOf(prefs.getString("pdf_contact","") ?: "") }
 var pdfAddress by remember { mutableStateOf(prefs.getString("pdf_address","") ?: "") }
 val pdfSaveLauncher=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
  if(uri!=null) runCatching { ctx.contentResolver.openOutputStream(uri)?.use { it.write(pendingPdfBytes ?: byteArrayOf()) } }
   .onSuccess { android.widget.Toast.makeText(ctx,"PDF сохранён",android.widget.Toast.LENGTH_SHORT).show() }
   .onFailure { android.widget.Toast.makeText(ctx,"Ошибка сохранения PDF: " + (it.message ?: "ошибка"),android.widget.Toast.LENGTH_LONG).show() }
  pendingPdfBytes=null
 }
 val xlsxSaveLauncher=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) { uri ->
  if(uri!=null) runCatching { ctx.contentResolver.openOutputStream(uri)?.use { it.write(pendingXlsxBytes ?: byteArrayOf()) } }
   .onSuccess { android.widget.Toast.makeText(ctx,"XLSX сохранён",android.widget.Toast.LENGTH_SHORT).show() }
   .onFailure { android.widget.Toast.makeText(ctx,"Ошибка сохранения XLSX: " + (it.message ?: "ошибка"),android.widget.Toast.LENGTH_LONG).show() }
  pendingXlsxBytes=null
 }
 var tab by remember { mutableStateOf("Объект") }
 val projectStore=remember{ProjectStore(ctx)}
 var activeProjectId by remember {
  mutableStateOf(prefs.getString("active_project_id",null) ?: "project_${System.currentTimeMillis()}".also { prefs.edit().putString("active_project_id",it).apply() })
 }
 val initialSaved=remember{projectStore.loadProject(activeProjectId)}
 var project by remember { mutableStateOf(initialSaved?.name ?: prefs.getString("project","Новый объект") ?: "Новый объект") }
 fun toRoomModels(saved:SavedProject?):List<Room> = saved?.rooms?.mapIndexed { i,g ->
  val p=g.vertices
  val w=if(p.size>=2) (p.maxOf{it.x}-p.minOf{it.x}).coerceAtLeast(0.01) else 4.0
  val l=if(p.size>=3) (p.maxOf{it.y}-p.minOf{it.y}).coerceAtLeast(0.01) else 5.0
  val h=g.wallHeights.firstOrNull()?:2.7
  Room(i+1L,g.name,w,l,h,w,l,w,l,p,g.wallHeights,g.wallThicknesses,g.openingsByWall,g.wallHeightOverrides,g.measurements,g.wallHeightEndOverrides)
 }?.takeIf { it.isNotEmpty() } ?: listOf(Room(1,"Комната 1",4.0,5.0,2.7,4.0,5.0,4.0,5.0))
 var rooms by remember { mutableStateOf(toRoomModels(initialSaved)) }
 var selected by remember { mutableIntStateOf(0) }
 var lines by remember { mutableStateOf(initialSaved?.estimate ?: emptyList()) }
 var constructionRecords by remember { mutableStateOf(initialSaved?.construction ?: emptyList()) }
 var engineeringPoints by remember { mutableStateOf(initialSaved?.engineeringPoints ?: emptyList()) }
 var engineeringKind by remember { mutableStateOf("Розетка") }
 var engineeringX by remember { mutableStateOf("1") }
 var engineeringY by remember { mutableStateOf("1") }
 var engineeringNote by remember { mutableStateOf("") }
 var engineeringRoomIndex by remember { mutableIntStateOf(0) }
 var projectIds by remember { mutableStateOf(projectStore.listProjectIds()) }
 var showProjects by remember { mutableStateOf(false) }
 var saveMessage by remember { mutableStateOf("Автосохранение включено • изменения сохраняются локально") }
 var priceSearch by remember { mutableStateOf("") }
 val initialPriceBook=remember{projectStore.loadPriceBook()}
 var priceBook by remember { mutableStateOf(initialPriceBook ?: PriceBook("",emptyList())) }
 val initialCatalog=initialPriceBook?.versions?.firstOrNull{it.id==initialPriceBook.activeVersionId}?.items
 var catalogItems by remember { mutableStateOf(mergeCatalog(initialCatalog ?: emptyList())) }
 var catalogPriceDrafts by remember { mutableStateOf<Map<String,String>>(emptyMap()) }
 var priceBookName by remember { mutableStateOf(initialPriceBook?.versions?.firstOrNull{it.id==initialPriceBook.activeVersionId}?.name ?: "Мой прайс-лист") }
 var newCatalogWorkName by remember { mutableStateOf("") }
 var newCatalogWorkCategory by remember { mutableStateOf("Малярные работы") }
 var newCatalogWorkUnit by remember { mutableStateOf("м²") }
 var newCatalogWorkPrice by remember { mutableStateOf("0") }
 var includeWallPlaster by remember { mutableStateOf(true) }
 var includeWallPrimer by remember { mutableStateOf(true) }
 var includeFloorScreed by remember { mutableStateOf(false) }
 var includeCeilingPrimer by remember { mutableStateOf(false) }
 var includeWallWaterproofing by remember { mutableStateOf(false) }
 var includeFloorWaterproofing by remember { mutableStateOf(false) }
 var includeWallTile by remember { mutableStateOf(false) }
 var includeFloorTile by remember { mutableStateOf(false) }
 var includeWallPutty by remember { mutableStateOf(false) }
 var includeWallPaint by remember { mutableStateOf(false) }
 var includeWallDecorative by remember { mutableStateOf(false) }
 var includeCeilingPutty by remember { mutableStateOf(false) }
 var includeCeilingPaint by remember { mutableStateOf(false) }
 var includeStretchCeiling by remember { mutableStateOf(false) }
 var includeFloorLaminate by remember { mutableStateOf(false) }
 var materialMode by remember { mutableStateOf("Штукатурка") }
 var materialArea by remember { mutableStateOf("45") }
 var materialLayer by remember { mutableStateOf("10") }
 var materialCoverage by remember { mutableStateOf("1.2") }
 var materialPack by remember { mutableStateOf("25") }
 var materialReserve by remember { mutableStateOf("10") }
 var layoutType by remember { mutableStateOf("Плитка") }
 var layoutA by remember { mutableStateOf("600") }
 var layoutB by remember { mutableStateOf("600") }
 var layoutJoint by remember { mutableStateOf("2") }
 var layoutRoomA by remember { mutableStateOf("4000") }
 var layoutRoomB by remember { mutableStateOf("5000") }
 var waste by remember { mutableStateOf(10f) }
 var showAddRoom by remember { mutableStateOf(false) }
 var roomName by remember { mutableStateOf("Комната") }
 var roomW by remember { mutableStateOf("4") }
 var roomL by remember { mutableStateOf("5") }
 var roomH by remember { mutableStateOf("2.7") }
 var demolitionSurface by remember { mutableStateOf("Стена") }
 var demolitionRegion by remember { mutableStateOf<List<Vec2>>(emptyList()) }
 var demolitionRegionMessage by remember { mutableStateOf("") }
 var demolitionWall by remember { mutableStateOf("Стена 1") }
 var selectedDemolitionWallIndex by remember { mutableIntStateOf(0) }
 var demolitionMaterial by remember { mutableStateOf("Керамическая плитка") }
 var demolitionLength by remember { mutableStateOf("4") }
 var demolitionOffset by remember { mutableStateOf("0") }
 var demolitionHeight by remember { mutableStateOf("2.7") }
 var demolitionThickness by remember { mutableStateOf("10") }
 var demolitionDensity by remember { mutableStateOf("2300") }
 var demolitionBulking by remember { mutableStateOf("1.35") }
 var bagLitres by remember { mutableStateOf("60") }
 var bagMaxKg by remember { mutableStateOf("25") }
 var bagFill by remember { mutableStateOf("0.85") }
 var demolitionRate by remember { mutableStateOf("1000") }
 var partitionKind by remember { mutableStateOf("Газобетон") }
 var partitionLength by remember { mutableStateOf("3") }
 var partitionStart by remember { mutableStateOf<Vec2?>(null) }
 var partitionEnd by remember { mutableStateOf<Vec2?>(null) }
 var partitionHeight by remember { mutableStateOf("2.7") }
 var partitionThickness by remember { mutableStateOf("100") }
 var partitionOpenings by remember { mutableStateOf("0") }
 var partitionReserve by remember { mutableStateOf("7") }
 var blockLength by remember { mutableStateOf("600") }
 var blockHeight by remember { mutableStateOf("250") }
 var blockDensity by remember { mutableStateOf("600") }
 var boardLayers by remember { mutableStateOf("1") }
 var boardSides by remember { mutableStateOf("2") }
 var studSpacing by remember { mutableStateOf("600") }
 var insulation by remember { mutableStateOf(false) }
 var openingWallIndex by remember { mutableIntStateOf(0) }
 var openingName by remember { mutableStateOf("Дверь") }
 var openingKind by remember { mutableStateOf("Дверь") }
 var openingOffset by remember { mutableStateOf("0.5") }
 var openingWidth by remember { mutableStateOf("0.9") }
 var openingHeight by remember { mutableStateOf("2.1") }
 var openingSill by remember { mutableStateOf("0") }
 var openingDepth by remember { mutableStateOf("0.15") }
 var openingMessage by remember { mutableStateOf("") }
 var vertexDrafts by remember { mutableStateOf<Map<Long,String>>(emptyMap()) }
 var wallDimensionDrafts by remember { mutableStateOf<Map<String,String>>(emptyMap()) }
 var geometryMessage by remember { mutableStateOf("") }
 var selectedVertexIndex by remember { mutableIntStateOf(0) }
 var partitionRate by remember { mutableStateOf("0") }
 var demolitionBagPrice by remember { mutableStateOf("0") }
 var view3dAngle by remember { mutableFloatStateOf(35f) }
 var view3dZoom by remember { mutableFloatStateOf(1f) }
 var view3dPanX by remember { mutableFloatStateOf(0f) }
 var view3dPanY by remember { mutableFloatStateOf(0f) }
 var hidden3dWalls by remember { mutableStateOf(setOf<Int>()) }
 var demolitionHaulPrice by remember { mutableStateOf("0") }
 var blockUnitPrice by remember { mutableStateOf("0") }
 var adhesiveKgPrice by remember { mutableStateOf("0") }
 var boardSheetPrice by remember { mutableStateOf("0") }
 var profileMeterPrice by remember { mutableStateOf("0") }
 var insulationM3Price by remember { mutableStateOf("0") }
 val current=rooms.getOrNull(selected) ?: rooms.first()
  val demolitionEdgeLength=current.vertices.getOrNull(selectedDemolitionWallIndex)?.let { a -> a.distanceTo(current.vertices[(selectedDemolitionWallIndex+1)%current.vertices.size]) } ?: 0.0
  val demolitionStartOffset=(demolitionOffset.toDoubleOrNull()?:0.0).coerceIn(0.0,demolitionEdgeLength)
  val demolitionEffectiveLength=(demolitionLength.toDoubleOrNull()?:0.0).coerceAtLeast(0.0).coerceAtMost((demolitionEdgeLength-demolitionStartOffset).coerceAtLeast(0.0))
  val partitionMeasuredLength=if(partitionStart!=null&&partitionEnd!=null) partitionStart!!.distanceTo(partitionEnd!!) else partitionLength.toDoubleOrNull()?:0.0
 val totalFloor=rooms.sumOf{it.floor}
 val totalWalls=rooms.sumOf{it.walls}
 val totalPerimeter=rooms.sumOf{it.perimeter}
 fun save(){
  val geometry=rooms.map { r ->
   RoomGeometry(r.name,r.vertices,if(r.wallHeights.size==r.vertices.size) r.wallHeights else List(r.vertices.size){r.height},if(r.wallThicknesses.size==r.vertices.size) r.wallThicknesses else List(r.vertices.size){0.0},r.openingsByWall.filterKeys{it in r.vertices.indices},if(r.wallHeightOverrides.size==r.vertices.size) r.wallHeightOverrides else emptyList(),r.wallMeasurements,if(r.wallHeightEndOverrides.size==r.vertices.size) r.wallHeightEndOverrides else emptyList())
  }
  projectStore.saveProject(SavedProject(activeProjectId,project,geometry,lines,construction=constructionRecords,engineeringPoints=engineeringPoints))
  prefs.edit().putString("project",project).putString("active_project_id",activeProjectId).apply()
  projectIds=projectStore.listProjectIds()
  saveMessage="Сохранено: ${java.text.SimpleDateFormat("HH:mm:ss",java.util.Locale.getDefault()).format(java.util.Date())}"
 }
 fun openProject(id:String){
  val saved=projectStore.loadProject(id) ?: return
  activeProjectId=id;project=saved.name;rooms=toRoomModels(saved);lines=saved.estimate;constructionRecords=saved.construction;engineeringPoints=saved.engineeringPoints;selected=0
  prefs.edit().putString("active_project_id",id).putString("project",project).apply()
  showProjects=false;saveMessage="Открыт проект «${saved.name}»"
 }
 // Автосохранение проекта: изменения геометрии, сметы и конструктивных записей
 // фиксируются локально при каждом изменении состояния, а не только по кнопке.
 LaunchedEffect(activeProjectId, project, rooms, lines, constructionRecords, engineeringPoints) {
  save()
 }
 fun createProject(){
  activeProjectId="project_${System.currentTimeMillis()}"
  project="Новый объект";rooms=listOf(Room(1,"Комната 1",4.0,5.0,2.7,4.0,5.0,4.0,5.0));lines=emptyList();constructionRecords=emptyList();engineeringPoints=emptyList();selected=0
  prefs.edit().putString("active_project_id",activeProjectId).putString("project",project).apply()
  save();showProjects=false
 }
 Scaffold(containerColor=navy, topBar={ TopAppBar(title={Column { Text("PLAN MASTER 3D",fontWeight=FontWeight.Bold); Text(project,fontSize=12.sp,color=Color.LightGray) }},actions={IconButton(onClick={save()}){Icon(Icons.Default.Save,"Сохранить")}},colors=TopAppBarDefaults.topAppBarColors(containerColor=navy)) },
 bottomBar={NavigationBar(containerColor=panel){ listOf("Объект","Обмеры","3D","Смета","Материалы","Раскладка").forEach { label -> NavigationBarItem(selected=tab==label,onClick={tab=label},icon={Icon(when(label){"Объект"->Icons.Default.Home;"Обмеры"->Icons.Default.SquareFoot;"3D"->Icons.Default.Layers;"Смета"->Icons.Default.Receipt;"Материалы"->Icons.Default.Inventory2;else->Icons.Default.GridOn},null)},label={Text(label,fontSize=10.sp)}) } } }) { pad ->
  Column(Modifier.fillMaxSize().padding(pad).padding(horizontal=14.dp)) {
   when(tab){
    "Объект" -> LazyColumn(verticalArrangement=Arrangement.spacedBy(12.dp),contentPadding=PaddingValues(vertical=12.dp)){
      item { CardBlock { Text("Панель объекта",fontSize=20.sp,fontWeight=FontWeight.Bold); OutlinedTextField(project,{project=it},label={Text("Название объекта")},modifier=Modifier.fillMaxWidth()); Text("Помещений: ${rooms.size}  •  Площадь пола: ${df.format(totalFloor)} м²");
        Text("Конструктивных элементов в проекте: ${constructionRecords.size}",color=accent,fontSize=12.sp); Text("Периметр: ${df.format(totalPerimeter)} м  •  Стены: ${df.format(totalWalls)} м²"); Text(saveMessage,fontSize=11.sp,color=Color.LightGray); Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Button(onClick={save()},modifier=Modifier.weight(1f)){Text("Сохранить")};OutlinedButton(onClick={showProjects=true},modifier=Modifier.weight(1f)){Text("Мои проекты")}}; OutlinedButton(onClick={createProject()},modifier=Modifier.fillMaxWidth()){Text("Создать новый проект")} } }
      item { Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){ Metric("Пол",totalFloor,"м²",Modifier.weight(1f)); Metric("Стены",totalWalls,"м²",Modifier.weight(1f)); Metric("Периметр",totalPerimeter,"м",Modifier.weight(1f)) } }
      item { CardBlock { Text("Помещения",fontWeight=FontWeight.Bold); rooms.forEachIndexed { i,r -> Row(Modifier.fillMaxWidth().clickable{selected=i;tab="Обмеры"}.padding(vertical=8.dp),horizontalArrangement=Arrangement.SpaceBetween){ Text("${r.name}  •  ${df.format(r.floor)} м²"); Text("Изменить ›",color=accent) } } ; Button(onClick={showAddRoom=true},modifier=Modifier.fillMaxWidth()){Text("Добавить помещение")} } }
      item { CardBlock { Text("Быстрые ведомости",fontWeight=FontWeight.Bold); Text("Площадь пола: ${df.format(totalFloor)} м²"); Text("Площадь стен с вычетом проёмов: ${df.format(totalWalls)} м²"); Text("Периметр: ${df.format(totalPerimeter)} м") } }
      item { CardBlock { Text("Строительные операции",fontWeight=FontWeight.Bold); Button(onClick={tab="Демонтаж"},modifier=Modifier.fillMaxWidth()){Text("Демонтаж и строительный мусор")}; OutlinedButton(onClick={tab="Перегородки"},modifier=Modifier.fillMaxWidth()){Text("Новые перегородки и материалы")}; OutlinedButton(onClick={tab="Инженерия"},modifier=Modifier.fillMaxWidth()){Text("Электрика и сантехника — точки на плане")} } }
    }
    "Обмеры" -> LazyColumn(verticalArrangement=Arrangement.spacedBy(12.dp),contentPadding=PaddingValues(vertical=12.dp)){
      item { CardBlock { Text("Геометрия помещения",fontSize=19.sp,fontWeight=FontWeight.Bold); Text("Базовый режим прямоугольного помещения. Длины каждой стены редактируются отдельно; исходные размеры не подгоняются автоматически.") ; Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){ rooms.forEachIndexed{i,r->FilterChip(selected=selected==i,onClick={selected=i},label={Text(r.name)})} } } }
      item { CardBlock {
        Text(current.name,fontWeight=FontWeight.Bold)
        Text("Редактирование контура перетаскиванием вершин",fontWeight=FontWeight.SemiBold)
        Text("Потяни круглую точку на плане. После отпускания приложение проверит контур и сохранит корректную геометрию.",fontSize=11.sp,color=Color.LightGray)
        EditableRoomPlan(
          room=current,
          selectedVertex=selectedVertexIndex.coerceIn(0,current.vertices.lastIndex),
          onSelectedVertex={selectedVertexIndex=it},
          onCommit={candidate ->
            val candidateRoom=RoomGeometry(current.name,candidate,current.wallHeights,current.wallThicknesses,current.openingsByWall,current.wallHeightOverrides,wallHeightEndOverrides=current.wallHeightEndOverrides)
            val errors=GeometryEngine.validateRoom(candidateRoom,current.height)
            if(errors.isNotEmpty()) {
              geometryMessage="Изменение отклонено: "+errors.joinToString("; ")
            } else {
              val updated=current.copy(
                width=(candidate.maxOf{it.x}-candidate.minOf{it.x}).coerceAtLeast(0.01),
                length=(candidate.maxOf{it.y}-candidate.minOf{it.y}).coerceAtLeast(0.01),
                vertices=candidate,
                wallMeasurements=emptyList()
              )
              rooms=rooms.toMutableList().also{it[selected]=updated}
              vertexDrafts=vertexDrafts-current.id
              geometryMessage="Вершина перемещена. Геометрия и расчёты обновлены; исходные векторные обмеры очищены, так как контур изменён вручную."
            }
          },
          modifier=Modifier.fillMaxWidth().height(250.dp)
        )
        Text("Выбрана вершина ${selectedVertexIndex.coerceIn(0,current.vertices.lastIndex)+1}: X ${df.format(current.vertices[selectedVertexIndex.coerceIn(0,current.vertices.lastIndex)].x)} м • Y ${df.format(current.vertices[selectedVertexIndex.coerceIn(0,current.vertices.lastIndex)].y)} м",fontSize=12.sp,color=accent)
         val activeVertexIndex=selectedVertexIndex.coerceIn(0,current.vertices.lastIndex)
         val activeVertex=current.vertices[activeVertexIndex]
         var vertexXDraft by remember(current.id,activeVertexIndex) { mutableStateOf(String.format(java.util.Locale.US,"%.3f",activeVertex.x)) }
         var vertexYDraft by remember(current.id,activeVertexIndex) { mutableStateOf(String.format(java.util.Locale.US,"%.3f",activeVertex.y)) }
         Row(horizontalArrangement=Arrangement.spacedBy(8.dp),verticalAlignment=Alignment.CenterVertically) {
           OutlinedTextField(value=vertexXDraft,onValueChange={vertexXDraft=it},modifier=Modifier.weight(1f),label={Text("X, м")},singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal))
           OutlinedTextField(value=vertexYDraft,onValueChange={vertexYDraft=it},modifier=Modifier.weight(1f),label={Text("Y, м")},singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal))
         }
         Button(onClick={
           val x=vertexXDraft.trim().replace(',','.').toDoubleOrNull()
           val y=vertexYDraft.trim().replace(',','.').toDoubleOrNull()
           if(x==null||y==null||!x.isFinite()||!y.isFinite()) {
             geometryMessage="Координаты должны быть корректными конечными числами."
           } else {
             val candidate=current.vertices.toMutableList().also{it[activeVertexIndex]=Vec2(x,y)}
             val candidateRoom=RoomGeometry(current.name,candidate,current.wallHeights,current.wallThicknesses,current.openingsByWall,current.wallHeightOverrides)
             val errors=GeometryEngine.validateRoom(candidateRoom,current.height)
             if(errors.isNotEmpty()) geometryMessage="Координаты не применены: "+errors.joinToString("; ")
             else {
               rooms=rooms.toMutableList().also{it[selected]=current.copy(
                 width=(candidate.maxOf{it.x}-candidate.minOf{it.x}).coerceAtLeast(0.01),
                 length=(candidate.maxOf{it.y}-candidate.minOf{it.y}).coerceAtLeast(0.01),
                 vertices=candidate,wallMeasurements=emptyList()
               )}
               vertexDrafts=vertexDrafts-current.id
               geometryMessage="Координаты вершины применены. Площади и объёмы пересчитаны."
             }
           }
         },modifier=Modifier.fillMaxWidth()){Text("Применить координаты вершины")}
        NumberField("Высота помещения, м",roomH){roomH=it}
        Text("Координаты вершин контура (метры)",fontWeight=FontWeight.SemiBold)
        val initialVertexText=current.vertices.joinToString("\n"){ "${String.format(java.util.Locale.US,"%.3f",it.x)};${String.format(java.util.Locale.US,"%.3f",it.y)}" }
        val draft=vertexDrafts[current.id]?:initialVertexText
        OutlinedTextField(value=draft,onValueChange={vertexDrafts=vertexDrafts+(current.id to it)},modifier=Modifier.fillMaxWidth(),minLines=4,label={Text("X;Y — одна вершина на строку")})
        Text("Пример: 0;0  •  4;0  •  4;3  •  0;3. Вершины задаются по порядку обхода контура.",fontSize=11.sp,color=Color.LightGray)
        Button(onClick={
          val parsed=runCatching {
            draft.lines().filter{it.isNotBlank()}.map { line ->
              val pair=line.trim().split(";")
              require(pair.size==2){"В каждой строке укажи X;Y через точку с запятой"}
              Vec2(pair[0].trim().replace(',','.').toDouble(),pair[1].trim().replace(',','.').toDouble())
            }
          }.getOrElse { geometryMessage=it.message?: "Ошибка координат";emptyList() }
          if(parsed.size>=3) {
            val sameCount=parsed.size==current.vertices.size
            if(!sameCount&&current.openingsByWall.isNotEmpty()) geometryMessage="Нельзя менять число вершин, пока у помещения есть проёмы. Сначала перенеси или удали проёмы."
            else if(GeometryEngine.selfIntersects(parsed)||GeometryEngine.area(parsed)<=1e-8) geometryMessage="Контур пересекается сам с собой или имеет нулевую площадь."
            else {
              val updated=current.copy(
                width=(parsed.maxOf{it.x}-parsed.minOf{it.x}).coerceAtLeast(0.01),
                length=(parsed.maxOf{it.y}-parsed.minOf{it.y}).coerceAtLeast(0.01),
                height=roomH.toDoubleOrNull()?:current.height,
                vertices=parsed,
                wallHeights=List(parsed.size){roomH.toDoubleOrNull()?:current.height},
                wallThicknesses=if(sameCount&&current.wallThicknesses.size==parsed.size)current.wallThicknesses else List(parsed.size){0.0},
                openingsByWall=if(sameCount)current.openingsByWall else emptyMap(),
                wallHeightOverrides=if(sameCount&&current.wallHeightOverrides.size==parsed.size)current.wallHeightOverrides else emptyList(),
                wallHeightEndOverrides=if(sameCount&&current.wallHeightEndOverrides.size==parsed.size)current.wallHeightEndOverrides else emptyList()
              )
              val errors=GeometryEngine.validateRoom(RoomGeometry(updated.name,updated.vertices,updated.wallHeights,updated.wallThicknesses,updated.openingsByWall,updated.wallHeightOverrides,wallHeightEndOverrides=updated.wallHeightEndOverrides),updated.height)
              if(errors.isNotEmpty())geometryMessage=errors.joinToString("; ")
              else {rooms=rooms.toMutableList().also{it[selected]=updated};geometryMessage="Контур обновлён без автоматической подгонки. Сохрани проект."}
            }
          } else if(geometryMessage.isBlank()) geometryMessage="Нужно минимум три корректные вершины."
        },modifier=Modifier.fillMaxWidth()){Text("Применить контур")}
        if(geometryMessage.isNotBlank())Text(geometryMessage,color=if(geometryMessage.startsWith("Контур обновлён"))accent else Color(0xFFFFB86B),fontSize=12.sp)
      } }
      item { CardBlock {
        Text("Обмер по длинам и направлениям",fontSize=18.sp,fontWeight=FontWeight.Bold)
        Text("Введи каждый участок отдельной строкой: длина в метрах; азимут в градусах. Угол отсчитывается против часовой стрелки от оси X: 0° — вправо, 90° — вверх. Приложение покажет невязку и не будет принудительно замыкать неточный контур.")
        var measurementDraft by remember(current.id) { mutableStateOf(current.wallMeasurements.joinToString("\n"){ "${it.lengthM};${it.azimuthDegrees}" }) }
        var measurementStatus by remember(current.id) { mutableStateOf("") }
        OutlinedTextField(value=measurementDraft,onValueChange={measurementDraft=it},modifier=Modifier.fillMaxWidth(),minLines=4,label={Text("Длина;азимут — одна стена на строку")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal))
        Button(onClick={
          val parsed=runCatching {
            measurementDraft.lines().filter{it.isNotBlank()}.mapIndexed { index,line ->
              val pair=line.trim().split(";")
              require(pair.size==2) { "В строке ${index+1} укажи длину и азимут через ;" }
              WallVectorMeasurement(pair[0].trim().replace(',','.').toDouble(),pair[1].trim().replace(',','.').toDouble(),"Стена ${index+1}")
            }
          }.getOrElse { measurementStatus=it.message?: "Ошибка обмеров";emptyList() }
          if(parsed.size>=3) {
            val result=runCatching { GeometryEngine.buildFromMeasurements(parsed,tolerance=0.01) }
              .getOrElse { measurementStatus=it.message?: "Некорректные обмеры";null }
            if(result!=null) {
              if(!result.closure.closed) {
                rooms=rooms.toMutableList().also{it[selected]=current.copy(wallMeasurements=parsed)}
                measurementStatus="Невязка ${df.format(result.closure.residual)} м (ΔX ${df.format(result.closure.dx)}; ΔY ${df.format(result.closure.dy)}). Исходные обмеры сохранены; существующий контур не изменён."
              } else if(parsed.size!=current.vertices.size&&current.openingsByWall.isNotEmpty()) {
                rooms=rooms.toMutableList().also{it[selected]=current.copy(wallMeasurements=parsed)}
                measurementStatus="Обмеры сохранены, но число стен изменилось. Перенеси или удали проёмы перед применением нового контура."
              } else {
                val sameCount=parsed.size==current.vertices.size
                val updated=current.copy(
                  width=(result.roomVertices.maxOf{it.x}-result.roomVertices.minOf{it.x}).coerceAtLeast(0.01),
                  length=(result.roomVertices.maxOf{it.y}-result.roomVertices.minOf{it.y}).coerceAtLeast(0.01),
                  vertices=result.roomVertices,
                  wallMeasurements=parsed,
                  wallHeights=if(sameCount&&current.wallHeights.size==parsed.size)current.wallHeights else List(parsed.size){current.height},
                  wallThicknesses=if(sameCount&&current.wallThicknesses.size==parsed.size)current.wallThicknesses else List(parsed.size){0.0},
                  openingsByWall=if(sameCount)current.openingsByWall else emptyMap(),
                  wallHeightOverrides=if(sameCount&&current.wallHeightOverrides.size==parsed.size)current.wallHeightOverrides else emptyList(),
                  wallHeightEndOverrides=if(sameCount&&current.wallHeightEndOverrides.size==parsed.size)current.wallHeightEndOverrides else emptyList()
                )
                val errors=GeometryEngine.validateRoom(RoomGeometry(updated.name,updated.vertices,updated.wallHeights,updated.wallThicknesses,updated.openingsByWall,updated.wallHeightOverrides,parsed,updated.wallHeightEndOverrides),updated.height)
                if(errors.isNotEmpty())measurementStatus=errors.joinToString("; ")
                else {
                  rooms=rooms.toMutableList().also{it[selected]=updated}
                  vertexDrafts=vertexDrafts-current.id
                  measurementStatus="Контур построен по исходным обмерам. Невязка: ${df.format(result.closure.residual)} м. Автоматическая корректировка не выполнялась."
                }
              }
            }
          } else if(measurementStatus.isBlank()) measurementStatus="Нужно минимум три участка обмера."
        },modifier=Modifier.fillMaxWidth()){Text("Рассчитать и применить обмеры")}
        if(measurementStatus.isNotBlank()) Text(measurementStatus,color=if(measurementStatus.startsWith("Контур построен"))accent else Color(0xFFFFB86B),fontSize=12.sp)
        if(current.wallMeasurements.isNotEmpty()) Text("Сохранено исходных участков: ${current.wallMeasurements.size}",fontSize=11.sp,color=Color.LightGray)
      } }
      item { CardBlock { Text("Высота и толщина каждой стены",fontSize=18.sp,fontWeight=FontWeight.Bold)
        Text("Параметры независимы для каждой стены. Высота применяется по всей длине выбранной стены; толщина задаётся в миллиметрах.")
        current.vertices.indices.forEach { i ->
          val next=(i+1)%current.vertices.size
          val edgeLength=current.vertices[i].distanceTo(current.vertices[next])
          val baseHeights=if(current.wallHeights.size==current.vertices.size)current.wallHeights else List(current.vertices.size){current.height}
          val heights=current.vertices.indices.map{j->current.wallHeightOverrides.getOrNull(j) ?: baseHeights[j]}
          val thicknesses=if(current.wallThicknesses.size==current.vertices.size)current.wallThicknesses else List(current.vertices.size){0.0}
          val heightKey="${current.id}:wall:${i}:height"
          val thicknessKey="${current.id}:wall:${i}:thickness"
          Card(colors=CardDefaults.cardColors(containerColor=navy),shape=RoundedCornerShape(12.dp)) {
            Column(Modifier.fillMaxWidth().padding(10.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
              Text("Стена ${i+1} • длина ${df.format(edgeLength)} м",fontWeight=FontWeight.SemiBold)
              OutlinedTextField(value=wallDimensionDrafts[heightKey]?:df.format(heights[i]).replace(',','.'),onValueChange={value->
                wallDimensionDrafts=wallDimensionDrafts+(heightKey to value)
                val parsed=value.replace(',','.').toDoubleOrNull()
                if(parsed!=null&&parsed.isFinite()&&parsed in 0.1..20.0){
                  val updatedHeights=heights.toMutableList().also{it[i]=parsed}
                  val updatedEndHeights=current.vertices.indices.map{j->if(j==i)parsed else current.wallHeightEndOverrides.getOrNull(j) ?: current.wallHeightOverrides.getOrNull(j) ?: baseHeights[(j+1)%baseHeights.size]}
                  rooms=rooms.toMutableList().also{it[selected]=current.copy(wallHeightOverrides=updatedHeights,wallHeightEndOverrides=updatedEndHeights)}
                }
              },label={Text("Высота стены, м")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),singleLine=true,modifier=Modifier.fillMaxWidth())
              OutlinedTextField(value=wallDimensionDrafts[thicknessKey]?:df.format(thicknesses[i]*1000.0).replace(',','.'),onValueChange={value->
                wallDimensionDrafts=wallDimensionDrafts+(thicknessKey to value)
                val parsed=value.replace(',','.').toDoubleOrNull()
                if(parsed!=null&&parsed.isFinite()&&parsed in 0.0..2000.0){
                  val updatedThicknesses=thicknesses.toMutableList().also{it[i]=parsed/1000.0}
                  rooms=rooms.toMutableList().also{it[selected]=current.copy(wallThicknesses=updatedThicknesses)}
                }
              },label={Text("Толщина стены, мм")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),singleLine=true,modifier=Modifier.fillMaxWidth())
            }
          }
        }
        Text("Изменения учитываются в площадях стен и проверке высоты проёмов. Нажми «Сохранить», чтобы записать параметры.",fontSize=11.sp,color=Color.LightGray)
      } }
      item { CardBlock { Text("Двери, окна и проёмы",fontSize=18.sp,fontWeight=FontWeight.Bold)
        Text("Добавляй проём на выбранную стену. Позиция измеряется от её начальной вершины; размеры проверяются по геометрии.")
        Row(horizontalArrangement=Arrangement.spacedBy(6.dp),verticalAlignment=Alignment.CenterVertically) {
          Text("Стена",color=Color.LightGray)
          current.vertices.indices.forEach { i -> FilterChip(selected=openingWallIndex==i,onClick={openingWallIndex=i},label={Text("${i+1}")}) }
        }
        varType(openingKind,{openingKind=it;openingName=it;if(it=="Дверь"){openingWidth="0.9";openingHeight="2.1";openingSill="0"}else if(it=="Окно"){openingWidth="1.2";openingHeight="1.4";openingSill="0.9"}else{openingWidth="0.6";openingHeight="0.6";openingSill="1.0"}},listOf("Дверь","Окно","Ниша","Прочее"))
        NumberField("Название",openingName){openingName=it}
        NumberField("Отступ от начала стены, м",openingOffset){openingOffset=it}
        NumberField("Ширина, м",openingWidth){openingWidth=it}
        NumberField("Высота, м",openingHeight){openingHeight=it}
        NumberField("Высота от пола / подоконника, м",openingSill){openingSill=it}
        NumberField("Глубина откоса, м",openingDepth){openingDepth=it}
        Button(onClick={
          val candidate=Opening(java.util.UUID.randomUUID().toString(),openingName.ifBlank{openingKind},openingWidth.toDoubleOrNull()?:0.0,openingHeight.toDoubleOrNull()?:0.0,openingSill.toDoubleOrNull()?:0.0,openingDepth.toDoubleOrNull()?:0.0,openingKind=="Окно",openingOffset.toDoubleOrNull()?:0.0,openingKind)
          val updated=current.copy(openingsByWall=current.openingsByWall+(openingWallIndex to (current.openingsByWall[openingWallIndex].orEmpty()+candidate)))
          val roomGeometry=RoomGeometry(updated.name,updated.vertices,if(updated.wallHeights.size==updated.vertices.size)updated.wallHeights else List(updated.vertices.size){updated.height},updated.wallThicknesses,updated.openingsByWall,updated.wallHeightOverrides,wallHeightEndOverrides=updated.wallHeightEndOverrides)
          val errors=runCatching{GeometryEngine.validateOpenings(roomGeometry)}.getOrElse{listOf(it.message?: "Некорректные параметры")}
          if(errors.isEmpty()){
            rooms=rooms.toMutableList().also{it[selected]=updated}
            openingMessage="Проём добавлен. Не забудь сохранить проект."
          }else openingMessage=errors.joinToString("; ")
        },modifier=Modifier.fillMaxWidth()){Text("Добавить проём")}
        if(openingMessage.isNotBlank()) Text(openingMessage,color=if(openingMessage.startsWith("Проём добавлен"))accent else Color(0xFFFFB86B),fontSize=12.sp)
        current.openingsByWall.toSortedMap().forEach { (wall,items) ->
          items.forEach { opening ->
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
              Column(Modifier.weight(1f)) {
                Text("${opening.name} • стена ${wall+1}",fontWeight=FontWeight.SemiBold)
                Text("${df.format(opening.width)} × ${df.format(opening.height)} м; отступ ${df.format(opening.offsetFromStart)} м",fontSize=12.sp,color=Color.LightGray)
              }
              TextButton(onClick={
                val remaining=items.filterNot{it.id==opening.id}
                val map=current.openingsByWall.toMutableMap()
                if(remaining.isEmpty())map.remove(wall) else map[wall]=remaining
                rooms=rooms.toMutableList().also{it[selected]=current.copy(openingsByWall=map)}
                openingMessage="Проём удалён. Сохрани проект."
              }){Text("Удалить")}
            }
          }
        }
        Text("Площадь проёмов: ${df.format(current.geometry.openingArea)} м² • откосы: ${df.format(current.geometry.revealArea)} м²",color=accent); Text("Схема 2D",fontWeight=FontWeight.Bold); RoomPlan(current,Modifier.fillMaxWidth().height(220.dp)); Text("Контур помещения отображается по фактическим вершинам.") } }
      item { CardBlock { Text("Автоматические расчёты",fontWeight=FontWeight.Bold); Text("Пол: ${df.format(current.floor)} м²"); Text("Стены (чистая площадь): ${df.format(current.walls)} м²"); Text("Объём стен по заданной толщине: ${df.format(current.geometry.netWallVolumeM3)} м³"); Text("Периметр: ${df.format(current.perimeter)} м"); Text("Потолок: ${df.format(current.floor)} м²") } }
    }
    "3D" -> LazyColumn(verticalArrangement=Arrangement.spacedBy(12.dp),contentPadding=PaddingValues(vertical=12.dp)){
      item { CardBlock {
        Text("Интерактивная 3D-модель помещения",fontSize=19.sp,fontWeight=FontWeight.Bold)
        Text("${current.name} • ${current.vertices.size} стен • высота и толщина учитываются по каждой стене")
        RoomModel3D(
          current,view3dAngle,view3dZoom,hidden3dWalls,Offset(view3dPanX,view3dPanY),
          onCameraGesture={pan,zoomChange,rotationChange->
            view3dPanX=(view3dPanX+pan.x).coerceIn(-1200f,1200f)
            view3dPanY=(view3dPanY+pan.y).coerceIn(-1200f,1200f)
            view3dZoom=(view3dZoom*zoomChange).coerceIn(0.65f,2.4f)
            view3dAngle=(view3dAngle+rotationChange+360f)%360f
          },
          modifier=Modifier.fillMaxWidth().height(340.dp)
        )
        Text("Управление жестами: один палец — перемещение, два пальца — масштаб и поворот.",fontSize=11.sp,color=Color.LightGray)
        Text("Видимость стен",fontWeight=FontWeight.SemiBold)
        Row(horizontalArrangement=Arrangement.spacedBy(6.dp),modifier=Modifier.fillMaxWidth()){
          current.vertices.indices.forEach { wallIndex ->
            FilterChip(selected=wallIndex !in hidden3dWalls,onClick={hidden3dWalls=if(wallIndex in hidden3dWalls) hidden3dWalls-wallIndex else hidden3dWalls+wallIndex},label={Text("Стена ${wallIndex+1}")})
          }
        }
        OutlinedButton(onClick={hidden3dWalls=emptySet()},modifier=Modifier.fillMaxWidth()){Text("Показать все стены")}
        Text("Поворот модели: ${view3dAngle.toInt()}°")
        Slider(value=view3dAngle,onValueChange={view3dAngle=it},valueRange=0f..360f)
        Text("Масштаб: ${(view3dZoom*100).toInt()}%")
        Slider(value=view3dZoom,onValueChange={view3dZoom=it},valueRange=0.65f..2.4f)
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
          OutlinedButton(onClick={view3dAngle=(view3dAngle-15f+360f)%360f},modifier=Modifier.weight(1f)){Text("↶ 15°")}
          OutlinedButton(onClick={view3dAngle=(view3dAngle+15f)%360f},modifier=Modifier.weight(1f)){Text("15° ↷")}
          OutlinedButton(onClick={view3dAngle=35f;view3dZoom=1f;view3dPanX=0f;view3dPanY=0f},modifier=Modifier.weight(1f)){Text("Сброс")}
        }
      } }
      item { CardBlock {
        Text("Параметры модели",fontWeight=FontWeight.Bold)
        Text("Площадь пола: ${df.format(current.floor)} м²")
        Text("Чистая площадь стен: ${df.format(current.walls)} м²")
        Text("Объём стен: ${df.format(current.geometry.netWallVolumeM3)} м³")
        Text("Проёмы: ${current.openingsByWall.values.sumOf{it.size}} • площадь ${df.format(current.geometry.openingArea)} м²")
        Text("Это первый геометрический 3D-просмотр: контур и высоты стен отображаются в объёме. Следующий этап — корректные вырезы дверей/окон, ниши, интерактивное выделение элементов и скрытие стен.")
      } }
    }
    "Смета" -> LazyColumn(verticalArrangement=Arrangement.spacedBy(10.dp),contentPadding=PaddingValues(vertical=12.dp)){
      item { CardBlock { Text("Каталог работ Smeta Pro + новые разделы",fontWeight=FontWeight.Bold); OutlinedTextField(priceSearch,{priceSearch=it},label={Text("Поиск работ")},modifier=Modifier.fillMaxWidth()); Text("Выбрано позиций: ${lines.size}  •  Итого: ${df.format(lines.sumOf{it.total})} ₽",color=accent,fontWeight=FontWeight.Bold) } }
      item { CardBlock {
        Text("Автоматическая ведомость объёмов",fontWeight=FontWeight.Bold)
        val takeoff=GeometryTakeoff.calculate(rooms)
        Text("По всем помещениям: ${rooms.size} • пол ${df.format(takeoff.floorAreaM2)} м² • потолок ${df.format(takeoff.ceilingAreaM2)} м²")
        Text("Чистая площадь стен: ${df.format(takeoff.netWallAreaM2)} м² • проёмы: ${df.format(takeoff.openingAreaM2)} м² • откосы: ${df.format(takeoff.revealAreaM2)} м²")
        rooms.forEach { room -> Text("${room.name}: пол ${df.format(room.floor)} м² • стены ${df.format(room.walls)} м² • откосы ${df.format(room.geometry.revealArea)} м²",fontSize=12.sp,color=Color.LightGray) }
        Divider(color=Color(0xFF35465B))
        Text("Выбери работы для переноса рассчитанных объёмов в смету:")
        Row(verticalAlignment=Alignment.CenterVertically){Checkbox(checked=includeWallPlaster,onCheckedChange={includeWallPlaster=it});Text("Штукатурка стен (${df.format(takeoff.netWallAreaM2)} м²)")}
        Row(verticalAlignment=Alignment.CenterVertically){Checkbox(checked=includeWallPrimer,onCheckedChange={includeWallPrimer=it});Text("Грунтование стен (${df.format(takeoff.netWallAreaM2)} м²)")}
        Row(verticalAlignment=Alignment.CenterVertically){Checkbox(checked=includeWallWaterproofing,onCheckedChange={includeWallWaterproofing=it});Text("Гидроизоляция стен, 2 слоя (${df.format(takeoff.netWallAreaM2)} м²)")}
        Row(verticalAlignment=Alignment.CenterVertically){Checkbox(checked=includeFloorScreed,onCheckedChange={includeFloorScreed=it});Text("Стяжка пола (${df.format(takeoff.floorAreaM2)} м²)")}
        Row(verticalAlignment=Alignment.CenterVertically){Checkbox(checked=includeFloorWaterproofing,onCheckedChange={includeFloorWaterproofing=it});Text("Гидроизоляция пола, 2 слоя (${df.format(takeoff.floorAreaM2)} м²)")}
        Row(verticalAlignment=Alignment.CenterVertically){Checkbox(checked=includeFloorTile,onCheckedChange={includeFloorTile=it});Text("Укладка керамогранита на пол (${df.format(takeoff.floorAreaM2)} м²)")}
        Row(verticalAlignment=Alignment.CenterVertically){Checkbox(checked=includeWallTile,onCheckedChange={includeWallTile=it});Text("Облицовка стен керамогранитом (${df.format(takeoff.netWallAreaM2)} м²)")}
        if(catalogItems.any{it.n=="Шпаклёвка стен"}) Row(verticalAlignment=Alignment.CenterVertically){Checkbox(checked=includeWallPutty,onCheckedChange={includeWallPutty=it});Text("Шпаклёвка стен (${df.format(takeoff.netWallAreaM2)} м²)")}
        if(catalogItems.any{it.n=="Покраска стен"}) Row(verticalAlignment=Alignment.CenterVertically){Checkbox(checked=includeWallPaint,onCheckedChange={includeWallPaint=it});Text("Покраска стен (${df.format(takeoff.netWallAreaM2)} м²)")}
        if(catalogItems.any{it.n=="Декоративная штукатурка стен"}) Row(verticalAlignment=Alignment.CenterVertically){Checkbox(checked=includeWallDecorative,onCheckedChange={includeWallDecorative=it});Text("Декоративная штукатурка стен (${df.format(takeoff.netWallAreaM2)} м²)")}
        if(catalogItems.any{it.n=="Шпаклёвка потолка"}) Row(verticalAlignment=Alignment.CenterVertically){Checkbox(checked=includeCeilingPutty,onCheckedChange={includeCeilingPutty=it});Text("Шпаклёвка потолка (${df.format(takeoff.ceilingAreaM2)} м²)")}
        if(catalogItems.any{it.n=="Покраска потолка"}) Row(verticalAlignment=Alignment.CenterVertically){Checkbox(checked=includeCeilingPaint,onCheckedChange={includeCeilingPaint=it});Text("Покраска потолка (${df.format(takeoff.ceilingAreaM2)} м²)")}
        if(catalogItems.any{it.n=="Монтаж натяжного потолка"}) Row(verticalAlignment=Alignment.CenterVertically){Checkbox(checked=includeStretchCeiling,onCheckedChange={includeStretchCeiling=it});Text("Монтаж натяжного потолка (${df.format(takeoff.ceilingAreaM2)} м²)")}
        if(catalogItems.any{it.n=="Укладка ламината"}) Row(verticalAlignment=Alignment.CenterVertically){Checkbox(checked=includeFloorLaminate,onCheckedChange={includeFloorLaminate=it});Text("Укладка ламината (${df.format(takeoff.floorAreaM2)} м²)")}
        Row(verticalAlignment=Alignment.CenterVertically){Checkbox(checked=includeCeilingPrimer,onCheckedChange={includeCeilingPrimer=it});Text("Грунтование потолка (${df.format(takeoff.ceilingAreaM2)} м²)")}
        Button(onClick={
          val generated=AutomaticEstimateGenerator.generate(
            takeoff=takeoff,
            catalog=catalogItems,
            options=AutomaticEstimateOptions(
              wallPlaster=includeWallPlaster,
              wallPrimer=includeWallPrimer,
              floorScreed=includeFloorScreed,
              ceilingPrimer=includeCeilingPrimer,
              wallWaterproofing=includeWallWaterproofing,
              floorWaterproofing=includeFloorWaterproofing,
              wallTile=includeWallTile,
              floorTile=includeFloorTile,
              wallPutty=includeWallPutty,
              wallPaint=includeWallPaint,
              wallDecorative=includeWallDecorative,
              ceilingPutty=includeCeilingPutty,
              ceilingPaint=includeCeilingPaint,
              stretchCeiling=includeStretchCeiling,
              floorLaminate=includeFloorLaminate
            )
          )
          lines=lines.filterNot{it.category=="Автообъёмы"}+generated
          saveMessage=if(generated.isEmpty()) "Автоматические позиции удалены из сметы. Ручные позиции сохранены." else "Автоматические объёмы обновлены в смете. Ручные позиции сохранены."
        },modifier=Modifier.fillMaxWidth()){Text("Обновить выбранные объёмы в смете")}
        Text("Объёмы берутся из геометрии всех помещений с учётом проёмов. Повторное обновление заменяет только автоматические строки и сохраняет ручные. Цены подставляются из каталога; позиции без найденной цены не создаются.",fontSize=11.sp,color=Color.LightGray)
      } }
      item { CardBlock {
        Text("Управление прайс-листом",fontWeight=FontWeight.Bold)
        OutlinedTextField(value=priceBookName,onValueChange={priceBookName=it},label={Text("Название новой версии")},modifier=Modifier.fillMaxWidth(),singleLine=true)
        if(priceBook.versions.isNotEmpty()) {
          Text("Сохранённые версии",fontSize=12.sp,color=Color.LightGray)
          Column(verticalArrangement=Arrangement.spacedBy(4.dp)) {
            priceBook.versions.asReversed().forEach { version ->
              Row(verticalAlignment=Alignment.CenterVertically) {
                FilterChip(
                  selected=priceBook.activeVersionId==version.id,
                  onClick={
                    val next=priceBook.copy(activeVersionId=version.id)
                    priceBook=next
                    catalogItems=mergeCatalog(version.items)
                    catalogPriceDrafts=emptyMap()
                    priceBookName=version.name
                    projectStore.savePriceBook(next)
                    saveMessage="Активирован прайс-лист «${version.name}»"
                  },
                  label={Text(version.name)}
                )
                TextButton(onClick={priceBookName=version.name}){Text("Название")}
              }
            }
          }
        }
        Divider(color=Color(0xFF35465B))
        Text("Добавить собственную работу",fontWeight=FontWeight.SemiBold)
        OutlinedTextField(value=newCatalogWorkName,onValueChange={newCatalogWorkName=it},label={Text("Наименование работы или услуги")},modifier=Modifier.fillMaxWidth(),singleLine=true)
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(value=newCatalogWorkCategory,onValueChange={newCatalogWorkCategory=it},label={Text("Раздел")},modifier=Modifier.weight(1f),singleLine=true)
          OutlinedTextField(value=newCatalogWorkUnit,onValueChange={newCatalogWorkUnit=it},label={Text("Ед. изм.")},modifier=Modifier.width(100.dp),singleLine=true)
        }
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp),verticalAlignment=Alignment.CenterVertically) {
          OutlinedTextField(value=newCatalogWorkPrice,onValueChange={newCatalogWorkPrice=it},label={Text("Цена, ₽")},modifier=Modifier.weight(1f),singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal))
          Button(onClick={
            val name=newCatalogWorkName.trim()
            val category=newCatalogWorkCategory.trim()
            val unit=newCatalogWorkUnit.trim()
            val price=newCatalogWorkPrice.trim().replace(',','.').toDoubleOrNull()
            when {
              name.isBlank() || category.isBlank() || unit.isBlank() -> saveMessage="Заполни название, раздел и единицу измерения."
              price==null || !price.isFinite() || price<0.0 -> saveMessage="Укажи корректную цену — число не меньше нуля."
              catalogItems.any{it.n.equals(name,true)} -> saveMessage="Такая позиция уже есть в каталоге."
              else -> {
                catalogItems=catalogItems+CatalogWork(name,price,unit,category)
                catalogPriceDrafts=catalogPriceDrafts+(name to price.toString())
                newCatalogWorkName=""
                saveMessage="Позиция добавлена в черновик прайса. Сохрани новую версию, чтобы закрепить изменения."
              }
            }
          }){Text("Добавить")}
        }
        Button(onClick={
          var valid=true
          val edited=catalogItems.map { item ->
            val raw= catalogPriceDrafts[item.n]?.trim()?.replace(',','.')
            if(raw.isNullOrEmpty()) item else {
              val value=raw.toDoubleOrNull()
              if(value==null || !value.isFinite() || value<0.0) { valid=false; item }
              else item.copy(p=value)
            }
          }
          if(!valid || priceBookName.isBlank()) {
            saveMessage="Проверь цены: нужны конечные числа не меньше нуля и название версии."
          } else {
            val version=PriceBookVersion(java.util.UUID.randomUUID().toString(),priceBookName.trim(),System.currentTimeMillis(),edited)
            val next=PriceBook(version.id,priceBook.versions+version)
            projectStore.savePriceBook(next)
            priceBook=next
            catalogItems=edited
            catalogPriceDrafts=emptyMap()
            saveMessage="Прайс-лист «${version.name}» сохранён. Версий: ${next.versions.size}."
          }
        },modifier=Modifier.fillMaxWidth()){Text("Сохранить цены новой версией")}
        Text("Цены сохраняются отдельно от проектов. Можно хранить несколько прайс-листов и переключаться между ними. Новые цены используются при добавлении работ и обновлении автоматической сметы.",fontSize=11.sp,color=Color.LightGray)
      } }
      items(catalogItems.filter{priceSearch.isBlank()||it.n.contains(priceSearch,true)||it.c.contains(priceSearch,true)}) { item -> CardBlock {
        Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
          Column(Modifier.weight(1f)) {
            Text(item.n,fontWeight=FontWeight.SemiBold)
            Text("${item.c} • ${item.u}${if(item.from) " • стартовая цена" else ""}",color=Color.LightGray,fontSize=12.sp)
          }
          OutlinedTextField(
            value=catalogPriceDrafts[item.n] ?: df.format(item.p).replace(',','.'),
            onValueChange={catalogPriceDrafts=catalogPriceDrafts+(item.n to it)},
            modifier=Modifier.width(112.dp),
            label={Text("Цена ₽")},
            singleLine=true,
            keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal)
          )
          IconButton(onClick={lines=lines+EstimateLine(item.n,item.u,item.p,1.0,item.c)}){Icon(Icons.Default.Add,tint=accent,contentDescription="Добавить")}
        }
      } }
      item { CardBlock {
        Text("Экспорт проекта",fontWeight=FontWeight.Bold)
        Text("PDF: титульная страница, планы помещений, ведомости и смета.",fontSize=12.sp,color=Color.LightGray)
        Button(onClick={showPdfOptions=true},modifier=Modifier.fillMaxWidth()){Icon(Icons.Default.PictureAsPdf,null);Text(" Сформировать PDF-отчёт")}
        OutlinedButton(onClick={
          runCatching { XlsxReportExporter.export(project,rooms,lines) }
            .onSuccess { bytes ->
              pendingXlsxBytes=bytes
              xlsxSaveLauncher.launch("PLAN-MASTER-3D-${project.replace(Regex("[^A-Za-zА-Яа-я0-9_-]"), "_")}.xlsx")
            }
            .onFailure { android.widget.Toast.makeText(ctx,"Ошибка подготовки XLSX: " + (it.message ?: "ошибка"),android.widget.Toast.LENGTH_LONG).show() }
        },modifier=Modifier.fillMaxWidth()){Text("Экспорт ведомостей и сметы в Excel (XLSX)")}
      } }
      if(lines.isNotEmpty()) item { CardBlock { Text("Смета",fontWeight=FontWeight.Bold); lines.forEachIndexed{i,line->Text("${line.name}: ${df.format(line.qty)} ${line.unit} × ${df.format(line.price)} ₽ = ${df.format(line.total)} ₽"); Row { TextButton(onClick={lines=lines.toMutableList().also{it[i]=line.copy(qty=(line.qty-1).coerceAtLeast(0.0))}}){Text("−")}; TextButton(onClick={lines=lines.toMutableList().also{it[i]=line.copy(qty=line.qty+1)}}){Text("+")}; TextButton(onClick={lines=lines.toMutableList().also{it.removeAt(i)}}){Text("Удалить")} } }; Text("ИТОГО: ${df.format(lines.sumOf{it.total})} ₽",fontWeight=FontWeight.Bold); Button(onClick={shareEstimate(ctx,lines)},modifier=Modifier.fillMaxWidth()){Text("Поделиться сметой / текст")} ; OutlinedButton(onClick={shareCsv(ctx,lines)},modifier=Modifier.fillMaxWidth()){Text("Экспорт таблицы CSV (Excel)")} } }
    }
    "Материалы" -> LazyColumn(verticalArrangement=Arrangement.spacedBy(12.dp),contentPadding=PaddingValues(vertical=12.dp)){
      item { CardBlock {
        Text("Калькулятор расхода материалов",fontSize=19.sp,fontWeight=FontWeight.Bold)
        varType(materialMode,{materialMode=it},listOf("Штукатурка","Шпаклёвка","Краска","Грунтовка","Клей"))
        val projectTakeoff=GeometryTakeoff.calculate(rooms)
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp),modifier=Modifier.fillMaxWidth()) {
          OutlinedButton(onClick={materialArea=current.walls.toString()},modifier=Modifier.weight(1f)){Text("Стены комнаты")}
          OutlinedButton(onClick={materialArea=current.floor.toString()},modifier=Modifier.weight(1f)){Text("Пол комнаты")}
        }
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp),modifier=Modifier.fillMaxWidth()) {
          OutlinedButton(onClick={materialArea=projectTakeoff.netWallAreaM2.toString()},modifier=Modifier.weight(1f)){Text("Стены всего объекта")}
          OutlinedButton(onClick={materialArea=projectTakeoff.floorAreaM2.toString()},modifier=Modifier.weight(1f)){Text("Пол всего объекта")}
        }
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp),modifier=Modifier.fillMaxWidth()) {
          OutlinedButton(onClick={materialArea=projectTakeoff.ceilingAreaM2.toString()},modifier=Modifier.weight(1f)){Text("Потолок всего объекта")}
          OutlinedButton(onClick={materialArea=projectTakeoff.revealAreaM2.toString()},modifier=Modifier.weight(1f)){Text("Откосы всего объекта")}
        }
        NumberField("Площадь, м²",materialArea){materialArea=it}
        NumberField("Толщина слоя, мм",materialLayer){materialLayer=it}
        NumberField("Расход на 1 м² (на 1 мм для сухих смесей)",materialCoverage){materialCoverage=it}
        NumberField("Фасовка, кг или л",materialPack){materialPack=it}
        NumberField("Запас, %",materialReserve){materialReserve=it}
        val area=materialArea.toDoubleOrNull()?:0.0
        val layer=materialLayer.toDoubleOrNull()?:1.0
        val rate=materialCoverage.toDoubleOrNull()?:0.0
        val pack=materialPack.toDoubleOrNull()?:1.0
        val reserve=materialReserve.toDoubleOrNull()?:0.0
        val dryMix=materialMode=="Штукатурка"||materialMode=="Шпаклёвка"
        val calculation=runCatching { MaterialCalculator.calculate(MaterialCalculationInput(area,layer,rate,pack,reserve,dryMix)) }.getOrNull()
        Text(if(calculation!=null) "Расход: ${df.format(calculation.baseAmount)} кг/л • с запасом: ${df.format(calculation.amountWithReserve)} кг/л • упаковок: ${calculation.packages}" else "Проверь значения: параметры должны быть конечными, площадь/расход/слой/запас — неотрицательными, фасовка — больше нуля, результат не должен превышать допустимый диапазон.",color=if(calculation!=null)accent else Color(0xFFFFB86B),fontWeight=FontWeight.Bold)
        Text("Для штукатурки и шпаклёвки расход умножается на толщину слоя. Для краски, грунта и клея — на площадь. Запас учитывается отдельно. Используй расход из паспорта конкретного материала.",fontSize=12.sp,color=Color.LightGray)
      } }
    }
    "Раскладка" -> LazyColumn(verticalArrangement=Arrangement.spacedBy(12.dp),contentPadding=PaddingValues(vertical=12.dp)){
      item { CardBlock {
        Text("Калькулятор раскладки",fontSize=19.sp,fontWeight=FontWeight.Bold)
        varType(layoutType,{layoutType=it},listOf("Плитка","Ламинат","Панели"))
        NumberField("Размер элемента A, мм",layoutA){layoutA=it}
        NumberField("Размер элемента B, мм",layoutB){layoutB=it}
        NumberField("Размер помещения A, мм",layoutRoomA){layoutRoomA=it}
        NumberField("Размер помещения B, мм",layoutRoomB){layoutRoomB=it}
        NumberField("Ширина шва/зазора, мм",layoutJoint){layoutJoint=it}
        Slider(waste,{waste=it},valueRange=0f..25f)
        Text("Запас: ${waste.toInt()}%")
        val layout=runCatching {
          JointLayoutCalculator.calculate(LayoutCalculationInput(
            elementAMm=layoutA.replace(',','.').toDouble(),
            elementBMm=layoutB.replace(',','.').toDouble(),
            roomAMm=layoutRoomA.replace(',','.').toDouble(),
            roomBMm=layoutRoomB.replace(',','.').toDouble(),
            jointMm=layoutJoint.replace(',','.').toDouble(),
            reservePercent=waste.toDouble()
          ))
        }.getOrNull()
        if(layout!=null) {
          Text("Площадь помещения: ${df.format(layout.roomAreaM2)} м²",color=accent,fontWeight=FontWeight.Bold)
          Text("Лучшее направление: ${if(layout.rotated) "элемент повёрнут на 90°" else "без поворота"}")
          Text("Ряды: ${layout.rows} • элементов в ряду: ${layout.columns}")
          Text("Всего элементов без запаса: ${layout.pieces} шт.")
          Text("С запасом: ${layout.piecesWithReserve} шт.",color=accent,fontWeight=FontWeight.Bold)
          Text("Площадь закупки: ${df.format(layout.purchaseAreaM2)} м² • ориентировочный остаток: ${df.format(layout.wasteAreaM2)} м²")
        } else Text("Проверь размеры: все значения должны быть конечными и больше нуля, запас — от 0 до 100%.",color=Color(0xFFFFB86B))
        Text("Расчёт сравнивает два направления укладки и учитывает ширину шва. Повторное использование обрезков и сложные рисунки пока не оптимизируются.",fontSize=12.sp,color=Color.LightGray)
      } }
    }
    "Инженерия" -> LazyColumn(verticalArrangement=Arrangement.spacedBy(12.dp),contentPadding=PaddingValues(vertical=12.dp)){
      item { CardBlock {
        Text("Электрика и сантехника",fontSize=19.sp,fontWeight=FontWeight.Bold)
        Text("Размещай инженерные точки касанием по плану или введи координаты X/Y в метрах. Точки сохраняются в проекте.")
        Text("Помещение",fontWeight=FontWeight.SemiBold)
        Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) { rooms.forEachIndexed{i,r->FilterChip(selected=engineeringRoomIndex==i,onClick={engineeringRoomIndex=i;engineeringX="1";engineeringY="1"},label={Text(r.name)})} }
        Text("Тип точки",fontWeight=FontWeight.SemiBold)
        varType(engineeringKind,{engineeringKind=it},listOf("Розетка","Выключатель"))
        varType(engineeringKind,{engineeringKind=it},listOf("Светильник","Вентилятор"))
        varType(engineeringKind,{engineeringKind=it},listOf("Электровывод","Интернет"))
        Divider(color=Color(0xFF35465B))
        varType(engineeringKind,{engineeringKind=it},listOf("Водорозетка","Канализация"))
        varType(engineeringKind,{engineeringKind=it},listOf("Унитаз","Раковина"))
        varType(engineeringKind,{engineeringKind=it},listOf("Смеситель","Душ"))
        varType(engineeringKind,{engineeringKind=it},listOf("Полотенцесушитель","Ванна"))
        varType(engineeringKind,{engineeringKind=it},listOf("Водомер","Инсталляция"))
        val targetRoom=rooms.getOrNull(engineeringRoomIndex) ?: current
        EngineeringPointCanvas(targetRoom,engineeringPoints.filter{it.roomIndex==engineeringRoomIndex},Modifier.fillMaxWidth().height(220.dp)){ placed -> engineeringX=df.format(placed.x).replace(',','.');engineeringY=df.format(placed.y).replace(',','.') }
        NumberField("Координата X, м",engineeringX){engineeringX=it}
        NumberField("Координата Y, м",engineeringY){engineeringY=it}
        OutlinedTextField(value=engineeringNote,onValueChange={engineeringNote=it},label={Text("Примечание (необязательно)")},modifier=Modifier.fillMaxWidth(),singleLine=true)
        val point=Vec2(engineeringX.replace(',','.').toDoubleOrNull()?:Double.NaN,engineeringY.replace(',','.').toDoubleOrNull()?:Double.NaN)
        val pointValid=point.x.isFinite()&&point.y.isFinite()&&GeometryEngine.containsPoint(targetRoom.vertices,point)
        Text(if(pointValid) "Координаты внутри помещения «${targetRoom.name}»" else "Точка должна находиться внутри контура помещения",color=if(pointValid)accent else Color(0xFFFFB86B),fontSize=12.sp)
        Button(onClick={
          engineeringPoints=engineeringPoints+EngineeringPoint(java.util.UUID.randomUUID().toString(),engineeringKind,engineeringRoomIndex,point.x,point.y,engineeringNote.trim())
          engineeringNote=""
          saveMessage="Инженерная точка добавлена и сохранена в проекте."
        },enabled=pointValid,modifier=Modifier.fillMaxWidth()){Text("Добавить точку")}
      } }
      item { CardBlock {
        Text("Точки проекта: ${engineeringPoints.size}",fontWeight=FontWeight.Bold)
        if(engineeringPoints.isEmpty()) Text("Пока нет инженерных точек.")
        engineeringPoints.forEach { p ->
          val roomName=rooms.getOrNull(p.roomIndex)?.name ?: "Помещение удалено"
          Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
              Text(p.kind,fontWeight=FontWeight.SemiBold)
              Text("$roomName • X ${df.format(p.x)} м • Y ${df.format(p.y)} м",fontSize=12.sp,color=Color.LightGray)
              if(p.note.isNotBlank()) Text(p.note,fontSize=11.sp,color=Color.LightGray)
            }
            IconButton(onClick={engineeringPoints=engineeringPoints.filterNot{it.id==p.id}}){Icon(Icons.Default.Delete,"Удалить точку")}
          }
          Divider(color=Color(0xFF35465B))
        }
        Button(onClick={
          val generated=EngineeringEstimateGenerator.generate(engineeringPoints,catalogItems)
          lines=lines.filterNot{it.category.startsWith("Инженерия · ")}+generated
          saveMessage=if(generated.isEmpty()) "Точки сохранены. Для сметы добавь соответствующие расценки в активный прайс." else "Инженерные работы добавлены в смету по активному прайсу."
          tab="Смета"
        },enabled=engineeringPoints.isNotEmpty(),modifier=Modifier.fillMaxWidth()){Text("Рассчитать инженерные работы в смету")}
        Text("Используются только цены активного прайса. Для отсутствующих расценок стоимость не выдумывается.",fontSize=11.sp,color=Color.LightGray)
      } }
    }
    "Демонтаж" -> LazyColumn(verticalArrangement=Arrangement.spacedBy(12.dp),contentPadding=PaddingValues(vertical=12.dp)){
      item { CardBlock {
        Text("Выбор участка демонтажа",fontSize=19.sp,fontWeight=FontWeight.Bold)
        varType(demolitionSurface,{demolitionSurface=it;demolitionRegion=emptyList();demolitionRegionMessage="";if(it!="Стена"){demolitionThickness=if(it=="Потолок")"12.5" else "50";demolitionDensity=if(it=="Потолок")"800" else "2200"}},listOf("Стена","Пол","Потолок"))
        if(demolitionSurface=="Стена"){
          Text("Выбери стену на плане. Длину и отступ можно задать для частичного демонтажа.")
          SelectableRoomPlan(current,selectedDemolitionWallIndex,Modifier.fillMaxWidth().height(210.dp)){ index ->
            selectedDemolitionWallIndex=index;demolitionWall="Стена ${index+1}"
            demolitionLength=df.format(current.vertices[index].distanceTo(current.vertices[(index+1)%current.vertices.size])).replace(',','.')
            demolitionOffset="0";demolitionHeight=current.height.toString()
          }
          Text("Выбран участок: $demolitionWall",color=accent,fontSize=12.sp)
          NumberField("Отступ от начала стены, м",demolitionOffset){demolitionOffset=it}
          NumberField("Длина демонтируемого участка, м",demolitionLength){demolitionLength=it}
          NumberField("Высота участка, м",demolitionHeight){demolitionHeight=it}
        }else{
          Text("Нажимай на план, чтобы отметить вершины зоны демонтажа. Минимум 3 точки; замкни контур автоматически. Все точки должны лежать внутри помещения.")
          RegionSelectionCanvas(current,demolitionRegion,Modifier.fillMaxWidth().height(230.dp)){ point ->
            if(demolitionRegion.size<12) demolitionRegion=demolitionRegion+point
          }
          val valid=GeometryEngine.polygonContainedBy(current.vertices,demolitionRegion)
          val selectedArea=if(valid)GeometryEngine.area(demolitionRegion) else 0.0
          Text("Точек: ${demolitionRegion.size} • площадь зоны: ${df.format(selectedArea)} м²",color=accent,fontWeight=FontWeight.SemiBold)
          Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
            OutlinedButton(onClick={demolitionRegion=demolitionRegion.dropLast(1) },modifier=Modifier.weight(1f),enabled=demolitionRegion.isNotEmpty()){Text("Убрать точку")}
            OutlinedButton(onClick={demolitionRegion=emptyList();demolitionRegionMessage=""},modifier=Modifier.weight(1f)){Text("Очистить")}
          }
          if(!valid&&demolitionRegion.isNotEmpty())Text("Контур пока не замкнут, пересекается или выходит за границы помещения.",color=Color(0xFFFFB86B),fontSize=12.sp)
          NumberField("Глубина снимаемого слоя, мм",demolitionThickness){demolitionThickness=it}
        }
        varType(demolitionMaterial,{demolitionMaterial=it;demolitionDensity=ConstructionMaterialPresets.demolitionDensities[it]?.toString()?:demolitionDensity;if(demolitionSurface=="Пол"&&it=="Цементная стяжка/бетон")demolitionThickness="50"},ConstructionMaterialPresets.demolitionDensities.keys.toList())
        NumberField("Плотность материала, кг/м³",demolitionDensity){demolitionDensity=it}
        NumberField("Коэффициент разрыхления",demolitionBulking){demolitionBulking=it}
      } }
      item { CardBlock {
        Text("Масса мусора и упаковка",fontWeight=FontWeight.Bold)
        NumberField("Объём мешка, литров",bagLitres){bagLitres=it}
        NumberField("Допустимая масса мешка, кг",bagMaxKg){bagMaxKg=it}
        NumberField("Коэффициент заполнения мешка",bagFill){bagFill=it}
        NumberField("Цена демонтажа, ₽/м²",demolitionRate){demolitionRate=it}
        NumberField("Цена мешка, ₽/шт",demolitionBagPrice){demolitionBagPrice=it}
        NumberField("Вывоз, ₽/т",demolitionHaulPrice){demolitionHaulPrice=it}
        val isWall=demolitionSurface=="Стена"
        val regionValid=GeometryEngine.polygonContainedBy(current.vertices,demolitionRegion)
        val area=if(isWall)demolitionEffectiveLength*(demolitionHeight.toDoubleOrNull()?:0.0) else if(regionValid)GeometryEngine.area(demolitionRegion) else 0.0
        val result=runCatching{DemolitionCalculator.calculate(DemolitionInput(if(isWall)demolitionEffectiveLength else area,if(isWall)(demolitionHeight.toDoubleOrNull()?:0.0) else 1.0,demolitionThickness.toDouble(),demolitionDensity.toDouble(),demolitionBulking.toDouble(),bagLitres.toDouble(),bagMaxKg.toDouble(),bagFill.toDouble()))}.getOrNull()
        if(result!=null&&area>0){
          Text("Площадь демонтажа: ${df.format(result.areaM2)} м²")
          Text("Объём материала: ${df.format(result.solidVolumeM3)} м³ • масса: ${df.format(result.solidMassKg)} кг")
          Text("После разрыхления: ${df.format(result.looseVolumeM3)} м³")
          Text("Мешки: ${result.bagsRequired} шт. (по объёму ${result.bagsByVolume}, по весу ${result.bagsByWeight})",color=accent,fontWeight=FontWeight.Bold)
          Button(onClick={
            val surfaceName=when(demolitionSurface){"Пол"->"пола";"Потолок"->"потолка";else->"стены"}
            val workName=when(demolitionMaterial){"Керамическая плитка"->"Демонтаж плитки";"Гипсовая штукатурка","Цементная штукатурка"->"Демонтаж старой штукатурки";else->"Демонтаж $surfaceName: $demolitionMaterial"}
            val wallIndex=if(isWall)selectedDemolitionWallIndex else -1
            val startPoint=if(isWall)GeometryEngine.pointOnEdge(current.vertices,wallIndex,demolitionStartOffset)?:current.vertices[wallIndex] else Vec2(0.0,0.0)
            val endPoint=if(isWall)GeometryEngine.pointOnEdge(current.vertices,wallIndex,demolitionStartOffset+demolitionEffectiveLength)?:startPoint else Vec2(0.0,0.0)
            constructionRecords=constructionRecords+ConstructionRecord(
              id=java.util.UUID.randomUUID().toString(),kind=if(isWall)"DEMOLITION" else if(demolitionSurface=="Пол")"DEMOLITION_FLOOR" else "DEMOLITION_CEILING",
              roomIndex=selected,material=demolitionMaterial,wallIndex=wallIndex,startX=startPoint.x,startY=startPoint.y,endX=endPoint.x,endY=endPoint.y,
              lengthM=(if(isWall) demolitionEffectiveLength else area),heightM=(if(isWall) (demolitionHeight.toDoubleOrNull()?:0.0) else 1.0),
              thicknessMm=demolitionThickness.toDouble(),densityKgM3=demolitionDensity.toDouble(),quantity=result.areaM2,
              regionPoints=if(isWall)emptyList() else demolitionRegion
            )
            lines=lines+EstimateLine(workName,"м²",demolitionRate.toDoubleOrNull()?:0.0,result.areaM2,"Демонтаж")+
              EstimateLine("Упаковка мусора ($demolitionMaterial)","меш",demolitionBagPrice.toDoubleOrNull()?:0.0,result.bagsRequired.toDouble(),"Демонтаж")+
              EstimateLine("Вывоз строительного мусора","т",demolitionHaulPrice.toDoubleOrNull()?:0.0,result.solidMassKg/1000.0,"Вывоз")
            saveMessage="Зона демонтажа добавлена в проект и смету. Автосохранение включено."
            tab="Смета"
          },modifier=Modifier.fillMaxWidth()){Text("Добавить демонтаж в смету")}
        }else Text("Укажи корректную площадь, толщину, плотность и параметры мешков.",color=Color(0xFFFFB86B))
      } }
    }
    "Перегородки" -> LazyColumn(verticalArrangement=Arrangement.spacedBy(12.dp),contentPadding=PaddingValues(vertical=12.dp)){
      item { CardBlock {
        Text("Новая перегородка",fontSize=19.sp,fontWeight=FontWeight.Bold)
        Text("Отметь на плане начало и конец новой перегородки двумя касаниями. Длина и расчёт материалов берутся строго по фактическому отрезку на плане.")
        PartitionPlacementCanvas(current,partitionStart,partitionEnd,Modifier.fillMaxWidth().height(230.dp)){ point ->
          if(partitionStart==null || partitionEnd!=null){partitionStart=point;partitionEnd=null}
          else {partitionEnd=point;partitionLength=df.format(partitionStart!!.distanceTo(point)).replace(',','.')}
        }
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp),verticalAlignment=Alignment.CenterVertically){
          Text(if(partitionStart==null) "Точка начала не выбрана" else if(partitionEnd==null) "Выбери точку окончания" else "Фактическая длина по плану: ${df.format(partitionMeasuredLength)} м",color=accent,modifier=Modifier.weight(1f))
          TextButton(onClick={partitionStart=null;partitionEnd=null}){Text("Сбросить")}
        }
        val splitPreview=if(partitionStart!=null && partitionEnd!=null) GeometryEngine.splitByBoundaryChord(current.vertices,partitionStart!!,partitionEnd!!) else null
        if(partitionStart!=null && partitionEnd!=null) {
          if(splitPreview!=null) Text("Контур делится на ${df.format(GeometryEngine.area(splitPreview.first))} м² и ${df.format(GeometryEngine.area(splitPreview.second))} м²",color=accent)
          else Text("Невозможно разделить: обе точки должны лежать на границе, а линия проходить внутри помещения.",color=Color(0xFFFFB86B))
        }
        Text("Длина фиксируется по плану и не редактируется отдельно.",color=Color.LightGray,fontSize=12.sp)
        NumberField("Высота перегородки, м",partitionHeight){partitionHeight=it}
        NumberField("Площадь дверных/прочих проёмов, м²",partitionOpenings){partitionOpenings=it}
        NumberField("Запас материала, %",partitionReserve){partitionReserve=it}
        varType(partitionKind,{partitionKind=it;when(it){"Газобетон"->{partitionThickness="100";blockLength="600";blockHeight="250";blockDensity="600"};"ПГП"->{partitionThickness="80";blockLength="667";blockHeight="500";blockDensity="1100"};"Кирпич"->{partitionThickness="120";blockLength="250";blockHeight="65";blockDensity="1800"};else->partitionThickness="100"}},listOf("Газобетон","ПГП","Кирпич","ГКЛ"))
      } }
      item { CardBlock {
        if(partitionKind!="ГКЛ"){
          Text(if(partitionKind=="Газобетон") "Кладка из газобетона" else if(partitionKind=="ПГП") "Перегородка из пазогребневых плит" else "Кирпичная кладка",fontWeight=FontWeight.Bold)
          varType(partitionThickness,{partitionThickness=it},when(partitionKind){"Газобетон"->listOf("75","100","150","200");"ПГП"->listOf("80","100");else->listOf("120","250")})
          Text("Толщина блока: $partitionThickness мм")
          NumberField("Длина блока, мм",blockLength){blockLength=it}
          NumberField("Высота блока, мм",blockHeight){blockHeight=it}
          NumberField("Плотность блока, кг/м³",blockDensity){blockDensity=it}
          NumberField("Цена блока, ₽/шт",blockUnitPrice){blockUnitPrice=it}
          NumberField(if(partitionKind=="Кирпич") "Цена раствора, ₽/кг" else "Цена клея, ₽/кг",adhesiveKgPrice){adhesiveKgPrice=it}
          Text("В расчёт входят блоки, масса кладки и ориентировочный расход клея. Армирование и перемычки будут отдельными опциями.")
        } else {
          Text("Каркасная перегородка ГКЛ",fontWeight=FontWeight.Bold)
          varType(partitionThickness,{partitionThickness=it},listOf("50","75","100","150"))
          Text("Толщина каркаса: $partitionThickness мм")
          varType(boardLayers,{boardLayers=it},listOf("1","2"))
          Text("Слоёв ГКЛ на каждой стороне: $boardLayers")
          varType(boardSides,{boardSides=it},listOf("1","2"))
          Text("Обшиваемых сторон: $boardSides")
          varType(studSpacing,{studSpacing=it},listOf("400","600"))
          Text("Шаг стоек: $studSpacing мм")
          Row(verticalAlignment=Alignment.CenterVertically){Checkbox(checked=insulation,onCheckedChange={insulation=it});Text("Шумоизоляция внутри каркаса")}
          NumberField("Цена листа ГКЛ, ₽/шт",boardSheetPrice){boardSheetPrice=it}
          NumberField("Цена профиля, ₽/м.п.",profileMeterPrice){profileMeterPrice=it}
          if(insulation) NumberField("Цена шумоизоляции, ₽/м³",insulationM3Price){insulationM3Price=it}
        }
        NumberField("Цена монтажа, ₽/м² (задай свою)",partitionRate){partitionRate=it}
      } }
      item { CardBlock {
        val result=runCatching {
          if(partitionKind!="ГКЛ") PartitionCalculator.calculate(PartitionInput(when(partitionKind){"Газобетон"->PartitionKind.AAC_BLOCK;"ПГП"->PartitionKind.PGP;else->PartitionKind.BRICK},partitionMeasuredLength,partitionHeight.toDouble(),partitionThickness.toInt(),partitionOpenings.toDouble(),partitionReserve.toDouble(),blockLength.toInt(),blockHeight.toInt(),blockDensity.toDouble()))
          else PartitionCalculator.calculate(PartitionInput(PartitionKind.GYPSUM_BOARD,partitionMeasuredLength,partitionHeight.toDouble(),partitionThickness.toInt(),partitionOpenings.toDouble(),partitionReserve.toDouble(),boardLayersPerSide=boardLayers.toInt(),boardSides=boardSides.toInt(),studSpacingMm=studSpacing.toInt(),insulation=insulation))
        }.getOrNull()
        Text("Расчёт материалов и работ",fontWeight=FontWeight.Bold)
        if(result!=null){
          Text("Площадь до вычета проёмов: ${df.format(result.grossAreaM2)} м²")
          Text("Чистая площадь: ${df.format(result.netAreaM2)} м²")
          if(partitionKind!="ГКЛ"){
            Text("Объём кладки: ${df.format(result.wallVolumeM3)} м³")
            Text("${if(partitionKind=="Газобетон") "Блоков" else if(partitionKind=="ПГП") "Пазогребневых плит" else "Кирпичей"} с запасом: ${result.blockCount} шт")
            Text("Ориентировочная масса блоков: ${df.format(result.blockMassKg)} кг")
            Text("${if(partitionKind=="Кирпич") "Раствор" else "Клей"}: ${df.format(result.adhesiveKg)} кг")
          } else {
            Text("Листов ГКЛ с запасом: ${result.boardSheets} шт")
            Text("Стойки с запасом: ${result.studs} шт • шаг $studSpacing мм")
            Text("Направляющие: ${df.format(result.trackLengthM)} м.п. • хлыстов по 3 м: ${result.trackPieces}")
            Text("Общая длина профиля с запасом: ${df.format(result.profileTotalLengthM)} м")
            if(insulation) Text("Шумоизоляция: ${df.format(result.insulationVolumeM3)} м³ • масса ${df.format(result.insulationMassKg)} кг")
          }
          Button(onClick={
            val rate=partitionRate.toDoubleOrNull()?:0.0
            val p1=partitionStart
            val p2=partitionEnd
            val partitionRoomIndex=selected
            val rawSplit=if(p1!=null && p2!=null) GeometryEngine.splitByBoundaryChord(current.vertices,p1,p2) else null
            fun cutPassesThroughOpening(point:Vec2):Boolean = current.vertices.indices.any { edge ->
              val a=current.vertices[edge];val b=current.vertices[(edge+1)%current.vertices.size];val d=b-a
              val len2=d.x*d.x+d.y*d.y
              if(len2<1e-12) false else {
                val t=((point.x-a.x)*d.x+(point.y-a.y)*d.y)/len2
                val projected=a+d*t.coerceIn(0.0,1.0)
                val onEdge=t in 0.0..1.0 && projected.distanceTo(point)<1e-6
                val distance=t.coerceIn(0.0,1.0)*a.distanceTo(b)
                onEdge && current.openingsByWall[edge].orEmpty().any { distance>it.offsetFromStart+1e-7 && distance<it.offsetFromStart+it.width-1e-7 }
              }
            }
            val cutsOpening=(p1!=null&&cutPassesThroughOpening(p1))||(p2!=null&&cutPassesThroughOpening(p2))
            val split=if(cutsOpening) null else rawSplit
            if(p1!=null && p2!=null && split!=null){
              fun roomFromPolygon(id:Long,name:String,points:List<Vec2>):Room {
                val w=(points.maxOf{it.x}-points.minOf{it.x}).coerceAtLeast(0.01)
                val h=(points.maxOf{it.y}-points.minOf{it.y}).coerceAtLeast(0.01)
                val original=current.vertices
                val sourceGeometry=RoomGeometry(
                  current.name,original,current.wallHeights,current.wallThicknesses,
                  current.openingsByWall,current.wallHeightOverrides,
                  wallHeightEndOverrides=current.wallHeightEndOverrides
                )
                val originalHeights=if(current.wallHeights.size==original.size) current.wallHeights else List(original.size){current.height}
                val originalThicknesses=if(current.wallThicknesses.size==original.size) current.wallThicknesses else List(original.size){0.0}
                fun projection(a:Vec2,b:Vec2,p:Vec2):Double? {
                  val d=b-a;val len2=d.x*d.x+d.y*d.y
                  if(len2<1e-12)return null
                  val t=((p.x-a.x)*d.x+(p.y-a.y)*d.y)/len2
                  if(t < -1e-7 || t > 1.0000001)return null
                  val q=a+d*t.coerceIn(0.0,1.0)
                  return if(q.distanceTo(p)<1e-6)t.coerceIn(0.0,1.0) else null
                }
                fun sourceEdge(a:Vec2,b:Vec2):Int? = original.indices.firstOrNull { i ->
                  projection(original[i],original[(i+1)%original.size],a)!=null &&
                  projection(original[i],original[(i+1)%original.size],b)!=null
                }
                val heights=points.map { point ->
                  val edge=original.indices.firstOrNull { projection(original[it],original[(it+1)%original.size],point)!=null }
                  if(edge==null) current.height else {
                    val t=projection(original[edge],original[(edge+1)%original.size],point)!!
                    GeometryEngine.wallHeightAt(sourceGeometry,edge,t,current.height)
                  }
                }
                val thicknesses=points.indices.map { i ->
                  val edge=sourceEdge(points[i],points[(i+1)%points.size])
                  if(edge==null) partitionThickness.toDouble()/1000.0 else originalThicknesses[edge]
                }
                val wallStartHeights=points.indices.map { i ->
                  val edge=sourceEdge(points[i],points[(i+1)%points.size])
                  if(edge==null) partitionHeight.toDoubleOrNull()?:current.height else {
                    val t=projection(original[edge],original[(edge+1)%original.size],points[i])!!
                    GeometryEngine.wallHeightAt(sourceGeometry,edge,t,current.height)
                  }
                }
                val wallEndHeights=points.indices.map { i ->
                  val edge=sourceEdge(points[i],points[(i+1)%points.size])
                  if(edge==null) partitionHeight.toDoubleOrNull()?:current.height else {
                    val t=projection(original[edge],original[(edge+1)%original.size],points[(i+1)%points.size])!!
                    GeometryEngine.wallHeightAt(sourceGeometry,edge,t,current.height)
                  }
                }
                val openings=mutableMapOf<Int,List<Opening>>()
                points.indices.forEach { newIndex ->
                  val source=sourceEdge(points[newIndex],points[(newIndex+1)%points.size]) ?: return@forEach
                  val a=original[source];val b=original[(source+1)%original.size]
                  val newStart=projection(a,b,points[newIndex])!!*a.distanceTo(b)
                  val newEnd=projection(a,b,points[(newIndex+1)%points.size])!!*a.distanceTo(b)
                  val sameDirection=newEnd>=newStart
                  val segmentMin=minOf(newStart,newEnd);val segmentMax=maxOf(newStart,newEnd)
                  val mapped=current.openingsByWall[source].orEmpty().mapNotNull { opening ->
                    val openingStart=opening.offsetFromStart
                    val openingEnd=openingStart+opening.width
                    if(openingStart<segmentMin-1e-7 || openingEnd>segmentMax+1e-7) null
                    else opening.copy(offsetFromStart=if(sameDirection) openingStart-newStart else newStart-openingEnd)
                  }
                  if(mapped.isNotEmpty()) openings[newIndex]=mapped
                }
                return Room(id,name,w,h,current.height,w,h,w,h,points,heights,thicknesses,openings,wallStartHeights,emptyList(),wallEndHeights)
              }
              rooms=rooms.toMutableList().also { list ->
                list[selected]=roomFromPolygon(current.id,current.name,split.first)
                list.add(selected+1,roomFromPolygon(System.currentTimeMillis(),"${current.name} — 2",split.second))
              }
              // Keep point-to-room references valid after inserting the second half.
              engineeringPoints=engineeringPoints.map { point ->
                when {
                  point.roomIndex>partitionRoomIndex -> point.copy(roomIndex=point.roomIndex+1)
                  point.roomIndex==partitionRoomIndex &&
                    GeometryEngine.containsPoint(split.second,Vec2(point.x,point.y)) ->
                    point.copy(roomIndex=partitionRoomIndex+1)
                  else -> point
                }
              }
              constructionRecords=constructionRecords.map { record ->
                if(record.roomIndex>partitionRoomIndex) record.copy(roomIndex=record.roomIndex+1) else record
              }
              selected=selected+1
              constructionRecords=constructionRecords+ConstructionRecord(
                id=java.util.UUID.randomUUID().toString(),kind="PARTITION",roomIndex=partitionRoomIndex,
                material=partitionKind,wallIndex=-1,startX=p1.x,startY=p1.y,endX=p2.x,endY=p2.y,
                lengthM=partitionMeasuredLength,heightM=partitionHeight.toDouble(),thicknessMm=partitionThickness.toDouble(),
                openingsAreaM2=partitionOpenings.toDouble(),reservePercent=partitionReserve.toDouble(),
                layersPerSide=boardLayers.toIntOrNull()?:1,sides=boardSides.toIntOrNull()?:2,
                studSpacingMm=studSpacing.toIntOrNull()?:600,insulation=insulation,
                densityKgM3=blockDensity.toDoubleOrNull()?:0.0,quantity=result.netAreaM2
              )
              saveMessage="Помещение разделено на две области. Нажми «Сохранить» для записи."
            } else {
              saveMessage=if(cutsOpening) "Разделение отменено: точка попала внутрь существующего проёма. Перенеси точку разреза на участок стены вне проёма." else "Разделение не выполнено: выбери две точки на границе помещения."
              return@Button
            }
            val rows=mutableListOf<EstimateLine>()
            rows.add(EstimateLine(if(partitionKind=="ГКЛ") "Монтаж каркасной перегородки ГКЛ $partitionThickness мм" else "Монтаж перегородки: $partitionKind $partitionThickness мм","м²",rate,result.netAreaM2,"Перегородки"))
            if(partitionKind!="ГКЛ"){
              val materialName=when(partitionKind){"Газобетон"->"Газобетонные блоки";"ПГП"->"Пазогребневые плиты";else->"Кирпич"}
              rows.add(EstimateLine("$materialName $partitionThickness мм","шт",blockUnitPrice.toDoubleOrNull()?:0.0,result.blockCount.toDouble(),"Материалы",true))
              rows.add(EstimateLine(if(partitionKind=="Кирпич") "Кладочный раствор" else "Клей для $partitionKind","кг",adhesiveKgPrice.toDoubleOrNull()?:0.0,result.adhesiveKg,"Материалы",true))
            } else {
              rows.add(EstimateLine("ГКЛ ($boardLayers слоя, $boardSides сторон)","лист",boardSheetPrice.toDoubleOrNull()?:0.0,result.boardSheets.toDouble(),"Материалы",true))
              rows.add(EstimateLine("Профиль каркаса, стойки + направляющие","м.п.",profileMeterPrice.toDoubleOrNull()?:0.0,result.profileTotalLengthM,"Материалы",true))
              if(insulation) rows.add(EstimateLine("Шумоизоляционное заполнение","м³",insulationM3Price.toDoubleOrNull()?:0.0,result.insulationVolumeM3,"Материалы",true))
            }
            lines=lines+rows
            tab="Смета"
          },modifier=Modifier.fillMaxWidth()){Text("Добавить перегородку и материалы в смету")}
          Text("Расчётные количества ориентировочные: проёмы, раскладка листов, усиления и правила производителя могут изменить фактический расход.")
        } else Text("Проверь введённые размеры и параметры конструкции.",color=Color(0xFFFFB86B))
      } }
    }
  }
 }
 if(showPdfOptions) AlertDialog(
  onDismissRequest={showPdfOptions=false},
  title={Text("Экспорт PDF")},
  text={Column(Modifier.heightIn(max=430.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(8.dp)){
   Text("Реквизиты необязательны. Заполненные поля сохраняются для следующих экспортов.")
   OutlinedTextField(pdfCustomer,{pdfCustomer=it},label={Text("Заказчик")},singleLine=true,modifier=Modifier.fillMaxWidth())
   OutlinedTextField(pdfExecutor,{pdfExecutor=it},label={Text("Исполнитель")},singleLine=true,modifier=Modifier.fillMaxWidth())
   OutlinedTextField(pdfContact,{pdfContact=it},label={Text("Телефон / контакты")},singleLine=true,modifier=Modifier.fillMaxWidth())
   OutlinedTextField(pdfAddress,{pdfAddress=it},label={Text("Адрес объекта")},singleLine=true,modifier=Modifier.fillMaxWidth())
   Text("Выбери формат документа. Используются сохранённые данные текущего проекта.")
   OutlinedButton(onClick={
    prefs.edit().putString("pdf_customer",pdfCustomer).putString("pdf_executor",pdfExecutor).putString("pdf_contact",pdfContact).putString("pdf_address",pdfAddress).apply()
    val data=PdfReportData(project,rooms,lines,constructionRecords,engineeringPoints,customerName=pdfCustomer.trim(),executorName=pdfExecutor.trim(),executorContact=pdfContact.trim(),objectAddress=pdfAddress.trim())
    runCatching{PdfReportRenderer.render(data,PdfReportMode.TECHNICAL)}.onSuccess{pendingPdfBytes=it;showPdfOptions=false;pdfSaveLauncher.launch("PLAN-MASTER-3D-Технический-отчёт.pdf")}.onFailure{android.widget.Toast.makeText(ctx,"Ошибка PDF: " + (it.message ?: "ошибка"),android.widget.Toast.LENGTH_LONG).show()}
   },modifier=Modifier.fillMaxWidth()){Text("Технический отчёт")}
   OutlinedButton(onClick={
    prefs.edit().putString("pdf_customer",pdfCustomer).putString("pdf_executor",pdfExecutor).putString("pdf_contact",pdfContact).putString("pdf_address",pdfAddress).apply()
    val data=PdfReportData(project,rooms,lines,constructionRecords,engineeringPoints,customerName=pdfCustomer.trim(),executorName=pdfExecutor.trim(),executorContact=pdfContact.trim(),objectAddress=pdfAddress.trim())
    runCatching{PdfReportRenderer.render(data,PdfReportMode.PROPOSAL)}.onSuccess{pendingPdfBytes=it;showPdfOptions=false;pdfSaveLauncher.launch("PLAN-MASTER-3D-Коммерческое-предложение.pdf")}.onFailure{android.widget.Toast.makeText(ctx,"Ошибка PDF: " + (it.message ?: "ошибка"),android.widget.Toast.LENGTH_LONG).show()}
   },modifier=Modifier.fillMaxWidth()){Text("Коммерческое предложение")}
  }},
  confirmButton={TextButton(onClick={showPdfOptions=false}){Text("Закрыть")}}
 )
 if(showAddRoom) AlertDialog(onDismissRequest={showAddRoom=false},title={Text("Новое помещение")},text={Column { OutlinedTextField(roomName,{roomName=it},label={Text("Название")}); OutlinedTextField(roomW,{roomW=it},label={Text("Ширина, м")}); OutlinedTextField(roomL,{roomL=it},label={Text("Длина, м")}); OutlinedTextField(roomH,{roomH=it},label={Text("Высота, м")}) }},confirmButton={TextButton(onClick={val w=roomW.toDoubleOrNull()?:4.0;val l=roomL.toDoubleOrNull()?:5.0;rooms=rooms+Room(System.currentTimeMillis(),roomName,w,l,roomH.toDoubleOrNull()?:2.7,w,l,w,l);selected=rooms.lastIndex;showAddRoom=false}){Text("Добавить")}},dismissButton={TextButton(onClick={showAddRoom=false}){Text("Отмена")}})
 if(showProjects) AlertDialog(onDismissRequest={showProjects=false},title={Text("Сохранённые проекты")},text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){if(projectIds.isEmpty()) Text("Пока нет сохранённых проектов");projectIds.forEach { id -> val item=projectStore.loadProject(id);if(item!=null) TextButton(onClick={openProject(id)}){Text("${item.name} • ${item.rooms.size} помещений")}}}},confirmButton={TextButton(onClick={showProjects=false}){Text("Закрыть")}})
}
}
@Composable fun CardBlock(content:@Composable ColumnScope.()->Unit){Card(Modifier.fillMaxWidth(),colors=CardDefaults.cardColors(containerColor=panel),shape=RoundedCornerShape(18.dp)){Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(8.dp),content=content)}}
@Composable fun Metric(name:String,value:Double,unit:String,modifier:Modifier=Modifier){Card(modifier,colors=CardDefaults.cardColors(containerColor=panel)){Column(Modifier.padding(10.dp)){Text(name,fontSize=11.sp,color=Color.LightGray);Text(df.format(value),fontWeight=FontWeight.Bold,fontSize=17.sp);Text(unit,fontSize=11.sp,color=Color.LightGray)}}}
@Composable fun NumberField(label:String,value:String,onChange:(String)->Unit){OutlinedTextField(value,onChange,label={Text(label)},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),modifier=Modifier.fillMaxWidth(),singleLine=true)}
@Composable fun varType(value:String,onChange:(String)->Unit,options:List<String>){Row(horizontalArrangement=Arrangement.spacedBy(5.dp)){options.forEach{FilterChip(selected=value==it,onClick={onChange(it)},label={Text(it,fontSize=11.sp)})}}}
@Composable fun RoomPlan(room:Room,modifier:Modifier=Modifier){
 var zoom by remember(room.id){mutableFloatStateOf(1f)}
 var pan by remember(room.id){mutableStateOf(Offset.Zero)}
 val cameraZoom=rememberUpdatedState(zoom)
 Canvas(modifier
  .pointerInput(room.id){detectTransformGestures{centroid,gesturePan,gestureZoom,_->
   val next=(cameraZoom.value*gestureZoom).coerceIn(0.55f,5f)
   pan=centroid-(centroid-pan)*(next/cameraZoom.value)+gesturePan
   zoom=next
  }}
  .pointerInput(room.id){detectTapGestures(onDoubleTap={zoom=(zoom*1.5f).coerceIn(0.55f,5f)})}
 ){
 val minX=room.vertices.minOf{it.x};val maxX=room.vertices.maxOf{it.x};val minY=room.vertices.minOf{it.y};val maxY=room.vertices.maxOf{it.y}
 val bw=(maxX-minX).coerceAtLeast(0.01).toFloat();val bh=(maxY-minY).coerceAtLeast(0.01).toFloat()
 val baseScale=min(size.width/(bw+1f),size.height/(bh+1f));val scale=baseScale*zoom
 val w=bw*scale;val h=bh*scale;val left=(size.width-w)/2;val top=(size.height-h)/2
 fun map(p:Vec2)=Offset(left+(p.x-minX).toFloat()*scale+pan.x,top+(p.y-minY).toFloat()*scale+pan.y)
 val path=Path();room.vertices.forEachIndexed{i,p->val pt=map(p);if(i==0)path.moveTo(pt.x,pt.y)else path.lineTo(pt.x,pt.y)};path.close()
 drawPath(path,Color(0xFF6ED4E5),style=Stroke(5f))
 room.openingsByWall.forEach { (wall,openings) ->
  if(wall in room.vertices.indices) {
   val a=room.vertices[wall];val b=room.vertices[(wall+1)%room.vertices.size];val len=a.distanceTo(b)
   openings.forEach { opening ->
    val from=GeometryEngine.pointOnEdge(room.vertices,wall,opening.offsetFromStart)
    val to=GeometryEngine.pointOnEdge(room.vertices,wall,opening.offsetFromStart+opening.width)
    if(from!=null&&to!=null&&opening.offsetFromStart+opening.width<=len+1e-7) {
     val color=if(opening.kind=="Окно")Color(0xFF80D8FF) else Color(0xFFFFB454)
     drawLine(color,map(from),map(to),strokeWidth=9f)
     drawCircle(color,4f,map(from));drawCircle(color,4f,map(to))
    }
   }
  }
 }
}}
@Composable fun RoomModel3D(
 room:Room,
 angle:Float,
 zoom:Float,
 hiddenWalls:Set<Int>,
 pan:Offset=Offset.Zero,
 onCameraGesture:(Offset,Float,Float)->Unit={_,_,_->},
 modifier:Modifier=Modifier
){
 val currentCameraGesture=rememberUpdatedState(onCameraGesture)
 Canvas(modifier.background(Color(0xFF111C2B)).pointerInput(room.vertices){
  detectTransformGestures { _, gesturePan, gestureZoom, gestureRotation ->
   currentCameraGesture.value(gesturePan,gestureZoom,gestureRotation)
  }
 }){
  if(room.vertices.size<3)return@Canvas
  val radians=Math.toRadians(angle.toDouble())
  val cosA=cos(radians);val sinA=sin(radians)
  val raw=mutableListOf<Pair<Double,Double>>()
  room.vertices.forEachIndexed{i,p->
   val x=p.x*cosA-p.y*sinA;val y=p.x*sinA+p.y*cosA
   raw.add((x-y)*0.82 to (x+y)*0.42)
   val next=(i+1)%room.vertices.size
   val h0=room.wallHeightOverrides.getOrNull(i)?:room.wallHeights.getOrNull(i)?:room.height
   val h1=room.wallHeightEndOverrides.getOrNull(i)?:room.wallHeightOverrides.getOrNull(i)?:room.wallHeights.getOrNull(next)?:room.height
   raw.add((x-y)*0.82 to ((x+y)*0.42-h0*0.95))
   val q=room.vertices[next]
   val qx=q.x*cosA-q.y*sinA;val qy=q.x*sinA+q.y*cosA
   raw.add((qx-qy)*0.82 to ((qx+qy)*0.42-h1*0.95))
  }
  val minX=raw.minOf{it.first};val maxX=raw.maxOf{it.first}
  val minY=raw.minOf{it.second};val maxY=raw.maxOf{it.second}
  val spanX=(maxX-minX).coerceAtLeast(0.1);val spanY=(maxY-minY).coerceAtLeast(0.1)
  val scale=(min(size.width/spanX.toFloat(),size.height/spanY.toFloat())*0.78f*zoom).coerceAtMost(220f)
  val cx=size.width/2f;val cy=size.height/2f
  fun project(p:Vec2,z:Double=0.0):Offset{
   val x=p.x*cosA-p.y*sinA;val y=p.x*sinA+p.y*cosA
   val px=(x-y)*0.82;val py=(x+y)*0.42-z*0.95
   return Offset(cx+pan.x+((px-(minX+maxX)/2)*scale).toFloat(),cy+pan.y+((py-(minY+maxY)/2)*scale).toFloat())
  }
  val floor=Path()
  room.vertices.forEachIndexed{i,p->val q=project(p);if(i==0)floor.moveTo(q.x,q.y)else floor.lineTo(q.x,q.y)}
  floor.close()
  drawPath(floor,Color(0xFF263B50))
  val order=room.vertices.indices.sortedByDescending{i->
   val a=room.vertices[i];val b=room.vertices[(i+1)%room.vertices.size]
   val mid=(a+b)*0.5
   mid.x*sinA+mid.y*cosA
  }
  order.forEach{i->
   if(i in hiddenWalls) return@forEach
   val j=(i+1)%room.vertices.size
   val a=room.vertices[i];val b=room.vertices[j]
   val length=a.distanceTo(b)
   if(length<=1e-9)return@forEach
   val h0=room.wallHeightOverrides.getOrNull(i)?:room.wallHeights.getOrNull(i)?:room.height
   val h1=room.wallHeightEndOverrides.getOrNull(i)?:room.wallHeightOverrides.getOrNull(i)?:room.wallHeights.getOrNull(j)?:room.height
   val openings=room.openingsByWall[i].orEmpty().filter{
    it.offsetFromStart>=0.0&&it.offsetFromStart+it.width<=length+1e-6&&
    it.sillHeight+it.height<=min(h0,h1)+1e-6
   }
   fun wallTop(distance:Double):Double=h0+(h1-h0)*(distance/length).coerceIn(0.0,1.0)
   fun wallPoint(distance:Double,z:Double):Offset{
    val t=(distance/length).coerceIn(0.0,1.0)
    return project(a+(b-a)*t,z)
   }
   val xs=(listOf(0.0,length)+openings.flatMap{listOf(it.offsetFromStart,it.offsetFromStart+it.width)}).distinct().sorted()
   val ys=(listOf(0.0,max(h0,h1))+openings.flatMap{listOf(it.sillHeight,it.sillHeight+it.height)}).distinct().sorted()
   for(xi in 0 until xs.lastIndex) {
    val x0=xs[xi];val x1=xs[xi+1]
    val top0=wallTop(x0);val top1=wallTop(x1)
    for(yi in 0 until ys.lastIndex) {
     val z0=ys[yi]
     if(z0>=max(top0,top1)-1e-7)continue
     val z1=min(ys[yi+1],max(top0,top1))
     if(z1-z0<=1e-7)continue
     val mx=(x0+x1)/2.0;val mz=(z0+z1)/2.0
     val isOpening=openings.any{mx>it.offsetFromStart-1e-7&&mx<it.offsetFromStart+it.width+1e-7&&mz>it.sillHeight-1e-7&&mz<it.sillHeight+it.height+1e-7}
     if(!isOpening){
      val p1=wallPoint(x0,z0);val p2=wallPoint(x1,z0)
      val p3=wallPoint(x1,min(z1,top1));val p4=wallPoint(x0,min(z1,top0))
      val panelPath=Path().apply{moveTo(p1.x,p1.y);lineTo(p2.x,p2.y);lineTo(p3.x,p3.y);lineTo(p4.x,p4.y);close()}
      val tint=if(i%2==0)Color(0xFF6C879C) else Color(0xFF526D84)
      drawPath(panelPath,tint)
      drawPath(panelPath,Color(0xFFB7D0DE),style=Stroke(1.5f))
     }
    }
   }
   val p1=project(a);val p2=project(b);val p3=project(b,h1);val p4=project(a,h0)
   val outline=Path().apply{moveTo(p1.x,p1.y);lineTo(p2.x,p2.y);lineTo(p3.x,p3.y);lineTo(p4.x,p4.y);close()}
   drawPath(outline,Color(0xFFB7D0DE),style=Stroke(2f))
   openings.forEach{opening->
    val left=opening.offsetFromStart;val right=left+opening.width
    val bottom=opening.sillHeight;val top=bottom+opening.height
    val c=if(opening.kind.contains("окн",true))Color(0xFF80D8FF) else Color(0xFFFFB454)
    val q1=wallPoint(left,bottom);val q2=wallPoint(right,bottom);val q3=wallPoint(right,top);val q4=wallPoint(left,top)
    drawLine(c,q1,q2,3f);drawLine(c,q2,q3,3f);drawLine(c,q3,q4,3f);drawLine(c,q4,q1,3f)
   }
  }
  drawPath(floor,Color(0xFF48C7D9),style=Stroke(3f))
 }
}
@Composable fun EditableRoomPlan(
 room:Room,
 selectedVertex:Int,
 onSelectedVertex:(Int)->Unit,
 onCommit:(List<Vec2>)->Unit,
 modifier:Modifier=Modifier
){
 var draft by remember(room.id,room.vertices){ mutableStateOf(room.vertices) }
 var draggingIndex by remember(room.id,room.vertices){ mutableIntStateOf(-1) }
 var planZoom by remember(room.id){mutableFloatStateOf(1f)}
 var planPan by remember(room.id){mutableStateOf(Offset.Zero)}
 val latestZoom=rememberUpdatedState(planZoom)
 val latestPan=rememberUpdatedState(planPan)
 val latestSelected=rememberUpdatedState(onSelectedVertex)
 val latestCommit=rememberUpdatedState(onCommit)
 Canvas(
  modifier
   .pointerInput(room.id){awaitEachGesture{
    awaitFirstDown(requireUnconsumed=false)
    var previousCentroid:Offset?=null
    var previousSpan=0f
    do {
     val event=awaitPointerEvent()
     val pressed=event.changes.filter{it.pressed}
     if(pressed.size>=2){
      val centroid=pressed.map{it.position}.reduce{a,b->a+b}/pressed.size.toFloat()
      val span=pressed.map{(it.position-centroid).getDistance()}.average().toFloat()
      val oldCentroid=previousCentroid
      if(oldCentroid!=null&&previousSpan>0f){
       val zoomChange=(span/previousSpan).coerceIn(0.5f,2f)
       val next=(latestZoom.value*zoomChange).coerceIn(0.55f,5f)
       planPan=centroid-(centroid-latestPan.value)*(next/latestZoom.value)+(centroid-oldCentroid)
       planZoom=next
       event.changes.forEach{if(it.pressed)it.consume()}
      }
      previousCentroid=centroid
      previousSpan=span
     }else{
      previousCentroid=null
      previousSpan=0f
     }
    }while(event.changes.any{it.pressed})
   }}
   .pointerInput(room.id,room.vertices){
   fun screenPoint(point:Offset):Vec2 {
    val minX=room.vertices.minOf{it.x};val maxX=room.vertices.maxOf{it.x}
    val minY=room.vertices.minOf{it.y};val maxY=room.vertices.maxOf{it.y}
    val bw=(maxX-minX).coerceAtLeast(0.01).toFloat()
    val bh=(maxY-minY).coerceAtLeast(0.01).toFloat()
    val scale=min(size.width/(bw+1f),size.height/(bh+1f))*latestZoom.value
    val left=(size.width-bw*scale)/2f+latestPan.value.x;val top=(size.height-bh*scale)/2f+latestPan.value.y
    return Vec2(minX+(point.x-left)/scale,minY+(point.y-top)/scale)
   }
   fun vertexScreen(point:Vec2):Offset {
    val minX=room.vertices.minOf{it.x};val maxX=room.vertices.maxOf{it.x}
    val minY=room.vertices.minOf{it.y};val maxY=room.vertices.maxOf{it.y}
    val bw=(maxX-minX).coerceAtLeast(0.01).toFloat()
    val bh=(maxY-minY).coerceAtLeast(0.01).toFloat()
    val scale=min(size.width/(bw+1f),size.height/(bh+1f))*latestZoom.value
    val left=(size.width-bw*scale)/2f+latestPan.value.x;val top=(size.height-bh*scale)/2f+latestPan.value.y
    return Offset(left+(point.x-minX).toFloat()*scale,top+(point.y-minY).toFloat()*scale)
   }
   detectDragGestures(
    onDragStart={position->
     val nearest=room.vertices.indices.map{index->index to (vertexScreen(room.vertices[index])-position).getDistance()}.minByOrNull{it.second}
     draggingIndex=if(nearest!=null&&nearest.second<=42f)nearest.first else -1
     if(draggingIndex>=0)latestSelected.value(draggingIndex)
    },
    onDragEnd={
     if(draggingIndex>=0) latestCommit.value(draft)
     draggingIndex=-1
    },
    onDragCancel={draggingIndex=-1;draft=room.vertices},
    onDrag={change,_->
     if(draggingIndex>=0){
      val point=screenPoint(change.position)
      draft=draft.toMutableList().also{it[draggingIndex]=point}
      change.consume()
     }
    }
   )
  }
 ){
  val minX=room.vertices.minOf{it.x};val maxX=room.vertices.maxOf{it.x}
  val minY=room.vertices.minOf{it.y};val maxY=room.vertices.maxOf{it.y}
  val bw=(maxX-minX).coerceAtLeast(0.01).toFloat()
  val bh=(maxY-minY).coerceAtLeast(0.01).toFloat()
  val scale=min(size.width/(bw+1f),size.height/(bh+1f))*latestZoom.value
  val left=(size.width-bw*scale)/2f+latestPan.value.x;val top=(size.height-bh*scale)/2f+latestPan.value.y
  fun map(point:Vec2)=Offset(left+(point.x-minX).toFloat()*scale,top+(point.y-minY).toFloat()*scale)
  val path=Path()
  draft.forEachIndexed{index,point->val p=map(point);if(index==0)path.moveTo(p.x,p.y)else path.lineTo(p.x,p.y)}
  path.close()
  drawPath(path,Color(0xFF263B50))
  drawPath(path,Color(0xFF6ED4E5),style=Stroke(4f))
  draft.forEachIndexed{index,point->
   val p=map(point)
   drawCircle(if(index==selectedVertex)Color(0xFFFFB454) else Color(0xFF48C7D9),if(index==selectedVertex)12f else 9f,p)
   drawCircle(Color.White,2.5f,p)
  }
 }
}

@Composable fun EngineeringPointCanvas(room:Room,points:List<EngineeringPoint>,modifier:Modifier=Modifier,onPlace:(Vec2)->Unit){
 Canvas(modifier.pointerInput(room.vertices){detectTapGestures { tap ->
  val minX=room.vertices.minOf{it.x};val maxX=room.vertices.maxOf{it.x};val minY=room.vertices.minOf{it.y};val maxY=room.vertices.maxOf{it.y}
  val bw=(maxX-minX).coerceAtLeast(0.01).toFloat();val bh=(maxY-minY).coerceAtLeast(0.01).toFloat()
  val scale=min(size.width/(bw+1f),size.height/(bh+1f));val left=(size.width-bw*scale)/2;val top=(size.height-bh*scale)/2
  val point=Vec2(minX+(tap.x-left)/scale,minY+(tap.y-top)/scale)
  if(GeometryEngine.containsPoint(room.vertices,point))onPlace(point)
 } }){
  val minX=room.vertices.minOf{it.x};val maxX=room.vertices.maxOf{it.x};val minY=room.vertices.minOf{it.y};val maxY=room.vertices.maxOf{it.y}
  val bw=(maxX-minX).coerceAtLeast(0.01).toFloat();val bh=(maxY-minY).coerceAtLeast(0.01).toFloat()
  val scale=min(size.width/(bw+1f),size.height/(bh+1f));val left=(size.width-bw*scale)/2;val top=(size.height-bh*scale)/2
  fun map(p:Vec2)=Offset(left+(p.x-minX).toFloat()*scale,top+(p.y-minY).toFloat()*scale)
  val boundary=Path();room.vertices.forEachIndexed{i,p->val q=map(p);if(i==0)boundary.moveTo(q.x,q.y)else boundary.lineTo(q.x,q.y)};boundary.close()
  drawPath(boundary,Color(0xFF6ED4E5),style=Stroke(4f))
  points.forEach{p->
   val color=when(p.kind){"Розетка","Выключатель","Электровывод","Интернет"->Color(0xFFFFB454);"Светильник","Вентилятор"->Color(0xFF80D8FF);else->Color(0xFF8BE28B)}
   drawCircle(color,8f,map(Vec2(p.x,p.y)))
   drawCircle(Color.White,8f,map(Vec2(p.x,p.y)),style=Stroke(2f))
  }
 }
}

@Composable fun SelectableRoomPlan(room:Room,selectedWall:Int,modifier:Modifier=Modifier,onSelect:(Int)->Unit){
 Canvas(modifier.pointerInput(room.vertices,selectedWall){detectTapGestures { tap ->
  val minX=room.vertices.minOf{it.x};val maxX=room.vertices.maxOf{it.x};val minY=room.vertices.minOf{it.y};val maxY=room.vertices.maxOf{it.y}
  val bw=(maxX-minX).coerceAtLeast(0.01).toFloat();val bh=(maxY-minY).coerceAtLeast(0.01).toFloat();val scale=min(size.width/(bw+1f),size.height/(bh+1f));val left=(size.width-bw*scale)/2;val top=(size.height-bh*scale)/2
  fun map(p:Vec2)=Offset(left+(p.x-minX).toFloat()*scale,top+(p.y-minY).toFloat()*scale)
  fun distance(p:Offset,s:Offset,e:Offset):Float { val dx=e.x-s.x;val dy=e.y-s.y;val len=dx*dx+dy*dy;val t=if(len==0f)0f else (((p.x-s.x)*dx+(p.y-s.y)*dy)/len).coerceIn(0f,1f);return hypot(p.x-(s.x+t*dx),p.y-(s.y+t*dy)) }
  val nearest=room.vertices.indices.map{i->i to distance(tap,map(room.vertices[i]),map(room.vertices[(i+1)%room.vertices.size]))}.minByOrNull{it.second}
  if(nearest!=null&&nearest.second<48f) onSelect(nearest.first)
 } }){
  val minX=room.vertices.minOf{it.x};val maxX=room.vertices.maxOf{it.x};val minY=room.vertices.minOf{it.y};val maxY=room.vertices.maxOf{it.y}
  val bw=(maxX-minX).coerceAtLeast(0.01).toFloat();val bh=(maxY-minY).coerceAtLeast(0.01).toFloat();val scale=min(size.width/(bw+1f),size.height/(bh+1f));val left=(size.width-bw*scale)/2;val top=(size.height-bh*scale)/2
  fun map(p:Vec2)=Offset(left+(p.x-minX).toFloat()*scale,top+(p.y-minY).toFloat()*scale)
  room.vertices.indices.forEach{i->drawLine(if(i==selectedWall)Color(0xFFFFB454) else Color(0xFF6ED4E5),map(room.vertices[i]),map(room.vertices[(i+1)%room.vertices.size]),if(i==selectedWall)10f else 5f)}
  drawCircle(Color(0xFFFFB454),7f,map(room.vertices.getOrElse(selectedWall){room.vertices.first()}))
 }
}
@Composable fun PartitionPlacementCanvas(room:Room,start:Vec2?,end:Vec2?,modifier:Modifier=Modifier,onPoint:(Vec2)->Unit){
 Canvas(modifier.pointerInput(room.width,room.length,start,end){detectTapGestures { tap ->
  val minX=room.vertices.minOf{it.x};val maxX=room.vertices.maxOf{it.x};val minY=room.vertices.minOf{it.y};val maxY=room.vertices.maxOf{it.y}
  val bw=(maxX-minX).coerceAtLeast(0.01).toFloat();val bh=(maxY-minY).coerceAtLeast(0.01).toFloat();val scale=min(size.width/(bw+1f),size.height/(bh+1f));val left=(size.width-bw*scale)/2;val top=(size.height-bh*scale)/2
  val raw=Vec2((minX+(tap.x-left)/scale).coerceIn(minX,maxX.toDouble()),(minY+(tap.y-top)/scale).coerceIn(minY,maxY.toDouble()))
  val snapped=room.vertices.indices.map { i ->
   val a=room.vertices[i];val b=room.vertices[(i+1)%room.vertices.size];val d=b-a
   val den=d.x*d.x+d.y*d.y
   val t=if(den<=1e-12) 0.0 else (((raw.x-a.x)*d.x+(raw.y-a.y)*d.y)/den).coerceIn(0.0,1.0)
   val q=a+d*t
   q to q.distanceTo(raw)
  }.minByOrNull{it.second}?.first ?: raw
  onPoint(snapped)
 } }){
  val minX=room.vertices.minOf{it.x};val maxX=room.vertices.maxOf{it.x};val minY=room.vertices.minOf{it.y};val maxY=room.vertices.maxOf{it.y}
  val bw=(maxX-minX).coerceAtLeast(0.01).toFloat();val bh=(maxY-minY).coerceAtLeast(0.01).toFloat();val scale=min(size.width/(bw+1f),size.height/(bh+1f));val left=(size.width-bw*scale)/2;val top=(size.height-bh*scale)/2
  val boundary=Path();room.vertices.forEachIndexed{i,p->val pt=Offset(left+(p.x-minX).toFloat()*scale,top+(p.y-minY).toFloat()*scale);if(i==0)boundary.moveTo(pt.x,pt.y)else boundary.lineTo(pt.x,pt.y)};boundary.close()
  drawPath(boundary,Color(0xFF6ED4E5),style=Stroke(4f))
  fun map(p:Vec2)=Offset(left+(p.x-minX).toFloat()*scale,top+(p.y-minY).toFloat()*scale)
  if(start!=null) drawCircle(Color(0xFFFFB454),9f,map(start))
  if(start!=null&&end!=null){drawLine(Color(0xFFFFB454),map(start),map(end),7f);drawCircle(Color(0xFFFFB454),9f,map(end))}
 }
}

@Composable fun RegionSelectionCanvas(room:Room,points:List<Vec2>,modifier:Modifier=Modifier,onPoint:(Vec2)->Unit){
 Canvas(modifier.pointerInput(room.vertices,points){detectTapGestures { tap ->
  val minX=room.vertices.minOf{it.x};val maxX=room.vertices.maxOf{it.x};val minY=room.vertices.minOf{it.y};val maxY=room.vertices.maxOf{it.y}
  val bw=(maxX-minX).coerceAtLeast(0.01).toFloat();val bh=(maxY-minY).coerceAtLeast(0.01).toFloat()
  val scale=min(size.width/(bw+1f),size.height/(bh+1f));val left=(size.width-bw*scale)/2;val top=(size.height-bh*scale)/2
  val point=Vec2(minX+(tap.x-left)/scale,minY+(tap.y-top)/scale)
  if(GeometryEngine.containsPoint(room.vertices,point))onPoint(point)
 } }){
  val minX=room.vertices.minOf{it.x};val maxX=room.vertices.maxOf{it.x};val minY=room.vertices.minOf{it.y};val maxY=room.vertices.maxOf{it.y}
  val bw=(maxX-minX).coerceAtLeast(0.01).toFloat();val bh=(maxY-minY).coerceAtLeast(0.01).toFloat()
  val scale=min(size.width/(bw+1f),size.height/(bh+1f));val left=(size.width-bw*scale)/2;val top=(size.height-bh*scale)/2
  fun map(p:Vec2)=Offset(left+(p.x-minX).toFloat()*scale,top+(p.y-minY).toFloat()*scale)
  val boundary=Path();room.vertices.forEachIndexed{i,p->val q=map(p);if(i==0)boundary.moveTo(q.x,q.y)else boundary.lineTo(q.x,q.y)};boundary.close()
  drawPath(boundary,Color(0xFF6ED4E5),style=Stroke(4f))
  if(points.isNotEmpty()){val region=Path();points.forEachIndexed{i,p->val q=map(p);if(i==0)region.moveTo(q.x,q.y)else region.lineTo(q.x,q.y)};if(points.size>=3)region.close();drawPath(region,Color(0xFFFFB454),style=Stroke(4f))}
  points.forEach{drawCircle(Color(0xFFFFB454),7f,map(it))}
 }
}
fun shareEstimate(ctx:Context,lines:List<EstimateLine>){val body=buildString{append("PLAN MASTER 3D — СМЕТА\n\n");lines.forEach{append("${it.name}: ${it.qty} ${it.unit} × ${it.price} = ${it.total} ₽\n")};append("\nИТОГО: ${lines.sumOf{it.total}} ₽")};val i=android.content.Intent(android.content.Intent.ACTION_SEND).setType("text/plain").putExtra(android.content.Intent.EXTRA_TEXT,body);ctx.startActivity(android.content.Intent.createChooser(i,"Отправить смету"))}
fun shareCsv(ctx:Context,lines:List<EstimateLine>){val body=buildString{append("Раздел;Наименование;Ед.;Количество;Цена;Сумма\n");lines.forEach{append("${it.category};${it.name};${it.unit};${it.qty};${it.price};${it.total}\n")}};val i=android.content.Intent(android.content.Intent.ACTION_SEND).setType("text/csv").putExtra(android.content.Intent.EXTRA_TEXT,body);ctx.startActivity(android.content.Intent.createChooser(i,"Экспорт таблицы"))}
