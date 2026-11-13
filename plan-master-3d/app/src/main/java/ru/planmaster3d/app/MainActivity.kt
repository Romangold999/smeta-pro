package ru.planmaster3d.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
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

data class Room(val id:Long,val name:String,val width:Double,val length:Double,val height:Double,val wall1:Double,val wall2:Double,val wall3:Double,val wall4:Double) {
    val floor get() = width*length
    val perimeter get() = wall1+wall2+wall3+wall4
    val walls get() = perimeter*height
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
@Composable fun PlanApp(ctx:Context) {
 val prefs=remember{ctx.getSharedPreferences("plan_master_3d",0)}
 var tab by remember { mutableStateOf("Объект") }
 var project by remember { mutableStateOf(prefs.getString("project","Новый объект") ?: "Новый объект") }
 var rooms by remember { mutableStateOf(listOf(Room(1,"Комната 1",4.0,5.0,2.7,4.0,5.0,4.0,5.0))) }
 var selected by remember { mutableIntStateOf(0) }
 var lines by remember { mutableStateOf(listOf<EstimateLine>()) }
 var priceSearch by remember { mutableStateOf("") }
 var materialMode by remember { mutableStateOf("Штукатурка") }
 var materialArea by remember { mutableStateOf("45") }
 var materialLayer by remember { mutableStateOf("10") }
 var materialCoverage by remember { mutableStateOf("1.2") }
 var materialPack by remember { mutableStateOf("25") }
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
 var demolitionWall by remember { mutableStateOf("Стена 1") }
 var demolitionMaterial by remember { mutableStateOf("Керамическая плитка") }
 var demolitionLength by remember { mutableStateOf("4") }
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
 var partitionRate by remember { mutableStateOf("0") }
 val current=rooms.getOrNull(selected) ?: rooms.first()
 val totalFloor=rooms.sumOf{it.floor}
 val totalWalls=rooms.sumOf{it.walls}
 val totalPerimeter=rooms.sumOf{it.perimeter}
 fun save(){ prefs.edit().putString("project",project).apply() }
 Scaffold(containerColor=navy, topBar={ TopAppBar(title={Column { Text("PLAN MASTER 3D",fontWeight=FontWeight.Bold); Text(project,fontSize=12.sp,color=Color.LightGray) }},actions={IconButton(onClick={save()}){Icon(Icons.Default.Save,"Сохранить")}},colors=TopAppBarDefaults.topAppBarColors(containerColor=navy)) },
 bottomBar={NavigationBar(containerColor=panel){ listOf("Объект","Обмеры","Смета","Материалы","Раскладка").forEach { label -> NavigationBarItem(selected=tab==label,onClick={tab=label},icon={Icon(when(label){"Объект"->Icons.Default.Home;"Обмеры"->Icons.Default.SquareFoot;"Смета"->Icons.Default.Receipt;"Материалы"->Icons.Default.Inventory2;else->Icons.Default.GridOn},null)},label={Text(label,fontSize=10.sp)}) } } }) { pad ->
  Column(Modifier.fillMaxSize().padding(pad).padding(horizontal=14.dp)) {
   when(tab){
    "Объект" -> LazyColumn(verticalArrangement=Arrangement.spacedBy(12.dp),contentPadding=PaddingValues(vertical=12.dp)){
      item { CardBlock { Text("Панель объекта",fontSize=20.sp,fontWeight=FontWeight.Bold); OutlinedTextField(project,{project=it},label={Text("Название объекта")},modifier=Modifier.fillMaxWidth()); Text("Помещений: ${rooms.size}  •  Площадь пола: ${df.format(totalFloor)} м²"); Text("Периметр: ${df.format(totalPerimeter)} м  •  Стены: ${df.format(totalWalls)} м²") } }
      item { Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){ Metric("Пол",totalFloor,"м²",Modifier.weight(1f)); Metric("Стены",totalWalls,"м²",Modifier.weight(1f)); Metric("Периметр",totalPerimeter,"м",Modifier.weight(1f)) } }
      item { CardBlock { Text("Помещения",fontWeight=FontWeight.Bold); rooms.forEachIndexed { i,r -> Row(Modifier.fillMaxWidth().clickable{selected=i;tab="Обмеры"}.padding(vertical=8.dp),horizontalArrangement=Arrangement.SpaceBetween){ Text("${r.name}  •  ${df.format(r.floor)} м²"); Text("Изменить ›",color=accent) } } ; Button(onClick={showAddRoom=true},modifier=Modifier.fillMaxWidth()){Text("Добавить помещение")} } }
      item { CardBlock { Text("Быстрые ведомости",fontWeight=FontWeight.Bold); Text("Площадь пола: ${df.format(totalFloor)} м²"); Text("Площадь стен без вычета проёмов: ${df.format(totalWalls)} м²"); Text("Периметр: ${df.format(totalPerimeter)} м") } }
      item { CardBlock { Text("Строительные операции",fontWeight=FontWeight.Bold); Button(onClick={tab="Демонтаж"},modifier=Modifier.fillMaxWidth()){Text("Демонтаж и строительный мусор")}; OutlinedButton(onClick={tab="Перегородки"},modifier=Modifier.fillMaxWidth()){Text("Новые перегородки и материалы")} } }
    }
    "Обмеры" -> LazyColumn(verticalArrangement=Arrangement.spacedBy(12.dp),contentPadding=PaddingValues(vertical=12.dp)){
      item { CardBlock { Text("Геометрия помещения",fontSize=19.sp,fontWeight=FontWeight.Bold); Text("Базовый режим прямоугольного помещения. Длины каждой стены редактируются отдельно; исходные размеры не подгоняются автоматически.") ; Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){ rooms.forEachIndexed{i,r->FilterChip(selected=selected==i,onClick={selected=i},label={Text(r.name)})} } } }
      item { CardBlock { Text(current.name,fontWeight=FontWeight.Bold); NumberField("Ширина комнаты, м",roomW){roomW=it}; NumberField("Длина комнаты, м",roomL){roomL=it}; NumberField("Высота стен, м",roomH){roomH=it}; Button(onClick={ rooms=rooms.toMutableList().also{it[selected]=current.copy(width=roomW.toDoubleOrNull()?:current.width,length=roomL.toDoubleOrNull()?:current.length,height=roomH.toDoubleOrNull()?:current.height,wall1=roomW.toDoubleOrNull()?:current.wall1,wall3=roomW.toDoubleOrNull()?:current.wall3,wall2=roomL.toDoubleOrNull()?:current.wall2,wall4=roomL.toDoubleOrNull()?:current.wall4)} },modifier=Modifier.fillMaxWidth()){Text("Применить размеры")} } }
      item { CardBlock { Text("Схема 2D",fontWeight=FontWeight.Bold); RoomPlan(current,Modifier.fillMaxWidth().height(220.dp)); Text("Схема масштабируется по габаритам. Для косых и ломаных контуров будет отдельный редактор геометрии.") } }
      item { CardBlock { Text("Автоматические расчёты",fontWeight=FontWeight.Bold); Text("Пол: ${df.format(current.floor)} м²"); Text("Стены: ${df.format(current.walls)} м²"); Text("Периметр: ${df.format(current.perimeter)} м"); Text("Потолок: ${df.format(current.floor)} м²") } }
    }
    "Смета" -> LazyColumn(verticalArrangement=Arrangement.spacedBy(10.dp),contentPadding=PaddingValues(vertical=12.dp)){
      item { CardBlock { Text("Каталог работ Smeta Pro + новые разделы",fontWeight=FontWeight.Bold); OutlinedTextField(priceSearch,{priceSearch=it},label={Text("Поиск работ")},modifier=Modifier.fillMaxWidth()); Text("Выбрано позиций: ${lines.size}  •  Итого: ${df.format(lines.sumOf{it.total})} ₽",color=accent,fontWeight=FontWeight.Bold) } }
      items(originalCatalog.filter{priceSearch.isBlank()||it.n.contains(priceSearch,true)||it.c.contains(priceSearch,true)}) { item -> CardBlock { Row(verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(item.n,fontWeight=FontWeight.SemiBold);Text("${item.c} • ${item.u} • ${df.format(item.p)} ₽${if(item.from) " от" else ""}",color=Color.LightGray,fontSize=12.sp)}; IconButton(onClick={lines=lines+EstimateLine(item.n,item.u,item.p,1.0,item.c)}){Icon(Icons.Default.Add,color=accent,contentDescription="Добавить")} } } }
      if(lines.isNotEmpty()) item { CardBlock { Text("Смета",fontWeight=FontWeight.Bold); lines.forEachIndexed{i,line->Text("${line.name}: ${df.format(line.qty)} ${line.unit} × ${df.format(line.price)} ₽ = ${df.format(line.total)} ₽"); Row { TextButton(onClick={lines=lines.toMutableList().also{it[i]=line.copy(qty=(line.qty-1).coerceAtLeast(0.0))}}){Text("−")}; TextButton(onClick={lines=lines.toMutableList().also{it[i]=line.copy(qty=line.qty+1)}}){Text("+")}; TextButton(onClick={lines=lines.toMutableList().also{it.removeAt(i)}}){Text("Удалить")} } }; Text("ИТОГО: ${df.format(lines.sumOf{it.total})} ₽",fontWeight=FontWeight.Bold); Button(onClick={shareEstimate(ctx,lines)},modifier=Modifier.fillMaxWidth()){Text("Поделиться сметой / текст")} ; OutlinedButton(onClick={shareCsv(ctx,lines)},modifier=Modifier.fillMaxWidth()){Text("Экспорт таблицы CSV (Excel)")} } }
    }
    "Материалы" -> LazyColumn(verticalArrangement=Arrangement.spacedBy(12.dp),contentPadding=PaddingValues(vertical=12.dp)){
      item { CardBlock { Text("Калькулятор расхода материалов",fontSize=19.sp,fontWeight=FontWeight.Bold); varType(materialMode,{materialMode=it},listOf("Штукатурка","Шпаклёвка","Краска","Грунтовка","Клей")); NumberField("Площадь, м²",materialArea){materialArea=it}; NumberField("Толщина слоя, мм",materialLayer){materialLayer=it}; NumberField("Расход на 1 м² (на 1 мм для сухих смесей)",materialCoverage){materialCoverage=it}; NumberField("Фасовка, кг или л",materialPack){materialPack=it}; val area=materialArea.toDoubleOrNull()?:0.0; val layer=materialLayer.toDoubleOrNull()?:1.0; val rate=materialCoverage.toDoubleOrNull()?:0.0; val pack=materialPack.toDoubleOrNull()?:1.0; val amount=area*rate*(if(materialMode=="Штукатурка"||materialMode=="Шпаклёвка") layer else 1.0); Text("Расход: ${df.format(amount)} кг/л • Упаковок: ${ceil(amount/pack.coerceAtLeast(0.01)).toInt()}",color=accent,fontWeight=FontWeight.Bold); Text("Расчёт ориентировочный: фактический расход задаётся по паспорту выбранного материала.") } }
    }
    "Раскладка" -> LazyColumn(verticalArrangement=Arrangement.spacedBy(12.dp),contentPadding=PaddingValues(vertical=12.dp)){
      item { CardBlock { Text("Калькулятор раскладки",fontSize=19.sp,fontWeight=FontWeight.Bold); varType(layoutType,{layoutType=it},listOf("Плитка","Ламинат","Панели")); NumberField("Размер элемента A, мм",layoutA){layoutA=it}; NumberField("Размер элемента B, мм",layoutB){layoutB=it}; NumberField("Размер помещения A, мм",layoutRoomA){layoutRoomA=it}; NumberField("Размер помещения B, мм",layoutRoomB){layoutRoomB=it}; Slider(waste,{waste=it},valueRange=0f..25f); Text("Запас: ${waste.toInt()}%"); val a=(layoutA.toDoubleOrNull()?:1.0).coerceAtLeast(1.0); val b=(layoutB.toDoubleOrNull()?:1.0).coerceAtLeast(1.0); val ra=(layoutRoomA.toDoubleOrNull()?:0.0); val rb=(layoutRoomB.toDoubleOrNull()?:0.0); val count=ceil(ra/a)*ceil(rb/b); val area=(ra*rb)/1_000_000.0; Text("Площадь: ${df.format(area)} м² • элементов без запаса: ${count.toInt()} • с запасом: ${ceil(count*(1+waste/100)).toInt()}",color=accent,fontWeight=FontWeight.Bold); Text("Предварительная сетка без оптимизации раскроя по рисунку, швам и повторному использованию обрезков.") } }
    }
    "Демонтаж" -> LazyColumn(verticalArrangement=Arrangement.spacedBy(12.dp),contentPadding=PaddingValues(vertical=12.dp)){
      item { CardBlock {
        Text("Демонтаж выбранного участка",fontSize=19.sp,fontWeight=FontWeight.Bold)
        Text("Выбери сторону помещения. Длину можно уменьшить, если демонтируется только часть стены.")
        Row(horizontalArrangement=Arrangement.spacedBy(5.dp)){ listOf("Стена 1","Стена 2","Стена 3","Стена 4").forEach { side -> FilterChip(selected=demolitionWall==side,onClick={demolitionWall=side;demolitionLength=if(side=="Стена 1"||side=="Стена 3") current.width.toString() else current.length.toString();demolitionHeight=current.height.toString()},label={Text(side)}) } }
        varType(demolitionMaterial,{demolitionMaterial=it;demolitionDensity=ConstructionMaterialPresets.demolitionDensities[it]?.toString()?:demolitionDensity;demolitionThickness=when(it){"Керамическая плитка"->"10";"ГКЛ"->"12.5";"Гипсовая штукатурка","Цементная штукатурка"->"20";"Цементная стяжка/бетон"->"50";"Газобетон"->"100";else->"120"}},ConstructionMaterialPresets.demolitionDensities.keys.toList())
        NumberField("Длина демонтируемого участка, м",demolitionLength){demolitionLength=it}
        NumberField("Высота участка, м",demolitionHeight){demolitionHeight=it}
        NumberField("Толщина снимаемого слоя/конструкции, мм",demolitionThickness){demolitionThickness=it}
        NumberField("Плотность материала, кг/м³",demolitionDensity){demolitionDensity=it}
        NumberField("Коэффициент разрыхления (обычно больше 1)",demolitionBulking){demolitionBulking=it}
      } }
      item { CardBlock {
        Text("Мешки и вывоз",fontWeight=FontWeight.Bold)
        NumberField("Объём мешка, литров",bagLitres){bagLitres=it}
        NumberField("Допустимая масса одного мешка, кг",bagMaxKg){bagMaxKg=it}
        NumberField("Коэффициент заполнения мешка (0,1–1)",bagFill){bagFill=it}
        NumberField("Цена демонтажа, ₽/м²",demolitionRate){demolitionRate=it}
        val result=runCatching { DemolitionCalculator.calculate(DemolitionInput(demolitionLength.toDouble(),demolitionHeight.toDouble(),demolitionThickness.toDouble(),demolitionDensity.toDouble(),demolitionBulking.toDouble(),bagLitres.toDouble(),bagMaxKg.toDouble(),bagFill.toDouble())) }.getOrNull()
        if(result!=null){
          Text("Площадь: ${df.format(result.areaM2)} м²")
          Text("Объём до разрыхления: ${df.format(result.solidVolumeM3)} м³")
          Text("Масса мусора: ${df.format(result.solidMassKg)} кг")
          Text("Объём после разрыхления: ${df.format(result.looseVolumeM3)} м³")
          Text("Мешков по объёму: ${result.bagsByVolume} • по весу: ${result.bagsByWeight}")
          Text("Итого мешков: ${result.bagsRequired} • средняя масса: ${df.format(result.estimatedBagMassKg)} кг/мешок",color=accent,fontWeight=FontWeight.Bold)
          Text("Оценка зависит от фактической плотности, влажности и состава мусора. Плотность и коэффициенты можно изменить.")
          Button(onClick={
            val rate=demolitionRate.toDoubleOrNull()?:0.0
            val workName=if(demolitionMaterial=="Керамическая плитка") "Демонтаж плитки" else if(demolitionMaterial=="Гипсовая штукатурка"||demolitionMaterial=="Цементная штукатурка") "Демонтаж старой штукатурки" else "Демонтаж: $demolitionMaterial"
            lines=lines+EstimateLine(workName,"м²",rate,result.areaM2,"Демонтаж")+EstimateLine("Упаковка мусора ($demolitionMaterial)","меш",0.0,result.bagsRequired.toDouble(),"Демонтаж")+EstimateLine("Масса строительного мусора","т",0.0,result.solidMassKg/1000.0,"Вывоз")
            tab="Смета"
          },modifier=Modifier.fillMaxWidth()){Text("Добавить демонтаж и отходы в смету")}
        } else Text("Проверь размеры, плотность и параметры мешка.",color=Color(0xFFFFB86B))
      } }
    }
    "Перегородки" -> LazyColumn(verticalArrangement=Arrangement.spacedBy(12.dp),contentPadding=PaddingValues(vertical=12.dp)){
      item { CardBlock {
        Text("Новая перегородка",fontSize=19.sp,fontWeight=FontWeight.Bold)
        Text("Задай длину, высоту и проёмы. Расчёт будет привязан к этому помещению; привязка к интерактивному 2D-чертежу добавляется следующим этапом.")
        NumberField("Длина перегородки, м",partitionLength){partitionLength=it}
        NumberField("Высота перегородки, м",partitionHeight){partitionHeight=it}
        NumberField("Площадь дверных/прочих проёмов, м²",partitionOpenings){partitionOpenings=it}
        NumberField("Запас материала, %",partitionReserve){partitionReserve=it}
        varType(partitionKind,{partitionKind=it},listOf("Газобетон","ГКЛ"))
      } }
      item { CardBlock {
        if(partitionKind=="Газобетон"){
          Text("Кладка из газобетона",fontWeight=FontWeight.Bold)
          varType(partitionThickness,{partitionThickness=it},listOf("75","100","150","200"))
          Text("Толщина блока: $partitionThickness мм")
          NumberField("Длина блока, мм",blockLength){blockLength=it}
          NumberField("Высота блока, мм",blockHeight){blockHeight=it}
          NumberField("Плотность блока, кг/м³",blockDensity){blockDensity=it}
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
        }
        NumberField("Цена монтажа, ₽/м² (задай свою)",partitionRate){partitionRate=it}
      } }
      item { CardBlock {
        val result=runCatching {
          if(partitionKind=="Газобетон") PartitionCalculator.calculate(PartitionInput(PartitionKind.AAC_BLOCK,partitionLength.toDouble(),partitionHeight.toDouble(),partitionThickness.toInt(),partitionOpenings.toDouble(),partitionReserve.toDouble(),blockLength.toInt(),blockHeight.toInt(),blockDensity.toDouble()))
          else PartitionCalculator.calculate(PartitionInput(PartitionKind.GYPSUM_BOARD,partitionLength.toDouble(),partitionHeight.toDouble(),partitionThickness.toInt(),partitionOpenings.toDouble(),partitionReserve.toDouble(),boardLayersPerSide=boardLayers.toInt(),boardSides=boardSides.toInt(),studSpacingMm=studSpacing.toInt(),insulation=insulation))
        }.getOrNull()
        Text("Расчёт материалов и работ",fontWeight=FontWeight.Bold)
        if(result!=null){
          Text("Площадь до вычета проёмов: ${df.format(result.grossAreaM2)} м²")
          Text("Чистая площадь: ${df.format(result.netAreaM2)} м²")
          if(partitionKind=="Газобетон"){
            Text("Объём кладки: ${df.format(result.wallVolumeM3)} м³")
            Text("Блоков с запасом: ${result.blockCount} шт")
            Text("Ориентировочная масса блоков: ${df.format(result.blockMassKg)} кг")
            Text("Клей для кладки: ${df.format(result.adhesiveKg)} кг")
          } else {
            Text("Листов ГКЛ с запасом: ${result.boardSheets} шт")
            Text("Стойки: ${result.studs} шт • шаг $studSpacing мм")
            Text("Направляющие: ${df.format(result.trackLengthM)} м.п. • хлыстов по 3 м: ${result.trackPieces}")
            Text("Общая длина профиля (стойки + направляющие): ${df.format(result.profileTotalLengthM)} м")
            if(insulation) Text("Шумоизоляция: ${df.format(result.insulationVolumeM3)} м³ • масса ${df.format(result.insulationMassKg)} кг")
          }
          Button(onClick={
            val rate=partitionRate.toDoubleOrNull()?:0.0
            val rows=mutableListOf<EstimateLine>()
            rows.add(EstimateLine(if(partitionKind=="Газобетон") "Монтаж перегородки из газобетона $partitionThickness мм" else "Монтаж каркасной перегородки ГКЛ $partitionThickness мм","м²",rate,result.netAreaM2,"Перегородки"))
            if(partitionKind=="Газобетон"){
              rows.add(EstimateLine("Газобетонные блоки $partitionThickness мм","шт",0.0,result.blockCount.toDouble(),"Материалы",true))
              rows.add(EstimateLine("Клей для газобетона","кг",0.0,result.adhesiveKg,"Материалы",true))
            } else {
              rows.add(EstimateLine("ГКЛ ($boardLayers слоя, $boardSides сторон)","лист",0.0,result.boardSheets.toDouble(),"Материалы",true))
              rows.add(EstimateLine("Профиль каркаса, стойки + направляющие","м.п.",0.0,result.profileTotalLengthM,"Материалы",true))
              if(insulation) rows.add(EstimateLine("Шумоизоляционное заполнение","м³",0.0,result.insulationVolumeM3,"Материалы",true))
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
}
@Composable fun CardBlock(content:@Composable ColumnScope.()->Unit){Card(Modifier.fillMaxWidth(),colors=CardDefaults.cardColors(containerColor=panel),shape=RoundedCornerShape(18.dp)){Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(8.dp),content=content)}}
@Composable fun Metric(name:String,value:Double,unit:String,modifier:Modifier=Modifier){Card(modifier,colors=CardDefaults.cardColors(containerColor=panel)){Column(Modifier.padding(10.dp)){Text(name,fontSize=11.sp,color=Color.LightGray);Text(df.format(value),fontWeight=FontWeight.Bold,fontSize=17.sp);Text(unit,fontSize=11.sp,color=Color.LightGray)}}}
@Composable fun NumberField(label:String,value:String,onChange:(String)->Unit){OutlinedTextField(value,onChange,label={Text(label)},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),modifier=Modifier.fillMaxWidth(),singleLine=true)}
@Composable fun varType(value:String,onChange:(String)->Unit,options:List<String>){Row(horizontalArrangement=Arrangement.spacedBy(5.dp)){options.forEach{FilterChip(selected=value==it,onClick={onChange(it)},label={Text(it,fontSize=11.sp)})}}}
@Composable fun RoomPlan(room:Room,modifier:Modifier=Modifier){Canvas(modifier){val scale=min(size.width/(room.width.toFloat()+1),size.height/(room.length.toFloat()+1));val w=room.width.toFloat()*scale;val h=room.length.toFloat()*scale;val left=(size.width-w)/2;val top=(size.height-h)/2;drawRect(Color(0xFF6ED4E5),Offset(left,top),androidx.compose.ui.geometry.Size(w,h),style=Stroke(5f));drawLine(Color.White,Offset(left,top),Offset(left+w,top),2f);drawLine(Color.White,Offset(left+w,top),Offset(left+w,top+h),2f)}}
fun shareEstimate(ctx:Context,lines:List<EstimateLine>){val body=buildString{append("PLAN MASTER 3D — СМЕТА\n\n");lines.forEach{append("${it.name}: ${it.qty} ${it.unit} × ${it.price} = ${it.total} ₽\n")};append("\nИТОГО: ${lines.sumOf{it.total}} ₽")};val i=android.content.Intent(android.content.Intent.ACTION_SEND).setType("text/plain").putExtra(android.content.Intent.EXTRA_TEXT,body);ctx.startActivity(android.content.Intent.createChooser(i,"Отправить смету"))}
fun shareCsv(ctx:Context,lines:List<EstimateLine>){val body=buildString{append("Раздел;Наименование;Ед.;Количество;Цена;Сумма\n");lines.forEach{append("${it.category};${it.name};${it.unit};${it.qty};${it.price};${it.total}\n")}};val i=android.content.Intent(android.content.Intent.ACTION_SEND).setType("text/csv").putExtra(android.content.Intent.EXTRA_TEXT,body);ctx.startActivity(android.content.Intent.createChooser(i,"Экспорт таблицы"))}
