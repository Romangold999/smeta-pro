package ru.planmaster3d.app

import kotlin.math.*

/** Точная двумерная геометрия. Единицы координат — метры. */
data class Vec2(val x: Double, val y: Double) {
    operator fun plus(o: Vec2)=Vec2(x+o.x,y+o.y)
    operator fun minus(o: Vec2)=Vec2(x-o.x,y-o.y)
    operator fun times(k: Double)=Vec2(x*k,y*k)
    fun length()=hypot(x,y)
    fun distanceTo(o:Vec2)=(this-o).length()
}
data class Opening(
    val id:String,
    val name:String,
    val width:Double,
    val height:Double,
    val sillHeight:Double=0.0,
    val depth:Double=0.0,
    val hasSill:Boolean=false,
    val offsetFromStart:Double=0.0,
    val kind:String="Проём"
) {
    init { require(width.isFinite() && height.isFinite() && sillHeight.isFinite() && depth.isFinite() && offsetFromStart.isFinite() && width>0 && height>0 && sillHeight>=0 && depth>=0 && offsetFromStart>=0) }
    val faceArea get()=width*height
    /** Два боковых откоса + верхний; подоконник учитывается отдельно по флагу. */
    fun revealArea():Double = depth*(2*height+width+(if(hasSill) width else 0.0))
}
data class WallMeasure(
    val start:Vec2,
    val end:Vec2,
    val heightStart:Double,
    val heightEnd:Double=heightStart,
    val thickness:Double=0.0,
    val openings:List<Opening> = emptyList(),
    val label:String=""
) {
    val length get()=start.distanceTo(end)
    val grossArea get()=length*(heightStart+heightEnd)/2.0
    val openingArea get()=openings.sumOf{it.faceArea}
    val netArea get()=(grossArea-openingArea).coerceAtLeast(0.0)
    val revealArea get()=openings.sumOf{it.revealArea()}
}
data class RoomGeometry(
    val name:String,
    val vertices:List<Vec2>,
    val wallHeights:List<Double> = emptyList(),
    val wallThicknesses:List<Double> = emptyList(),
    val openingsByWall:Map<Int,List<Opening>> = emptyMap()
)
data class ClosureResult(val dx:Double,val dy:Double,val residual:Double,val closed:Boolean)
data class RoomMetrics(
    val floorArea:Double,
    val perimeter:Double,
    val grossWallArea:Double,
    val netWallArea:Double,
    val ceilingArea:Double,
    val openingArea:Double,
    val revealArea:Double,
    val walls:List<WallMeasure>,
    val selfIntersects:Boolean
)
object GeometryEngine {
    private const val EPS=1e-8
    fun signedArea(points:List<Vec2>):Double {
        if(points.size<3) return 0.0
        var sum=0.0
        for(i in points.indices){val a=points[i];val b=points[(i+1)%points.size];sum+=a.x*b.y-b.x*a.y}
        return sum/2.0
    }
    fun area(points:List<Vec2>)=abs(signedArea(points))
    fun containsPoint(polygon:List<Vec2>,point:Vec2):Boolean {
        if(polygon.size<3 || selfIntersects(polygon)) return false
        var inside=false
        for(i in polygon.indices) {
            val a=polygon[i];val b=polygon[(i+1)%polygon.size]
            val cross=(b.x-a.x)*(point.y-a.y)-(b.y-a.y)*(point.x-a.x)
            if(abs(cross)<=EPS && onSegment(a,b,point)) return true
            if((a.y>point.y)!=(b.y>point.y)) {
                val x=a.x+(point.y-a.y)*(b.x-a.x)/(b.y-a.y)
                if(point.x<x) inside=!inside
            }
        }
        return inside
    }
    fun perimeter(points:List<Vec2>):Double {
        if(points.size<2) return 0.0
        return points.indices.sumOf{points[it].distanceTo(points[(it+1)%points.size])}
    }
    fun selfIntersects(p:List<Vec2>):Boolean {
        if(p.size<4)return false
        for(i in p.indices) for(j in i+1 until p.size) {
            val i2=(i+1)%p.size; val j2=(j+1)%p.size
            if(i==j || i2==j || j2==i) continue
            if(intersects(p[i],p[i2],p[j],p[j2])) return true
        }
        return false
    }
    private fun orient(a:Vec2,b:Vec2,c:Vec2)=(b.x-a.x)*(c.y-a.y)-(b.y-a.y)*(c.x-a.x)
    private fun onSegment(a:Vec2,b:Vec2,p:Vec2)=p.x>=min(a.x,b.x)-EPS&&p.x<=max(a.x,b.x)+EPS&&p.y>=min(a.y,b.y)-EPS&&p.y<=max(a.y,b.y)+EPS
    private fun intersects(a:Vec2,b:Vec2,c:Vec2,d:Vec2):Boolean {
        val o1=orient(a,b,c);val o2=orient(a,b,d);val o3=orient(c,d,a);val o4=orient(c,d,b)
        if(o1*o2 < -EPS && o3*o4 < -EPS)return true
        return (abs(o1)<=EPS&&onSegment(a,b,c))||(abs(o2)<=EPS&&onSegment(a,b,d))||(abs(o3)<=EPS&&onSegment(c,d,a))||(abs(o4)<=EPS&&onSegment(c,d,b))
    }
    /** Проверяет привязку проёмов к стенам, габариты и взаимные пересечения. */
    fun validateOpenings(room:RoomGeometry,defaultHeight:Double=2.7):List<String> {
        val errors=mutableListOf<String>()
        room.openingsByWall.forEach { (wallIndex, openings) ->
            if(wallIndex !in room.vertices.indices) {
                errors.add("Проёмы привязаны к отсутствующей стене №${wallIndex+1}")
                return@forEach
            }
            val start=room.vertices[wallIndex]
            val end=room.vertices[(wallIndex+1)%room.vertices.size]
            val length=start.distanceTo(end)
            val next=(wallIndex+1)%room.vertices.size
            val maxHeight=min(room.wallHeights.getOrElse(wallIndex){defaultHeight},room.wallHeights.getOrElse(next){defaultHeight})
            openings.forEach { opening ->
                if(opening.offsetFromStart+opening.width>length+EPS) errors.add("${opening.name}: ширина выходит за пределы стены №${wallIndex+1}")
                if(opening.sillHeight+opening.height>maxHeight+EPS) errors.add("${opening.name}: высота выходит за пределы стены №${wallIndex+1}")
            }
            val sorted=openings.sortedBy{it.offsetFromStart}
            for(i in sorted.indices) for(j in i+1 until sorted.size) {
                val first=sorted[i];val second=sorted[j]
                if(second.offsetFromStart>=first.offsetFromStart+first.width-EPS) break
                errors.add("Проёмы «${first.name}» и «${second.name}» пересекаются на стене №${wallIndex+1}")
            }
        }
        return errors
    }

