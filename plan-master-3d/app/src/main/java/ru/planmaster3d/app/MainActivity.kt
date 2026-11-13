package ru.planmaster3d.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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

data class Room(val id:Long,val name:String,val width:Double,val length:Double,val height:Double,val wall1:Double,val wall2:Double,val wall3:Double,val wall4:Double,val vertices:List<Vec2> = listOf(Vec2(0.0,0.0),Vec2(width,0.0),Vec2(width,length),Vec2(0.0,length)),val wallHeights:List<Double> = emptyList(),val wallThicknesses:List<Double> = emptyList(),val openingsByWall:Map<Int,List<Opening>> = emptyMap(),val wallHeightOverrides:List<Double> = emptyList(),val wallMeasurements:List<WallVectorMeasurement> = emptyList()) {
    val floor get() = GeometryEngine.area(vertices)
    val perimeter get() = GeometryEngine.perimeter(vertices)
    val geometry get() = GeometryEngine.metrics(RoomGeometry(name,vertices,wallHeights,wallThicknesses,openingsByWall,wallHeightOverrides),height)
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
  Room(i+1L,g.name,w,l,h,w,l,w,l,p,g.wallHeights,g.wallThicknesses,g.openingsByWall,g.wallHeightOverrides,g.measurements)
 }?.takeIf { it.isNotEmpty() } ?: listOf(Room(1,"Комната 1",4.0,5.0,2.7,4.0,5.0,4.0,5.0))
 var rooms by remember { mutableStateOf(toRoomModels(initialSaved)) }
 var selected by remember { mutableIntStateOf(0) }
 var lines by remember { mutableStateOf(initialSaved?.estimate ?: emptyList()) }
 var constructionRecords by remember { mutableStateOf(initialSaved?.construction ?: emptyList()) }
 var projectIds by remember { mutableStateOf(projectStore.listProjectIds()) }
 var showProjects by remember { mutableStateOf(false) }
 var saveMessage by remember { mutableStateOf("Автосохранение включено • изменения сохраняются локально") }
 var priceSearch by remember { mutableStateOf("") }
 var materialMode by remember { mutableStateOf("Штукатурка") }
 var materialArea by remember { mutableStateOf("45") }
 var materialLayer by remember { mutableStateOf("10") }
 var materialCoverage by remember { mutableStateOf("1.2") }
 var materialPack by remember { mutableStateOf("25") }
 var materialReserve by remember { mutableStateOf("10") }
 var layoutType by remember { mutableStateOf("Плитка") }
 var layoutA by remember { mutableStateOf("600") }
 var layoutB by remember { mutableStateOf("600") }
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
   RoomGeometry(r.name,r.vertices,if(r.wallHeights.size==r.vertices.size) r.wallHeights else List(r.vertices.size){r.height},if(r.wallThicknesses.size==r.vertices.size) r.wallThicknesses else List(r.vertices.size){0.0},r.openingsByWall.filterKeys{it in r.vertices.indices},if(r.wallHeightOverrides.size==r.vertices.size) r.wallHeightOverrides else emptyList(),r.wallMeasurements)
  }
  projectStore.saveProject(SavedProject(activeProjectId,project,geometry,lines,construction=constructionRecords))
  prefs.edit().putString("project",project).putString("active_project_id",activeProjectId).apply()
  projectIds=projectStore.listProjectIds()
  saveMessage="Сохранено: ${java.text.SimpleDateFormat("HH:mm:ss",java.util.Locale.getDefault()).format(java.util.Date())}"
 }
 fun openProject(id:String){
  val saved=projectStore.loadProject(id) ?: return
  activeProjectId=id;project=saved.name;rooms=toRoomModels(saved);lines=saved.estimate;constructionRecords=saved.construction;selected=0
  prefs.edit().putString("active_project_id",id).putString("project",project).apply()
  showProjects=false;saveMessage="Открыт проект «${saved.name}»"
 }
 // Автосохранение проекта: изменения геометрии, сметы и конструктивных записей
 // фиксируются локально при каждом изменении состояния, а не только по кнопке.
 LaunchedEffect(activeProjectId, project, rooms, lines, constructionRecords) {
  save()
 }
 fun createProject(){
  activeProjectId="project_${System.currentTimeMillis()}"
  project="Новый объект";rooms=listOf(Room(1,"Комната 1",4.0,5.0,2.7,4.0,5.0,4.0,5.0));lines=emptyList();constructionRecords=emptyList();selected=0
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
      item { CardBlock { Text("Строительные операции",fontWeight=FontWeight.Bold); Button(onClick={tab="Демонтаж"},modifier=Modifier.fillMaxWidth()){Text("Демонтаж и строительный мусор")}; OutlinedButton(onClick={tab="Перегородки"},modifier=Modifier.fillMaxWidth()){Text("Новые перегородки и материалы")} } }
    }
    "Обмеры" -> LazyColumn(verticalArrangement=Arrangement.spacedBy(12.dp),contentPadding=PaddingValues(vertical=12.dp)){
      item { CardBlock { Text("Геометрия помещения",fontSize=19.sp,fontWeight=FontWeight.Bold); Text("Базовый режим прямоугольного помещения. Длины каждой стены редактируются отдельно; исходные размеры не подгоняются автоматически.") ; Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){ rooms.forEachIndexed{i,r->FilterChip(selected=selected==i,onClick={selected=i},label={Text(r.name)})} } } }
      item { CardBlock {
        Text(current.name,fontWeight=FontWeight.Bold)
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
                wallHeightOverrides=if(sameCount&&current.wallHeightOverrides.size==parsed.size)current.wallHeightOverrides else emptyList()
              )
              val errors=GeometryEngine.validateOpenings(RoomGeometry(updated.name,updated.vertices,updated.wallHeights,updated.wallThicknesses,updated.openingsByWall,updated.wallHeightOverrides),updated.height)
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
                  wallHeightOverrides=if(sameCount&&current.wallHeightOverrides.size==parsed.size)current.wallHeightOverrides else emptyList()
                )
                val errors=GeometryEngine.validateRoom(RoomGeometry(updated.name,updated.vertices,updated.wallHeights,updated.wallThicknesses,updated.openingsByWall,updated.wallHeightOverrides,parsed),updated.height)
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
          val heights=if(current.wallHeightOverrides.size==current.vertices.size)current.wallHeightOverrides else current.vertices.indices.map{j->(baseHeights[j]+baseHeights[(j+1)%baseHeights.size])/2.0}
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
                  rooms=rooms.toMutableList().also{it[selected]=current.copy(wallHeightOverrides=updatedHeights)}
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
          val roomGeometry=RoomGeometry(updated.name,updated.vertices,if(updated.wallHeights.size==updated.vertices.size)updated.wallHeights else List(updated.vertices.size){updated.height},updated.wallThicknesses,updated.openingsByWall,updated.wallHeightOverrides)
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
        Slider(value=view3dZoom,onValueChange={view3dZoom=it},valueRange=0.65f..1.8f)
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
      items(originalCatalog.filter{priceSearch.isBlank()||it.n.contains(priceSearch,true)||it.c.contains(priceSearch,true)}) { item -> CardBlock { Row(verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(item.n,fontWeight=FontWeight.SemiBold);Text("${item.c} • ${item.u} • ${df.format(item.p)} ₽${if(item.from) " от" else ""}",color=Color.LightGray,fontSize=12.sp)}; IconButton(onClick={lines=lines+EstimateLine(item.n,item.u,item.p,1.0,item.c)}){Icon(Icons.Default.Add,tint=accent,contentDescription="Добавить")} } } }
      if(lines.isNotEmpty()) item { CardBlock { Text("Смета",fontWeight=FontWeight.Bold); lines.forEachIndexed{i,line->Text("${line.name}: ${df.format(line.qty)} ${line.unit} × ${df.format(line.price)} ₽ = ${df.format(line.total)} ₽"); Row { TextButton(onClick={lines=lines.toMutableList().also{it[i]=line.copy(qty=(line.qty-1).coerceAtLeast(0.0))}}){Text("−")}; TextButton(onClick={lines=lines.toMutableList().also{it[i]=line.copy(qty=line.qty+1)}}){Text("+")}; TextButton(onClick={lines=lines.toMutableList().also{it.removeAt(i)}}){Text("Удалить")} } }; Text("ИТОГО: ${df.format(lines.sumOf{it.total})} ₽",fontWeight=FontWeight.Bold); Button(onClick={shareEstimate(ctx,lines)},modifier=Modifier.fillMaxWidth()){Text("Поделиться сметой / текст")} ; OutlinedButton(onClick={shareCsv(ctx,lines)},modifier=Modifier.fillMaxWidth()){Text("Экспорт таблицы CSV (Excel)")} } }
    }
    "Материалы" -> LazyColumn(verticalArrangement=Arrangement.spacedBy(12.dp),contentPadding=PaddingValues(vertical=12.dp)){
      item { CardBlock {
        Text("Калькулятор расхода материалов",fontSize=19.sp,fontWeight=FontWeight.Bold)
        varType(materialMode,{materialMode=it},listOf("Штукатурка","Шпаклёвка","Краска","Грунтовка","Клей"))
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp),modifier=Modifier.fillMaxWidth()) {
          OutlinedButton(onClick={materialArea=current.walls.toString()},modifier=Modifier.weight(1f)){Text("Стены комнаты")}
          OutlinedButton(onClick={materialArea=current.floor.toString()},modifier=Modifier.weight(1f)){Text("Пол комнаты")}
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
        val valid=area>=0&&layer>=0&&rate>=0&&pack>0&&reserve>=0&&listOf(area,layer,rate,pack,reserve).all{it.isFinite()}
        val amount=if(valid) area*rate*(if(dryMix)layer else 1.0)*(1+reserve/100.0) else 0.0
        val packages=if(valid)ceil(amount/pack).toInt() else 0
        Text(if(valid) "Расход с запасом: ${df.format(amount)} кг/л • Упаковок: $packages" else "Проверь значения: фасовка должна быть больше нуля, остальные значения — неотрицательными.",color=if(valid)accent else Color(0xFFFFB86B),fontWeight=FontWeight.Bold)
        Text("Для штукатурки и шпаклёвки расход умножается на толщину слоя. Для краски, грунта и клея — на площадь. Запас учитывается отдельно. Используй расход из паспорта конкретного материала.",fontSize=12.sp,color=Color.LightGray)
      } }
    }
    "Раскладка" -> LazyColumn(verticalArrangement=Arrangement.spacedBy(12.dp),contentPadding=PaddingValues(vertical=12.dp)){
      item { CardBlock { Text("Калькулятор раскладки",fontSize=19.sp,fontWeight=FontWeight.Bold); varType(layoutType,{layoutType=it},listOf("Плитка","Ламинат","Панели")); NumberField("Размер элемента A, мм",layoutA){layoutA=it}; NumberField("Размер элемента B, мм",layoutB){layoutB=it}; NumberField("Размер помещения A, мм",layoutRoomA){layoutRoomA=it}; NumberField("Размер помещения B, мм",layoutRoomB){layoutRoomB=it}; Slider(waste,{waste=it},valueRange=0f..25f); Text("Запас: ${waste.toInt()}%"); val a=(layoutA.toDoubleOrNull()?:1.0).coerceAtLeast(1.0); val b=(layoutB.toDoubleOrNull()?:1.0).coerceAtLeast(1.0); val ra=(layoutRoomA.toDoubleOrNull()?:0.0); val rb=(layoutRoomB.toDoubleOrNull()?:0.0); val count=ceil(ra/a)*ceil(rb/b); val area=(ra*rb)/1_000_000.0; Text("Площадь: ${df.format(area)} м² • элементов без запаса: ${count.toInt()} • с запасом: ${ceil(count*(1+waste/100)).toInt()}",color=accent,fontWeight=FontWeight.Bold); Text("Предварительная сетка без оптимизации раскроя по рисунку, швам и повторному использованию обрезков.") } }
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
            Text("Стойки: ${result.studs} шт • шаг $studSpacing мм")
            Text("Направляющие: ${df.format(result.trackLengthM)} м.п. • хлыстов по 3 м: ${result.trackPieces}")
            Text("Общая длина профиля (стойки + направляющие): ${df.format(result.profileTotalLengthM)} м")
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
                    originalHeights[edge]+(originalHeights[(edge+1)%original.size]-originalHeights[edge])*t
                  }
                }
                val thicknesses=points.indices.map { i ->
                  val edge=sourceEdge(points[i],points[(i+1)%points.size])
                  if(edge==null) partitionThickness.toDouble()/1000.0 else originalThicknesses[edge]
                }
                val originalWallHeights=if(current.wallHeightOverrides.size==original.size) current.wallHeightOverrides else original.indices.map{j->(originalHeights[j]+originalHeights[(j+1)%original.size])/2.0}
                val heightOverrides=points.indices.map { i ->
                  val edge=sourceEdge(points[i],points[(i+1)%points.size])
                  if(edge==null) partitionHeight.toDoubleOrNull()?:current.height else originalWallHeights[edge]
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
                return Room(id,name,w,h,current.height,w,h,w,h,points,heights,thicknesses,openings,heightOverrides)
              }
              rooms=rooms.toMutableList().also { list ->
                list[selected]=roomFromPolygon(current.id,current.name,split.first)
                list.add(selected+1,roomFromPolygon(System.currentTimeMillis(),"${current.name} — 2",split.second))
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
 if(showAddRoom) AlertDialog(onDismissRequest={showAddRoom=false},title={Text("Новое помещение")},text={Column { OutlinedTextField(roomName,{roomName=it},label={Text("Название")}); OutlinedTextField(roomW,{roomW=it},label={Text("Ширина, м")}); OutlinedTextField(roomL,{roomL=it},label={Text("Длина, м")}); OutlinedTextField(roomH,{roomH=it},label={Text("Высота, м")}) }},confirmButton={TextButton(onClick={val w=roomW.toDoubleOrNull()?:4.0;val l=roomL.toDoubleOrNull()?:5.0;rooms=rooms+Room(System.currentTimeMillis(),roomName,w,l,roomH.toDoubleOrNull()?:2.7,w,l,w,l);selected=rooms.lastIndex;showAddRoom=false}){Text("Добавить")}},dismissButton={TextButton(onClick={showAddRoom=false}){Text("Отмена")}})
 if(showProjects) AlertDialog(onDismissRequest={showProjects=false},title={Text("Сохранённые проекты")},text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){if(projectIds.isEmpty()) Text("Пока нет сохранённых проектов");projectIds.forEach { id -> val item=projectStore.loadProject(id);if(item!=null) TextButton(onClick={openProject(id)}){Text("${item.name} • ${item.rooms.size} помещений")}}}},confirmButton={TextButton(onClick={showProjects=false}){Text("Закрыть")}})
}
}
@Composable fun CardBlock(content:@Composable ColumnScope.()->Unit){Card(Modifier.fillMaxWidth(),colors=CardDefaults.cardColors(containerColor=panel),shape=RoundedCornerShape(18.dp)){Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(8.dp),content=content)}}
@Composable fun Metric(name:String,value:Double,unit:String,modifier:Modifier=Modifier){Card(modifier,colors=CardDefaults.cardColors(containerColor=panel)){Column(Modifier.padding(10.dp)){Text(name,fontSize=11.sp,color=Color.LightGray);Text(df.format(value),fontWeight=FontWeight.Bold,fontSize=17.sp);Text(unit,fontSize=11.sp,color=Color.LightGray)}}}
@Composable fun NumberField(label:String,value:String,onChange:(String)->Unit){OutlinedTextField(value,onChange,label={Text(label)},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),modifier=Modifier.fillMaxWidth(),singleLine=true)}
@Composable fun varType(value:String,onChange:(String)->Unit,options:List<String>){Row(horizontalArrangement=Arrangement.spacedBy(5.dp)){options.forEach{FilterChip(selected=value==it,onClick={onChange(it)},label={Text(it,fontSize=11.sp)})}}}
@Composable fun RoomPlan(room:Room,modifier:Modifier=Modifier){Canvas(modifier){
 val minX=room.vertices.minOf{it.x};val maxX=room.vertices.maxOf{it.x};val minY=room.vertices.minOf{it.y};val maxY=room.vertices.maxOf{it.y}
 val bw=(maxX-minX).coerceAtLeast(0.01).toFloat();val bh=(maxY-minY).coerceAtLeast(0.01).toFloat()
 val scale=min(size.width/(bw+1f),size.height/(bh+1f));val w=bw*scale;val h=bh*scale;val left=(size.width-w)/2;val top=(size.height-h)/2
 fun map(p:Vec2)=Offset(left+(p.x-minX).toFloat()*scale,top+(p.y-minY).toFloat()*scale)
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
 Canvas(modifier.background(Color(0xFF111C2B)).pointerInput(angle,zoom){
  detectTransformGestures { _, gesturePan, gestureZoom, gestureRotation ->
   onCameraGesture(gesturePan,gestureZoom,gestureRotation)
  }
 }){
  if(room.vertices.size<3)return@Canvas
  val radians=Math.toRadians(angle.toDouble())
  val cosA=cos(radians);val sinA=sin(radians)
  val heights=room.vertices.indices.map{i->room.wallHeightOverrides.getOrNull(i)?:room.height}
  val raw=mutableListOf<Pair<Double,Double>>()
  room.vertices.forEachIndexed{i,p->
   val x=p.x*cosA-p.y*sinA;val y=p.x*sinA+p.y*cosA
   raw.add((x-y)*0.82 to (x+y)*0.42)
   val h=heights[i]
   raw.add((x-y)*0.82 to ((x+y)*0.42-h*0.95))
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
  // Рисуем дальние стены первыми, затем ближние — по средней глубине.
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
   val h=heights[i]
   val openings=room.openingsByWall[i].orEmpty().filter{it.offsetFromStart>=0.0&&it.offsetFromStart+it.width<=length+1e-6&&it.sillHeight+it.height<=h+1e-6}
   fun wallPoint(distance:Double,z:Double):Offset{
    val t=(distance/length.coerceAtLeast(1e-9)).coerceIn(0.0,1.0)
    return project(a+(b-a)*t,z)
   }
   // Разбиваем плоскость стены на прямоугольные участки и не рисуем ячейки проёмов.
   val xs=(listOf(0.0,length)+openings.flatMap{listOf(it.offsetFromStart,it.offsetFromStart+it.width)}).distinct().sorted()
   val ys=(listOf(0.0,h)+openings.flatMap{listOf(it.sillHeight,it.sillHeight+it.height)}).distinct().sorted()
   for(xi in 0 until xs.lastIndex) for(yi in 0 until ys.lastIndex){
    val x0=xs[xi];val x1=xs[xi+1];val z0=ys[yi];val z1=ys[yi+1]
    val mx=(x0+x1)/2.0;val mz=(z0+z1)/2.0
    val isOpening=openings.any{mx>it.offsetFromStart-1e-7&&mx<it.offsetFromStart+it.width+1e-7&&mz>it.sillHeight-1e-7&&mz<it.sillHeight+it.height+1e-7}
    if(!isOpening&&x1-x0>1e-7&&z1-z0>1e-7){
     val p1=wallPoint(x0,z0);val p2=wallPoint(x1,z0);val p3=wallPoint(x1,z1);val p4=wallPoint(x0,z1)
     val panelPath=Path().apply{moveTo(p1.x,p1.y);lineTo(p2.x,p2.y);lineTo(p3.x,p3.y);lineTo(p4.x,p4.y);close()}
     val tint=if(i%2==0)Color(0xFF6C879C) else Color(0xFF526D84)
     drawPath(panelPath,tint)
     drawPath(panelPath,Color(0xFFB7D0DE),style=Stroke(1.5f))
    }
   }
   // Контур стены и цветная рама проёма помогают читать двери и окна в объёме.
   val p1=project(a);val p2=project(b);val p3=project(b,h);val p4=project(a,h)
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
