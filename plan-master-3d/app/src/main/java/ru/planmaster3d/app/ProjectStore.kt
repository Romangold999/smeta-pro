package ru.planmaster3d.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class ConstructionRecord(
    val id:String,
    val kind:String,
    val roomIndex:Int,
    val material:String,
    val wallIndex:Int=-1,
    val startX:Double=0.0,
    val startY:Double=0.0,
    val endX:Double=0.0,
    val endY:Double=0.0,
    val lengthM:Double,
    val heightM:Double,
    val thicknessMm:Double,
    val openingsAreaM2:Double=0.0,
    val reservePercent:Double=0.0,
    val layersPerSide:Int=1,
    val sides:Int=2,
    val studSpacingMm:Int=600,
    val insulation:Boolean=false,
    val densityKgM3:Double=0.0,
    val quantity:Double=0.0,
    val regionPoints:List<Vec2> = emptyList(),
    val createdAt:Long=System.currentTimeMillis()
)
data class SavedProject(
    val id:String,
    val name:String,
    val rooms:List<RoomGeometry>,
    val estimate:List<EstimateLine>,
    val updatedAt:Long=System.currentTimeMillis(),
    val construction:List<ConstructionRecord> = emptyList()
)
data class PriceBookVersion(val id:String,val name:String,val createdAt:Long,val items:List<CatalogWork>)
data class PriceBook(val activeVersionId:String,val versions:List<PriceBookVersion>)

