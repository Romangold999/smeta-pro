package ru.planmaster3d.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class SavedProject(
    val id:String,
    val name:String,
    val rooms:List<RoomGeometry>,
    val estimate:List<EstimateLine>,
    val updatedAt:Long=System.currentTimeMillis()
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
                list.forEach{o->arr.put(JSONObject().put("id",o.id).put("name",o.name).put("width",o.width).put("height",o.height).put("sillHeight",o.sillHeight).put("depth",o.depth).put("hasSill",o.hasSill))}
                openings.put(index.toString(),arr)
            }
            r.put("openings",openings);rooms.put(r)
        }
        root.put("rooms",rooms)
        val lines=JSONArray()
        project.estimate.forEach{lines.put(JSONObject().put("name",it.name).put("unit",it.unit).put("price",it.price).put("qty",it.qty).put("category",it.category).put("material",it.material))}
        root.put("estimate",lines)
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
                    openings[key.toInt()]=buildList{for(k in 0 until oa.length()){val o=oa.getJSONObject(k);add(Opening(o.getString("id"),o.getString("name"),o.getDouble("width"),o.getDouble("height"),o.optDouble("sillHeight",0.0),o.optDouble("depth",0.0),o.optBoolean("hasSill",false)))}}
                }
                add(RoomGeometry(r.getString("name"),points,heights,thicknesses,openings))
            }
        }
        val estimate=buildList {
            val arr=root.getJSONArray("estimate")
            for(i in 0 until arr.length()){val e=arr.getJSONObject(i);add(EstimateLine(e.getString("name"),e.getString("unit"),e.getDouble("price"),e.getDouble("qty"),e.getString("category"),e.optBoolean("material",false)))}
        }
        SavedProject(root.getString("id"),root.getString("name"),rooms,estimate,root.optLong("updatedAt",0L))
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
