package ru.planmaster3d.app

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

enum class PdfReportMode { TECHNICAL, PROPOSAL }
data class PdfReportData(
    val projectName:String,
    val rooms:List<Room>,
    val estimate:List<EstimateLine>,
    val construction:List<ConstructionRecord>,
    val engineeringPoints:List<EngineeringPoint>,
    val createdAt:Long=System.currentTimeMillis()
) {
    val total get()=estimate.sumOf { it.total }
    val floorArea get()=rooms.sumOf { it.floor }
    val wallArea get()=rooms.sumOf { it.walls }
    val ceilingArea get()=rooms.sumOf { it.geometry.ceilingArea }
    val openingArea get()=rooms.sumOf { it.geometry.openingArea }
}

object PdfReportRenderer {
    private const val W=595
    private const val H=842
    private const val L=42f
    private const val R=553f
    private const val INK=0xFF172337.toInt()
    private const val MUTED=0xFF65758B.toInt()
    private const val TEAL=0xFF087F8C.toInt()
    private const val PALE=0xFFEAF3F5.toInt()
    private const val LINE=0xFFD7E0E8.toInt()
    private const val WHITE=0xFFFFFFFF.toInt()
    private const val SOFT=0xFFF5F7FA.toInt()

    fun render(data:PdfReportData,mode:PdfReportMode):ByteArray {
        require(data.rooms.isNotEmpty()) { "В проекте нет помещений" }
        require(data.rooms.all { it.vertices.size>=3 && it.floor.isFinite() }) { "Проверь геометрию помещений" }
        val doc=PdfDocument()
        var number=0
        fun addPage(title:String,subtitle:String="",isCover:Boolean=false,draw:(Canvas)->Unit={}) {
            number++
            val page=doc.startPage(PdfDocument.PageInfo.Builder(W,H,number).create())
            if(isCover) cover(page.canvas,data,mode) else {
                header(page.canvas,title,subtitle)
                draw(page.canvas)
                footer(page.canvas,number)
            }
            doc.finishPage(page)
        }
        addPage(if(mode==PdfReportMode.PROPOSAL) "Коммерческое предложение" else "Технический отчёт",isCover=true)
        addPage("Сводка проекта","Площади и состав объекта") { summary(it,data,mode) }
        data.rooms.forEachIndexed { i,room ->
            addPage("План помещения",room.name) { plan(it,room,i,data.engineeringPoints.filter { p->p.roomIndex==i }) }
            addPage("Объёмная схема помещения",room.name) { axonometric(it,room) }
        }
        if(data.construction.isNotEmpty()) addPage("Конструктивные работы","Демонтаж и новые перегородки") { construction(it,data) }
        if(data.engineeringPoints.isNotEmpty()) addPage("Инженерные точки","Размещение по помещениям и координатам") { engineering(it,data) }
        val perPage=16
        val pages=max(1,(data.estimate.size+perPage-1)/perPage)
        repeat(pages) { index ->
            val from=index*perPage
            val to=min(data.estimate.size,from+perPage)
            addPage("Смета работ и материалов","Лист " +(index+1)+" из "+pages) { estimate(it,data,from,to,index==pages-1) }
        }
        val output=ByteArrayOutputStream()
        doc.writeTo(output)
        doc.close()
        return output.toByteArray()
    }