    fun metrics(room:RoomGeometry,defaultHeight:Double=2.7):RoomMetrics {
        require(room.vertices.size>=3){"Для помещения нужны минимум три вершины"}
        require(room.wallHeights.isEmpty()||room.wallHeights.size==room.vertices.size)
        require(room.wallThicknesses.isEmpty()||room.wallThicknesses.size==room.vertices.size)
        val walls=room.vertices.indices.map{i->
            val j=(i+1)%room.vertices.size
            WallMeasure(room.vertices[i],room.vertices[j],
                room.wallHeights.getOrElse(i){defaultHeight},
                room.wallHeights.getOrElse(j){defaultHeight},
                room.wallThicknesses.getOrElse(i){0.0},
                room.openingsByWall[i].orEmpty(), "Стена ${i+1}")
        }
        val gross=walls.sumOf{it.grossArea}
        val openings=walls.sumOf{it.openingArea}
        return RoomMetrics(area(room.vertices),perimeter(room.vertices),gross,
            walls.sumOf{it.netArea},area(room.vertices),openings,walls.sumOf{it.revealArea},
            walls,selfIntersects(room.vertices))
    }
    /** Невязка исходных измеренных векторов до принудительного замыкания контура. */
    fun closure(vectors:List<Vec2>,tolerance:Double=0.01):ClosureResult {
        val dx=vectors.sumOf{it.x};val dy=vectors.sumOf{it.y}
        val residual=hypot(dx,dy)
        return ClosureResult(dx,dy,residual,residual<=tolerance)
    }
    fun diagonal(a:Vec2,b:Vec2)=a.distanceTo(b)
    fun wallLengths(points:List<Vec2>)=points.indices.map{points[it].distanceTo(points[(it+1)%points.size])}

    /** Точка на ребре по расстоянию от его начальной вершины. */
    fun pointOnEdge(points:List<Vec2>,edgeIndex:Int,distanceFromStart:Double):Vec2? {
        if(points.size<2 || edgeIndex !in points.indices || distanceFromStart<0.0) return null
        val a=points[edgeIndex];val b=points[(edgeIndex+1)%points.size];val length=a.distanceTo(b)
        if(length<=EPS || distanceFromStart>length+EPS) return null
        return a+(b-a)*(distanceFromStart.coerceIn(0.0,length)/length)
    }

    data class PolygonSplit(val first:List<Vec2>,val second:List<Vec2>,val originalArea:Double) {
        val combinedArea get()=area(first)+area(second)
    }

