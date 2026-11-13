package ru.smetapro.app

import android.content.*
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

private val bg=Color(0xFF101827); private val panel=Color(0xFF1B2638); private val blue=Color(0xFF55B8FF); private val muted=Color(0xFF9AAAC0)
data class Work(val n:String,val p:Double,val u:String,val c:String,val from:Boolean=false)
data class Line(val n:String,var p:Double,var q:Double,val u:String,val from:Boolean=false){val sum get()=p*q}
data class Project(val id:Long,var name:String,val lines:MutableList<Line> = mutableListOf()){val total get()=lines.sumOf{it.sum}}
data class PriceVersion(val id:Long,val title:String,val createdAt:Long,val items:List<Work>)
private val catalog=listOf(
Work("Укладка керамогранита 60×60, 60×20, 120×20",2000.0,"м²","Плитка"),Work("Укладка КГ 120×60, 80×80",2300.0,"м²","Плитка"),Work("Укладка керамогранита 160×80",3000.0,"м²","Плитка"),Work("Мозаика",3000.0,"м²","Плитка",true),
Work("Штукатурка ЦПС до 3 см",750.0,"м²","Подготовка"),Work("Контактный слой плиточным клеем",200.0,"м²","Подготовка"),Work("Грунтование",100.0,"м²","Подготовка"),Work("Поддон в строительном исполнении",40000.0,"шт","Сантехника",true),Work("Гидроизоляция (2 слоя)",350.0,"м²","Подготовка"),Work("Гидроизоляционная лента",200.0,"м.п.","Подготовка"),Work("Стяжка до 3 см",750.0,"м²","Подготовка"),
Work("Короб инсталляции ГКЛ",4500.0,"шт","ГКЛ"),Work("ГКЛ короб до 30 см",1300.0,"м.п.","ГКЛ"),Work("ГКЛ экран под ванну",2000.0,"шт","ГКЛ",true),Work("Облицовка керамогранитом погонаж до 40 см",1500.0,"м.п.","Плитка"),Work("Отверстия в керамограните до 70 мм",500.0,"шт","Плитка"),Work("Отверстия в керамограните более 70 мм",650.0,"шт","Плитка"),Work("Запил керамогранита 45°",1500.0,"м.п.","Плитка"),Work("Г-образная подрезка",800.0,"м.п.","Плитка"),Work("Заводской рез",500.0,"м.п.","Плитка"),
Work("Установка потайного люка с облицовкой (до 60×60 см)",4000.0,"шт","Монтаж"),Work("Установка ревизионного люка на магнитах",3000.0,"шт","Монтаж"),Work("Установка светильника",800.0,"шт","Электрика"),Work("Установка вентилятора",1000.0,"шт","Электрика"),
Work("Установка, сборка, подключение ванны (акрил, металл)",4500.0,"шт","Сантехника"),Work("Установка, сборка чугунной ванны",6000.0,"шт","Сантехника"),Work("Сборка, установка стеклянной ширмы душевой кабины",6000.0,"шт","Сантехника",true),Work("Установка унитаза",2500.0,"шт","Сантехника"),Work("Установка умывальника",1700.0,"шт","Сантехника"),Work("Установка бойлера",2500.0,"шт","Сантехника"),Work("Установка полотенцесушителя",3000.0,"шт","Сантехника"),Work("Установка, подключение инсталляции",4500.0,"шт","Сантехника"),Work("Установка унитаза инсталляции",2500.0,"шт","Сантехника"),Work("Установка кнопки инсталляции",500.0,"шт","Сантехника"),
Work("Установка зеркала",1300.0,"шт","Монтаж"),Work("Установка зеркала с подсветкой",1500.0,"шт","Электрика"),Work("Установка аксессуаров",800.0,"шт","Монтаж"),Work("Установка смесителя",1800.0,"шт","Сантехника"),Work("Установка крана умывальника",800.0,"шт","Сантехника"),Work("Установка тумбы умывальника",1700.0,"шт","Сантехника"),Work("Установка встраиваемого смесителя",3500.0,"шт","Сантехника"),Work("Установка гигиенического душа",1400.0,"шт","Сантехника"),Work("Установка встраиваемого гигиенического душа",3500.0,"шт","Сантехника"),Work("Установка смесителя + штанга",3000.0,"шт","Сантехника"),Work("Установка системы тропический душ",3500.0,"шт","Сантехника"),
Work("Распайка ПП трубы (1 точка)",2000.0,"точка","Сантехника"),Work("Канализация",2000.0,"точка","Сантехника"),Work("Установка водомера",800.0,"шт","Сантехника"),Work("Штроба в бетоне 20 мм",1000.0,"м.п.","Электрика"),Work("Штроба в бетоне 50 мм",1500.0,"м.п.","Электрика"),Work("Штроба в газоблоке 20 мм",700.0,"м.п.","Электрика"),Work("Заделка штробы",200.0,"м.п.","Подготовка"),Work("Отверстие в бетоне для подрозетника",400.0,"шт","Электрика"),Work("Монтаж подрозетника",100.0,"шт","Электрика"),Work("Установка розетки",250.0,"шт","Электрика"),Work("Демонтаж старой штукатурки",450.0,"м²","Демонтаж"),Work("Демонтаж плитки",1000.0,"м²","Демонтаж"),Work("Изготовление раковины из керамогранита (до 80 см)",25000.0,"шт","Сантехника"),Work("Монтаж раковины",7000.0,"шт","Сантехника",true)
)
class MainActivity:ComponentActivity(){override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{App(this)}}}
@Composable private fun App(ctx:Context){
 val prefs=remember{ctx.getSharedPreferences("smeta",0)}
 val projects=remember{ mutableStateListOf<Project>().apply{addAll(readProjects(prefs))} }
 if(projects.isEmpty())projects.add(Project(System.currentTimeMillis(),"Новая смета"))
 var currentId by remember{mutableLongStateOf(projects.first().id)}
 val project=projects.firstOrNull{it.id==currentId}?:projects.first()
 var page by remember{mutableStateOf("Смета")};var filter by remember{mutableStateOf("Все")};var query by remember{mutableStateOf("")}
 val priceVersions=remember{mutableStateListOf<PriceVersion>().apply{addAll(readPriceVersions(prefs));if(isEmpty())add(PriceVersion(System.currentTimeMillis(),"Базовый прайс",System.currentTimeMillis(),catalog))}}
 var activePriceId by remember{mutableLongStateOf(prefs.getLong("active_price_id",priceVersions.first().id))}
 val activePrice=priceVersions.firstOrNull{it.id==activePriceId}?:priceVersions.first()
 var priceDialog by remember{mutableStateOf(false)};var editingWork by remember{mutableStateOf<Work?>(null)}
 var priceName by remember{mutableStateOf("")};var priceValue by remember{mutableStateOf("0")};var priceUnit by remember{mutableStateOf("шт")};var priceCategory by remember{mutableStateOf("Монтаж")};var priceFrom by remember{mutableStateOf(false)}
 fun savePrices(){writePriceVersions(prefs,priceVersions);prefs.edit().putLong("active_price_id",activePriceId).apply()}
 fun openPriceEditor(w:Work?){editingWork=w;priceName=w?.n?:"";priceValue=w?.p?.toString()?:"0";priceUnit=w?.u?:"шт";priceCategory=w?.c?:"Монтаж";priceFrom=w?.from?:false;priceDialog=true}
 var showAdd by remember{mutableStateOf(false)};var newTitle by remember{mutableStateOf("")};var customName by remember{mutableStateOf("")};var customPrice by remember{mutableStateOf("0")};var customUnit by remember{mutableStateOf("шт")}
 var edit by remember{mutableStateOf<Line?>(null)}
 fun save(){writeProjects(prefs,projects)}
 MaterialTheme(colorScheme=darkColorScheme(primary=blue,background=bg,surface=panel,onSurface=Color.White)){
 Scaffold(containerColor=bg,bottomBar={NavigationBar(containerColor=panel){listOf("Смета","Прайс","Проекты","Настройки").forEach{p->NavigationBarItem(selected=page==p,onClick={page=p},icon={Text(if(p=="Смета")"₽" else if(p=="Прайс")"▤" else "▣")},label={Text(p)})}}}){pad->
 Column(Modifier.fillMaxSize().padding(pad).padding(horizontal=16.dp)){
 Row(Modifier.fillMaxWidth().padding(vertical=18.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text("СМЕТА PRO",color=blue,fontSize=12.sp,fontWeight=FontWeight.Bold,letterSpacing=2.sp);Text(if(page=="Смета")project.name else if(page=="Прайс")"Каталог работ" else if(page=="Проекты")"Мои проекты" else "Настройки",fontSize=24.sp,fontWeight=FontWeight.Bold)};if(page=="Смета")IconButton(onClick={showAdd=true}){Icon(Icons.Default.Add,null,tint=blue)}}
 when(page){
 "Смета"->{
 Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Card(Modifier.weight(1f),colors=CardDefaults.cardColors(containerColor=panel)){Column(Modifier.padding(14.dp)){Text("ПОЗИЦИЙ",color=muted,fontSize=10.sp);Text(project.lines.size.toString(),fontSize=23.sp,fontWeight=FontWeight.Bold)}};Card(Modifier.weight(1.4f),colors=CardDefaults.cardColors(containerColor=panel)){Column(Modifier.padding(14.dp)){Text("ИТОГО",color=muted,fontSize=10.sp);Text(rub(project.total),fontSize=23.sp,color=blue,fontWeight=FontWeight.ExtraBold)}}}
 Spacer(Modifier.height(10.dp));Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){OutlinedButton(onClick={pdf(ctx,project)},Modifier.weight(1f)){Icon(Icons.Default.PictureAsPdf,null);Text("PDF")};OutlinedButton(onClick={xlsx(ctx,project)},Modifier.weight(1f)){Text("Excel")};OutlinedButton(onClick={share(ctx,project)},Modifier.weight(1f)){Icon(Icons.Default.Share,null);Text("Текст")}}
 Button(onClick={page="Прайс"},Modifier.fillMaxWidth().padding(vertical=9.dp)){Icon(Icons.Default.Add,null);Text(" Добавить работу")}
 if(project.lines.isEmpty())Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Text("Смета пуста — добавьте работу из прайса",color=muted)}
 else LazyColumn(verticalArrangement=Arrangement.spacedBy(8.dp),contentPadding=PaddingValues(bottom=20.dp)){items(project.lines){l->Card(colors=CardDefaults.cardColors(containerColor=panel),shape=RoundedCornerShape(14.dp)){Column(Modifier.padding(13.dp)){Row(verticalAlignment=Alignment.CenterVertically){Text(l.n,Modifier.weight(1f),fontWeight=FontWeight.SemiBold);IconButton(onClick={edit=l}){Icon(Icons.Default.Edit,null,tint=blue)};IconButton(onClick={project.lines.remove(l);save()}){Icon(Icons.Default.Delete,null,tint=Color.Red)}};Row{Text(rub(l.p)+" / "+l.u+" × "+l.q,color=muted,modifier=Modifier.weight(1f));Text(rub(l.sum),color=blue,fontWeight=FontWeight.Bold)}}}}}
 }
 "Прайс"->{
 val categories=listOf("Все","Плитка","Сантехника","Электрика","Подготовка","ГКЛ","Демонтаж","Монтаж")
 val tabIndex=categories.indexOf(filter).coerceAtLeast(0)
 OutlinedTextField(query,{query=it},Modifier.fillMaxWidth(),placeholder={Text("Поиск работы")},singleLine=true)
 ScrollableTabRow(selectedTabIndex=tabIndex,edgePadding=0.dp,containerColor=bg,contentColor=blue) {
  categories.forEachIndexed{index,name->Tab(selected=tabIndex==index,onClick={filter=name},text={Text(name,maxLines=1)})}
 }
 val filtered=activePrice.items.filter{(filter=="Все"||it.c==filter)&&it.n.contains(query,true)}
 Text("${filtered.size} работ",color=muted,fontSize=11.sp,modifier=Modifier.padding(vertical=5.dp))
 LazyColumn(verticalArrangement=Arrangement.spacedBy(7.dp),contentPadding=PaddingValues(bottom=16.dp)){
  items(filtered){w->Card(Modifier.fillMaxWidth().clickable{project.lines.add(Line(w.n,w.p,1.0,w.u,w.from));save();page="Смета"},colors=CardDefaults.cardColors(containerColor=panel),shape=RoundedCornerShape(14.dp)){
   Row(Modifier.padding(12.dp),verticalAlignment=Alignment.CenterVertically){
    Column(Modifier.weight(1f)){Text(w.n,fontWeight=FontWeight.Medium);Text(w.c,color=muted,fontSize=11.sp)}
    Column(horizontalAlignment=Alignment.End){Text(rub(w.p)+(if(w.from)" от" else ""),color=blue,fontWeight=FontWeight.Bold);Text("за "+w.u,color=muted,fontSize=11.sp)}
   }
  }}
 }
 }
 else->{Button(onClick={showAdd=true},Modifier.fillMaxWidth()){Text("＋ Создать смету")};LazyColumn{items(projects){p->Card(Modifier.fillMaxWidth().padding(vertical=5.dp).clickable{currentId=p.id;page="Смета"},colors=CardDefaults.cardColors(containerColor=panel)){Row(Modifier.padding(16.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(p.name,fontWeight=FontWeight.Bold);Text(p.lines.size.toString()+" позиций",color=muted)};Text(rub(p.total),color=blue,fontWeight=FontWeight.Bold)}}}}}
 }
 }
 }}
 if(showAdd)AlertDialog(onDismissRequest={showAdd=false},title={Text("Новая смета / работа")},text={Column{OutlinedTextField(newTitle,{newTitle=it},label={Text("Название новой сметы")});OutlinedTextField(customName,{customName=it},label={Text("Или название работы")});OutlinedTextField(customPrice,{customPrice=it},label={Text("Цена")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal));OutlinedTextField(customUnit,{customUnit=it},label={Text("Единица")})}},confirmButton={TextButton(onClick={if(newTitle.isNotBlank()){val p=Project(System.currentTimeMillis(),newTitle);projects.add(p);currentId=p.id;newTitle="";save()}else if(customName.isNotBlank()){project.lines.add(Line(customName,customPrice.replace(',','.').toDoubleOrNull()?:0.0,1.0,customUnit));customName="";save()};showAdd=false}){Text("Сохранить")}},dismissButton={TextButton(onClick={showAdd=false}){Text("Отмена")}})
 edit?.let{line->var price by remember(line){mutableStateOf(line.p.toString())};var qty by remember(line){mutableStateOf(line.q.toString())};AlertDialog(onDismissRequest={edit=null},title={Text("Редактировать")},text={Column{Text(line.n);OutlinedTextField(price,{price=it},label={Text("Цена")});OutlinedTextField(qty,{qty=it},label={Text("Количество")})}},confirmButton={TextButton(onClick={line.p=price.replace(',','.').toDoubleOrNull()?:line.p;line.q=qty.replace(',','.').toDoubleOrNull()?:line.q;save();edit=null}){Text("Сохранить")}},dismissButton={TextButton(onClick={edit=null}){Text("Отмена")}})}
}
private fun writePriceVersions(prefs:SharedPreferences,versions:List<PriceVersion>){val all=JSONArray();versions.forEach{v->val o=JSONObject().put("id",v.id).put("title",v.title).put("createdAt",v.createdAt);val a=JSONArray();v.items.forEach{w->a.put(JSONObject().put("n",w.n).put("p",w.p).put("u",w.u).put("c",w.c).put("f",w.from))};o.put("items",a);all.put(o)};prefs.edit().putString("price_versions",all.toString()).apply()}
private fun readPriceVersions(prefs:SharedPreferences):List<PriceVersion> = try{val a=JSONArray(prefs.getString("price_versions","[]"));(0 until a.length()).map{i->val o=a.getJSONObject(i);val items=o.getJSONArray("items");val works=(0 until items.length()).map{j->val w=items.getJSONObject(j);Work(w.getString("n"),w.getDouble("p"),w.getString("u"),w.getString("c"),w.optBoolean("f"))};PriceVersion(o.getLong("id"),o.getString("title"),o.optLong("createdAt"),works)}}catch(_:Exception){emptyList()}
private fun rub(v:Double)="%,.0f ₽".format(java.util.Locale("ru","RU"),v)
private fun writeProjects(prefs:SharedPreferences,ps:List<Project>){val a=JSONArray();ps.forEach{p->val o=JSONObject().put("id",p.id).put("name",p.name);val ls=JSONArray();p.lines.forEach{ls.put(JSONObject().put("n",it.n).put("p",it.p).put("q",it.q).put("u",it.u).put("f",it.from))};o.put("lines",ls);a.put(o)};prefs.edit().putString("data",a.toString()).apply()}
private fun readProjects(p:SharedPreferences):List<Project> = try{val a=JSONArray(p.getString("data","[]"));(0 until a.length()).map{i->val o=a.getJSONObject(i);val pr=Project(o.getLong("id"),o.getString("name"));val l=o.getJSONArray("lines");for(j in 0 until l.length()){val x=l.getJSONObject(j);pr.lines.add(Line(x.getString("n"),x.getDouble("p"),x.getDouble("q"),x.getString("u"),x.optBoolean("f")))};pr}}catch(_:Exception){emptyList()}
private fun share(c:Context,p:Project){val s=buildString{append(p.name+"\n");p.lines.forEach{append(it.n+" | "+it.q+" "+it.u+" × "+rub(it.p)+" = "+rub(it.sum)+"\n")};append("ИТОГО: "+rub(p.total))};c.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT,s),"Отправить смету"))}
private fun sendFile(c:Context,f:File,m:String){val u=FileProvider.getUriForFile(c,c.packageName+".provider",f);c.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType(m).putExtra(Intent.EXTRA_STREAM,u).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION),"Поделиться файлом"))}
private fun pdf(c:Context,p:Project){val d=PdfDocument();val page=d.startPage(PdfDocument.PageInfo.Builder(595,842,1).create());val cv=page.canvas;val paint=Paint().apply{color=android.graphics.Color.DKGRAY;textSize=18f;typeface=android.graphics.Typeface.DEFAULT_BOLD};cv.drawText("СМЕТА PRO",35f,40f,paint);paint.textSize=14f;cv.drawText(p.name,35f,70f,paint);paint.textSize=10f;paint.typeface=android.graphics.Typeface.DEFAULT;var y=105f;cv.drawText("Работа",35f,y,paint);cv.drawText("Кол-во",350f,y,paint);cv.drawText("Сумма",485f,y,paint);y+=20; p.lines.take(34).forEach{cv.drawText(it.n.take(52),35f,y,paint);cv.drawText(it.q.toString()+" "+it.u,350f,y,paint);cv.drawText(rub(it.sum),485f,y,paint);y+=18};paint.textSize=15f;paint.typeface=android.graphics.Typeface.DEFAULT_BOLD;cv.drawText("ИТОГО: "+rub(p.total),390f,y+20,paint);d.finishPage(page);val f=File(c.cacheDir,"smeta.pdf");f.outputStream().use{d.writeTo(it)};d.close();sendFile(c,f,"application/pdf")}
private fun xlsx(c:Context,p:Project){val f=File(c.cacheDir,"smeta.xlsx");val rows=mutableListOf(listOf("Смета",p.name),listOf("Работа","Количество","Ед.","Цена, руб.","Сумма, руб."));p.lines.forEach{rows.add(listOf(it.n,it.q.toString(),it.u,it.p.toString(),it.sum.toString()))};rows.add(listOf("ИТОГО","","","",p.total.toString()));ZipOutputStream(f.outputStream()).use{z->fun put(n:String,s:String){z.putNextEntry(ZipEntry(n));z.write(s.toByteArray());z.closeEntry()};put("[Content_Types].xml","""<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/><Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/><Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/></Types>""");put("_rels/.rels","""<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/></Relationships>""");put("xl/workbook.xml","""<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"><sheets><sheet name="Смета" sheetId="1" r:id="rId1"/></sheets></workbook>""");put("xl/_rels/workbook.xml.rels","""<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/></Relationships>""");val xml=buildString{append("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetData>""");rows.forEachIndexed{ri,row->append("<row r=\""+(ri+1)+"\">");row.forEachIndexed{ci,v->val col=('A'.code+ci).toChar();val ref=col.toString()+(ri+1);val num=v.toDoubleOrNull();if(num!=null)append("<c r=\""+ref+"\"><v>"+num+"</v></c>")else append("<c r=\""+ref+"\" t=\"inlineStr\"><is><t>"+v.replace("&","&amp;").replace("<","&lt;")+"</t></is></c>")};append("</row>")};append("</sheetData></worksheet>")};put("xl/worksheets/sheet1.xml",xml)};sendFile(c,f,"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")}