/** Local project/pricelist storage. No network and no account dependency. */
class ProjectStore(context:Context) {
    private val prefs=context.getSharedPreferences("plan_master_3d_store_v1",Context.MODE_PRIVATE)
    fun saveProject(project:SavedProject) {
        val root=JSONObject().put("id",project.id).put("name",project.name).put("updatedAt",project.updatedAt)
        val rooms=JSONArray()
        project.rooms.forEach { room ->
            val r=JSONObject().put("name",room.name)
            val vertices=JSONArray()
            room.vertices.forEach{vertices.put(JSONObject().put("x",it.x).put("y",it.y))}
            r.put("vertices",vertices)
            r.put("heights",JSONArray(room.wallHeights))
            r.put("thicknesses",JSONArray(room.wallThicknesses))
            val openings=JSONObject()
            room.openingsByWall.forEach{(index,list)->
                val arr=JSONArray()
                list.forEach{o->arr.put(JSONObject().put("id",o.id).put("name",o.name).put("width",o.width).put("height",o.height).put("sillHeight",o.sillHeight).put("depth",o.depth).put("hasSill",o.hasSill).put("offsetFromStart",o.offsetFromStart).put("kind",o.kind))}
                openings.put(index.toString(),arr)
            }
            r.put("openings",openings);rooms.put(r)
        }
        root.put("rooms",rooms)
        val lines=JSONArray()
        project.estimate.forEach{lines.put(JSONObject().put("name",it.name).put("unit",it.unit).put("price",it.price).put("qty",it.qty).put("category",it.category).put("material",it.material))}
        root.put("estimate",lines)
        val construction=JSONArray()
        project.construction.forEach { e ->
            construction.put(JSONObject()
                .put("id",e.id).put("kind",e.kind).put("roomIndex",e.roomIndex)
                .put("material",e.material).put("wallIndex",e.wallIndex)
                .put("startX",e.startX).put("startY",e.startY)
                .put("endX",e.endX).put("endY",e.endY)
                .put("lengthM",e.lengthM).put("heightM",e.heightM)
                .put("thicknessMm",e.thicknessMm).put("openingsAreaM2",e.openingsAreaM2)
                .put("reservePercent",e.reservePercent).put("layersPerSide",e.layersPerSide)
                .put("sides",e.sides).put("studSpacingMm",e.studSpacingMm)
                .put("insulation",e.insulation).put("densityKgM3",e.densityKgM3)
                .put("quantity",e.quantity).put("regionPoints",JSONArray().also{arr->e.regionPoints.forEach{arr.put(JSONObject().put("x",it.x).put("y",it.y))}}).put("createdAt",e.createdAt))
        }
        root.put("construction",construction)
        prefs.edit().putString("project_${project.id}",root.toString()).apply()
    }
    fun loadProject(id:String):SavedProject? = runCatching {
        val root=JSONObject(prefs.getString("project_$id",null) ?: return null)
        val rooms=buildList {
            val arr=root.getJSONArray("rooms")
            for(i in 0 until arr.length()){
                val r=arr.getJSONObject(i)
                val vertices=r.getJSONArray("vertices")
                val points=buildList { for(j in 0 until vertices.length()){val p=vertices.getJSONObject(j);add(Vec2(p.getDouble("x"),p.getDouble("y")))} }
                val heights=r.optJSONArray("heights")?.toDoubleList().orEmpty()
                val thicknesses=r.optJSONArray("thicknesses")?.toDoubleList().orEmpty()
                val openings=mutableMapOf<Int,List<Opening>>()
                val objectOpenings=r.optJSONObject("openings") ?: JSONObject()
                objectOpenings.keys().forEach{key->
                    val oa=objectOpenings.getJSONArray(key)
                    openings[key.toInt()]=buildList{for(k in 0 until oa.length()){val o=oa.getJSONObject(k);add(Opening(o.getString("id"),o.getString("name"),o.getDouble("width"),o.getDouble("height"),o.optDouble("sillHeight",0.0),o.optDouble("depth",0.0),o.optBoolean("hasSill",false),o.optDouble("offsetFromStart",0.0),o.optString("kind","Проём")))}}
                }
                add(RoomGeometry(r.getString("name"),points,heights,thicknesses,openings))
            }
        }
        val estimate=buildList {
            val arr=root.getJSONArray("estimate")
            for(i in 0 until arr.length()){val e=arr.getJSONObject(i);add(EstimateLine(e.getString("name"),e.getString("unit"),e.getDouble("price"),e.getDouble("qty"),e.getString("category"),e.optBoolean("material",false)))}
        }
        val construction=buildList {
            val arr=root.optJSONArray("construction") ?: JSONArray()
            for(i in 0 until arr.length()){
                val e=arr.getJSONObject(i)
                add(ConstructionRecord(
                    id=e.getString("id"),kind=e.getString("kind"),roomIndex=e.optInt("roomIndex",0),
                    material=e.optString("material",""),wallIndex=e.optInt("wallIndex",-1),
                    startX=e.optDouble("startX",0.0),startY=e.optDouble("startY",0.0),
                    endX=e.optDouble("endX",0.0),endY=e.optDouble("endY",0.0),
                    lengthM=e.optDouble("lengthM",0.0),heightM=e.optDouble("heightM",0.0),
                    thicknessMm=e.optDouble("thicknessMm",0.0),openingsAreaM2=e.optDouble("openingsAreaM2",0.0),
                    reservePercent=e.optDouble("reservePercent",0.0),layersPerSide=e.optInt("layersPerSide",1),
                    sides=e.optInt("sides",2),studSpacingMm=e.optInt("studSpacingMm",600),
                    insulation=e.optBoolean("insulation",false),densityKgM3=e.optDouble("densityKgM3",0.0),
                    quantity=e.optDouble("quantity",0.0),regionPoints=e.optJSONArray("regionPoints")?.let{arr->buildList{for(k in 0 until arr.length()){val point=arr.getJSONObject(k);add(Vec2(point.getDouble("x"),point.getDouble("y")))}}}.orEmpty(),createdAt=e.optLong("createdAt",0L)
                ))
            }
        }
        SavedProject(root.getString("id"),root.getString("name"),rooms,estimate,root.optLong("updatedAt",0L),construction)
    }.getOrNull()
    fun deleteProject(id:String){prefs.edit().remove("project_$id").apply()}
    fun listProjectIds():List<String> = prefs.all.keys.filter{it.startsWith("project_")}.map{it.removePrefix("project_")}
    fun savePriceBook(book:PriceBook) {
        val versions=JSONArray()
        book.versions.forEach{v->
            val items=JSONArray()
            v.items.forEach{w->items.put(JSONObject().put("name",w.n).put("price",w.p).put("unit",w.u).put("category",w.c).put("from",w.from))}
            versions.put(JSONObject().put("id",v.id).put("name",v.name).put("createdAt",v.createdAt).put("items",items))
        }
        prefs.edit().putString("price_book",JSONObject().put("active",book.activeVersionId).put("versions",versions).toString()).apply()
    }
    fun loadPriceBook():PriceBook?=runCatching{
        val root=JSONObject(prefs.getString("price_book",null)?:return null)
        val versions=buildList{
            val arr=root.getJSONArray("versions")
            for(i in 0 until arr.length()){
                val v=arr.getJSONObject(i);val items=v.getJSONArray("items")
                add(PriceBookVersion(v.getString("id"),v.getString("name"),v.getLong("createdAt"),buildList{
                    for(j in 0 until items.length()){val w=items.getJSONObject(j);add(CatalogWork(w.getString("name"),w.getDouble("price"),w.getString("unit"),w.getString("category"),w.optBoolean("from",false)))}
                }))
            }
        }
        PriceBook(root.getString("active"),versions)
    }.getOrNull()
}
private fun JSONArray.toDoubleList()=buildList{for(i in 0 until length())add(getDouble(i))}