    private fun p(color:Int,size:Float,bold:Boolean=false)=Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color=color
        textSize=size
        typeface=if(bold) Typeface.create("sans-serif",Typeface.BOLD) else Typeface.create("sans-serif",Typeface.NORMAL)
    }
    private fun t(c:Canvas,s:String,x:Float,y:Float,size:Float=10f,color:Int=INK,bold:Boolean=false) { c.drawText(s,x,y,p(color,size,bold)) }
    private fun line(c:Canvas,y:Float,color:Int=LINE) { c.drawLine(L,y,R,y,p(color,1f)) }
    private fun wrap(c:Canvas,value:String,x:Float,y:Float,width:Float,size:Float=9f,color:Int=INK,bold:Boolean=false):Float {
        val paint=p(color,size,bold); var yy=y
        value.split("\n").forEach { paragraph ->
            var rest=paragraph
            if(rest.isBlank()){yy+=size+4f;return@forEach}
            while(rest.isNotEmpty()){
                val count=paint.breakText(rest,true,width,null).coerceAtLeast(1)
                var cut=count
                if(count<rest.length){val space=rest.substring(0,count).lastIndexOf(' ');if(space>count/2)cut=space}
                c.drawText(rest.substring(0,cut).trim(),x,yy,paint)
                rest=rest.substring(cut).trimStart()
                yy+=size+5f
            }
        }
        return yy
    }
    private fun header(c:Canvas,title:String,subtitle:String) {
        c.drawColor(WHITE);c.drawRect(0f,0f,W.toFloat(),8f,p(TEAL,1f))
        t(c,"PLAN MASTER 3D",L,34f,9f,TEAL,true);t(c,title,L,66f,21f,INK,true)
        if(subtitle.isNotBlank())t(c,subtitle,L,86f,10f,MUTED)
        line(c,103f)
    }
    private fun footer(c:Canvas,n:Int) {
        line(c,805f)
        t(c,"PLAN MASTER 3D  •  " + SimpleDateFormat("dd.MM.yyyy",Locale("ru")).format(Date()),L,824f,8f,MUTED)
        t(c,n.toString(),R-12f,824f,8f,MUTED)
    }
    private fun cover(c:Canvas,d:PdfReportData,mode:PdfReportMode) {
        c.drawColor(WHITE);c.drawRect(0f,0f,W.toFloat(),330f,p(INK,1f));c.drawRect(0f,0f,W.toFloat(),9f,p(TEAL,1f))
        t(c,"PLAN MASTER 3D",L,66f,13f,0xFF78D5DC.toInt(),true)
        t(c,if(mode==PdfReportMode.PROPOSAL)"КОММЕРЧЕСКОЕ" else "ТЕХНИЧЕСКИЙ",L,146f,27f,WHITE,true)
        t(c,if(mode==PdfReportMode.PROPOSAL)"ПРЕДЛОЖЕНИЕ" else "ОТЧЁТ",L,181f,27f,WHITE,true)
        wrap(c,d.projectName,L,232f,500f,19f,WHITE,true)
        t(c,SimpleDateFormat("d MMMM yyyy",Locale("ru")).format(Date(d.createdAt)),L,292f,10f,0xFFB7C6D8.toInt())
        t(c,"ОБЪЕКТ В ЦИФРАХ",L,386f,10f,TEAL,true)
        val cards=listOf("ПОМЕЩЕНИЯ" to d.rooms.size.toString(),"ПЛОЩАДЬ ПОЛА" to fmt(d.floorArea)+" м²","ПЛОЩАДЬ СТЕН" to fmt(d.wallArea)+" м²","ПОЗИЦИЙ СМЕТЫ" to d.estimate.size.toString())
        cards.forEachIndexed { i,pair ->
            val x=L+(i%2)*260f;val y=410f+(i/2)*86f
            c.drawRoundRect(x,y,x+245f,y+70f,9f,9f,p(PALE,1f))
            t(c,pair.first,x+14f,y+24f,8f,MUTED,true);t(c,pair.second,x+14f,y+51f,18f,INK,true)
        }
        t(c,if(mode==PdfReportMode.PROPOSAL)"ИТОГОВАЯ СТОИМОСТЬ" else "ИТОГО ПО СМЕТЕ",L,632f,10f,MUTED,true)
        t(c,money(d.total),L,686f,34f,TEAL,true)
        wrap(c,if(mode==PdfReportMode.PROPOSAL)"Состав и стоимость работ приведены по текущей смете проекта. Объёмы основаны на сохранённых обмерах." else "Документ сформирован из сохранённых данных проекта: геометрии помещений, конструктивных записей, инженерных точек и сметных строк.",L,733f,500f,10f,MUTED)
        t(c,"Сформировано приложением PLAN MASTER 3D",L,817f,8f,MUTED)
    }
    private fun summary(c:Canvas,d:PdfReportData,mode:PdfReportMode) {
        var y=132f
        val rows=listOf("Площадь пола" to (fmt(d.floorArea)+" м²"),"Площадь потолков" to (fmt(d.ceilingArea)+" м²"),"Чистая площадь стен" to (fmt(d.wallArea)+" м²"),"Площадь проёмов" to (fmt(d.openingArea)+" м²"),"Конструктивные записи" to d.construction.size.toString(),"Инженерные точки" to d.engineeringPoints.size.toString())
        rows.forEach { row ->
            c.drawRoundRect(L,y,R,y+38f,6f,6f,p(SOFT,1f))
            t(c,row.first,L+12f,y+24f,10f,MUTED);t(c,row.second,R-135f,y+24f,11f,INK,true);y+=46f
        }
        y+=12f;t(c,"СОСТАВ ПРОЕКТА",L,y,10f,TEAL,true);y+=24f
        d.rooms.forEachIndexed { i,room ->
            t(c,(i+1).toString()+". "+room.name,L,y,10f,INK,true)
            t(c,"Пол "+fmt(room.floor)+" м² • Стены "+fmt(room.walls)+" м² • Высота "+fmt(room.height)+" м",L+18f,y+16f,9f,MUTED);y+=42f
        }
        y+=8f;t(c,if(mode==PdfReportMode.PROPOSAL)"СТОИМОСТЬ ПРЕДЛОЖЕНИЯ" else "ИТОГО ПО ТЕКУЩЕЙ СМЕТЕ",L,y,10f,MUTED,true)
        t(c,money(d.total),L,y+34f,22f,TEAL,true)
        wrap(c,"Расценки и суммы перенесены из сметных строк проекта без подстановки отсутствующих цен.",L,y+62f,500f,9f,MUTED)
    }
    private fun plan(c:Canvas,room:Room,index:Int,points:List<EngineeringPoint>) {
        val v=room.vertices;val minX=v.minOf{it.x};val maxX=v.maxOf{it.x};val minY=v.minOf{it.y};val maxY=v.maxOf{it.y}
        val spanX=(maxX-minX).coerceAtLeast(0.01);val spanY=(maxY-minY).coerceAtLeast(0.01)
        val scale=min(420f/spanX.toFloat(),420f/spanY.toFloat())
        val ox=W/2f-((minX+maxX)/2f).toFloat()*scale;val oy=380f-((minY+maxY)/2f).toFloat()*scale
        fun pt(a:Vec2)=android.graphics.PointF(ox+a.x.toFloat()*scale,oy+a.y.toFloat()*scale)
        val path=Path()
        v.forEachIndexed{i,a->val q=pt(a);if(i==0)path.moveTo(q.x,q.y)else path.lineTo(q.x,q.y)}
        path.close();c.drawPath(path,p(PALE,1f).apply{style=Paint.Style.FILL});c.drawPath(path,p(INK,2.5f).apply{style=Paint.Style.STROKE})
        v.forEachIndexed { i,a ->
            val b=v[(i+1)%v.size];val pa=pt(a);val pb=pt(b);c.drawCircle(pa.x,pa.y,3.5f,p(TEAL,1f))
            t(c,fmt(a.distanceTo(b))+" м",(pa.x+pb.x)/2f+4f,(pa.y+pb.y)/2f-5f,8f,MUTED,true)
            val length=max(0.001f,a.distanceTo(b).toFloat());val ux=(pb.x-pa.x)/length;val uy=(pb.y-pa.y)/length
            room.openingsByWall[i].orEmpty().forEach { o ->
                val start=o.offsetFromStart.toFloat()*scale;val end=(o.offsetFromStart+o.width).toFloat()*scale
                c.drawLine(pa.x+ux*start,pa.y+uy*start,pa.x+ux*end,pa.y+uy*end,p(TEAL,5f))
                t(c,o.name.take(12),pa.x+ux*(start+end)/2f+3f,pa.y+uy*(start+end)/2f-8f,7f,TEAL,true)
            }
        }
        points.forEach { q->val a=pt(Vec2(q.x,q.y));c.drawCircle(a.x,a.y,6f,p(0xFFCE6A26.toInt(),1f));t(c,q.kind.take(10),a.x+8f,a.y-4f,7f,INK,true) }
        line(c,620f);t(c,"ПАРАМЕТРЫ ПОМЕЩЕНИЯ",L,644f,9f,TEAL,true)
        t(c,"Пол: "+fmt(room.floor)+" м²",L,668f,10f);t(c,"Стены: "+fmt(room.walls)+" м²",L+190f,668f,10f)
        t(c,"Высота: "+fmt(room.height)+" м",L,688f,10f);t(c,"Проёмов: "+room.openingsByWall.values.sumOf{it.size}+" • "+fmt(room.geometry.openingArea)+" м²",L+190f,688f,10f)
        wrap(c,"План построен по сохранённому контуру. Размеры указаны в метрах; проёмы выделены цветом. Это 2D-план, не снимок 3D-сцены.",L,724f,500f,8f,MUTED)
    }
    private fun axonometric(c:Canvas,room:Room) {
        val v=room.vertices
        val minX=v.minOf{it.x};val maxX=v.maxOf{it.x};val minY=v.minOf{it.y};val maxY=v.maxOf{it.y}
        val span=max(0.1,(maxX-minX)+(maxY-minY))
        val height=room.height.coerceAtLeast(0.1)
        val scale=min(360f/span.toFloat(),260f/(0.56f*span.toFloat()+0.8f*height.toFloat()))
        fun raw(point:Vec2,z:Double):android.graphics.PointF {
            val x=(point.x-minX)-(point.y-minY)
            val y=(point.x-minX)+(point.y-minY)
            return android.graphics.PointF(x.toFloat()*scale*0.65f,(y.toFloat()*scale*0.28f)-(z.toFloat()*scale*0.8f))
        }
        val projected=v.flatMap { listOf(raw(it,0.0),raw(it,height)) }
        val minPX=projected.minOf{it.x};val maxPX=projected.maxOf{it.x}
        val minPY=projected.minOf{it.y};val maxPY=projected.maxOf{it.y}
        val dx=W/2f-(minPX+maxPX)/2f
        val dy=430f-(minPY+maxPY)/2f
        fun point(a:Vec2,z:Double):android.graphics.PointF=raw(a,z).let{android.graphics.PointF(it.x+dx,it.y+dy)}
        val shades=listOf(0xFFDCE8ED.toInt(),0xFFE8EFF2.toInt(),0xFFCFDDE3.toInt(),0xFFE1EAEE.toInt())
        v.indices.forEach { i->
            val j=(i+1)%v.size
            val a=point(v[i],0.0);val b=point(v[j],0.0);val cTop=point(v[j],height);val dTop=point(v[i],height)
            val face=Path().apply{moveTo(a.x,a.y);lineTo(b.x,b.y);lineTo(cTop.x,cTop.y);lineTo(dTop.x,dTop.y);close()}
            c.drawPath(face,p(shades[i%shades.size],1f).apply{style=Paint.Style.FILL})
            c.drawPath(face,p(INK,1.4f).apply{style=Paint.Style.STROKE})
        }
        val roof=Path()
        v.forEachIndexed{i,a->val q=point(a,height);if(i==0)roof.moveTo(q.x,q.y)else roof.lineTo(q.x,q.y)}
        roof.close();c.drawPath(roof,p(PALE,1f).apply{style=Paint.Style.FILL})
        c.drawPath(roof,p(INK,1.6f).apply{style=Paint.Style.STROKE})
        v.indices.forEach { i->
            val a=point(v[i],0.0);val b=point(v[i],height)
            c.drawLine(a.x,a.y,b.x,b.y,p(INK,1.2f))
        }
        line(c,690f)
        t(c,"ОБЪЁМНЫЕ ПАРАМЕТРЫ",L,714f,9f,TEAL,true)
        t(c,"Высота стен: "+fmt(room.height)+" м",L,738f,10f)
        t(c,"Периметр: "+fmt(room.perimeter)+" м",L+190f,738f,10f)
        wrap(c,"Объёмная схема построена по фактическому контуру и высоте помещения. Она показывает геометрию объёма; дверные и оконные вырезы не прорисованы на этой схеме и отдельно обозначены на 2D-плане.",L,765f,500f,8f,MUTED)
    }

    private fun construction(c:Canvas,d:PdfReportData) {
        var y=132f;t(c,"Тип",L,y,8f,MUTED,true);t(c,"Материал и параметры",L+140f,y,8f,MUTED,true);t(c,"Кол-во",R-70f,y,8f,MUTED,true);y+=16f;line(c,y)
        d.construction.forEach { item->
            if(y>750f)return@forEach
            val room=d.rooms.getOrNull(item.roomIndex)?.name ?: "Помещение "+(item.roomIndex+1)
            t(c,item.kind.take(22),L,y+14f,8f,INK,true)
            wrap(c,room+" • "+item.material+" • L "+fmt(item.lengthM)+" м • H "+fmt(item.heightM)+" м • t "+fmt(item.thicknessMm)+" мм",L+140f,y+14f,240f,8f,MUTED)
            t(c,fmt(item.quantity),R-65f,y+14f,9f,INK,true);y+=38f;line(c,y)
        }
        wrap(c,"Параметры и количества перенесены из конструктивных записей проекта. Перед закупкой материалов проверьте фактические условия объекта.",L,min(y+22f,760f),500f,8f,MUTED)
    }
    private fun engineering(c:Canvas,d:PdfReportData) {
        var y=132f;t(c,"Точка",L,y,8f,MUTED,true);t(c,"Помещение",L+155f,y,8f,MUTED,true);t(c,"Координаты",R-115f,y,8f,MUTED,true);y+=16f;line(c,y)
        d.engineeringPoints.forEach { point->
            if(y>755f)return@forEach
            t(c,point.kind,L,y+14f,9f,INK,true);t(c,d.rooms.getOrNull(point.roomIndex)?.name ?: "—",L+155f,y+14f,9f)
            t(c,"X "+fmt(point.x)+" / Y "+fmt(point.y)+" м",R-115f,y+14f,8f,MUTED)
            if(point.note.isNotBlank())t(c,point.note.take(65),L+155f,y+28f,7f,MUTED)
            y+=if(point.note.isBlank())28f else 40f;line(c,y)
        }
    }
    private fun estimate(c:Canvas,d:PdfReportData,from:Int,to:Int,last:Boolean) {
        val qtyX=365f;val priceX=438f;val sumX=505f;var y=132f
        c.drawRect(L,y-13f,R,y+8f,p(PALE,1f))
        t(c,"Наименование",L+7f,y+1f,8f,INK,true);t(c,"Кол-во",qtyX,y+1f,8f,INK,true);t(c,"Цена",priceX,y+1f,8f,INK,true);t(c,"Сумма",sumX,y+1f,8f,INK,true);y+=25f
        for(i in from until to){
            val item=d.estimate[i]
            wrap(c,(i+1).toString()+". "+item.name,L+4f,y,235f,7.5f,INK)
            t(c,fmt(item.qty)+" "+item.unit,qtyX,y,7.5f);t(c,money(item.price),priceX,y,7.5f);t(c,money(item.total),sumX,y,7.5f,INK,true)
            y+=28f;line(c,y,SOFT);y+=4f
        }
        if(last){y+=12f;c.drawRoundRect(L,y,R,y+62f,8f,8f,p(PALE,1f));t(c,"ИТОГО ПО СМЕТЕ",L+14f,y+24f,9f,MUTED,true);t(c,money(d.total),R-170f,y+43f,19f,TEAL,true)}
        wrap(c,"Цены и единицы измерения перенесены из текущей сметы проекта.",L,770f,500f,7.5f,MUTED)
    }
    private fun fmt(v:Double)=String.format(Locale.US,"%.2f",v).trimEnd('0').trimEnd('.')
    private fun money(v:Double)=String.format(Locale("ru","RU"),"%,.0f ₽",v)
}