    /**
     * Разрезает простой многоугольник хордой, концы которой лежат на его границе.
     * Никакие координаты/размеры автоматически не подгоняются. Для некорректного
     * или выходящего за контур разреза возвращается null.
     */
    fun splitByBoundaryChord(points:List<Vec2>, start:Vec2, end:Vec2):PolygonSplit? {
        if(points.size<3 || selfIntersects(points) || start.distanceTo(end)<=EPS) return null
        val scale=max(1.0, perimeter(points))
        val tol=1e-7*scale
        fun edgeParameter(a:Vec2,b:Vec2,p:Vec2):Double? {
            val d=b-a
            val len2=d.x*d.x+d.y*d.y
            if(len2<=EPS) return null
            val t=((p.x-a.x)*d.x+(p.y-a.y)*d.y)/len2
            if(t < -tol || t > 1.0+tol) return null
            val projected=a+d*t.coerceIn(0.0,1.0)
            return if(projected.distanceTo(p)<=tol) t.coerceIn(0.0,1.0) else null
        }
        fun liesOnBoundary(p:Vec2)=points.indices.any { i->edgeParameter(points[i],points[(i+1)%points.size],p)!=null }
        if(!liesOnBoundary(start)||!liesOnBoundary(end)) return null
        fun inside(p:Vec2):Boolean {
            var hit=false
            for(i in points.indices) {
                val a=points[i];val b=points[(i+1)%points.size]
                if((a.y>p.y)!=(b.y>p.y)) {
                    val x=a.x+(p.y-a.y)*(b.x-a.x)/(b.y-a.y)
                    if(p.x<x) hit=!hit
                }
            }
            return hit
        }
        if(!inside((start+end)*0.5)) return null
        // The chord may touch the boundary only at its two endpoints.
        for(i in points.indices) {
            val a=points[i];val b=points[(i+1)%points.size]
            val t1=edgeParameter(a,b,start)
            val t2=edgeParameter(a,b,end)
            if(t1==null && t2==null && segmentsIntersect(start,end,a,b)) return null
        }
        val boundary=mutableListOf<Vec2>()
        for(i in points.indices) {
            val a=points[i];val b=points[(i+1)%points.size]
            boundary.add(a)
            val inserts=listOfNotNull(edgeParameter(a,b,start)?.let{it to start},edgeParameter(a,b,end)?.let{it to end})
                .filter{it.first>tol && it.first<1.0-tol}
                .distinctBy{it.second}
                .sortedBy{it.first}
            inserts.forEach{if(boundary.last().distanceTo(it.second)>tol) boundary.add(it.second)}
        }
        fun indexOf(p:Vec2)=boundary.indexOfFirst{it.distanceTo(p)<=tol}
        val si=indexOf(start);val ei=indexOf(end)
        if(si<0||ei<0||si==ei) return null
        fun path(from:Int,to:Int):List<Vec2> {
            val out=mutableListOf<Vec2>();var i=from
            while(true){out.add(boundary[i]);if(i==to)break;i=(i+1)%boundary.size;if(out.size>boundary.size+1)return emptyList()}
            return out
        }
        val a=path(si,ei);val b=path(ei,si)
        if(a.size<3||b.size<3) return null
        val areaA=area(a);val areaB=area(b);val original=area(points)
        if(areaA<=tol*tol||areaB<=tol*tol||abs(areaA+areaB-original)>max(1e-6,original*1e-6)) return null
        return PolygonSplit(a,b,original)
    }

    private fun segmentsIntersect(a:Vec2,b:Vec2,c:Vec2,d:Vec2):Boolean {
        fun cross(p:Vec2,q:Vec2,r:Vec2)=(q.x-p.x)*(r.y-p.y)-(q.y-p.y)*(r.x-p.x)
        val ab1=cross(a,b,c);val ab2=cross(a,b,d);val cd1=cross(c,d,a);val cd2=cross(c,d,b)
        return ab1*ab2 < -EPS && cd1*cd2 < -EPS
    }
}
data class MaterialResult(val required:Double,val packages:Int,val packageSize:Double)
object MaterialCalculator {
    /** ratePerM2 — расход на м²; layerMm применяется отдельно для материалов с расходом на мм. */
    fun calculate(area:Double,ratePerM2:Double,packageSize:Double,wastePercent:Double=0.0,layerMm:Double=1.0,perMillimetre:Boolean=false):MaterialResult {
        require(area>=0&&ratePerM2>=0&&packageSize>0&&wastePercent>=0&&layerMm>=0)
        val amount=area*ratePerM2*(if(perMillimetre)layerMm else 1.0)*(1+wastePercent/100.0)
        return MaterialResult(amount,ceil(amount/packageSize).toInt(),packageSize)
    }
}
data class LayoutResult(val rows:Int,val columns:Int,val pieces:Int,val piecesWithReserve:Int,val areaM2:Double,val wasteAreaM2:Double)
object LayoutCalculator {
    /** Раскладка по прямой сетке. Для сложных рисунков и повторного использования обрезков нужен отдельный nesting-алгоритм. */
    fun grid(roomWidthMm:Double,roomLengthMm:Double,itemWidthMm:Double,itemLengthMm:Double,reservePercent:Double=10.0):LayoutResult {
        require(roomWidthMm>0&&roomLengthMm>0&&itemWidthMm>0&&itemLengthMm>0&&reservePercent>=0)
        val a=ceil(roomWidthMm/itemWidthMm).toInt()
        val b=ceil(roomLengthMm/itemLengthMm).toInt()
        val pieces=a*b
        val area=roomWidthMm*roomLengthMm/1_000_000.0
        val itemArea=itemWidthMm*itemLengthMm/1_000_000.0
        val purchased=ceil(pieces*(1+reservePercent/100.0)).toInt()
        return LayoutResult(b,a,pieces,purchased,area,(purchased*itemArea-area).coerceAtLeast(0.0))
    }
}
