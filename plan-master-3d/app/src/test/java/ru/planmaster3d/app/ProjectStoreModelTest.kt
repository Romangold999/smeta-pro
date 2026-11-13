package ru.planmaster3d.app

import org.junit.Assert.*
import org.junit.Test

class ProjectStoreModelTest {
 @Test fun projectModelsRetainPolygonAndOpenings() {
  val room=RoomGeometry("Нестандартная",listOf(Vec2(0.0,0.0),Vec2(4.0,0.0),Vec2(3.0,2.0),Vec2(0.0,2.0)),
   wallHeights=listOf(2.7,2.7,2.5,2.7),
   openingsByWall=mapOf(0 to listOf(Opening("w1","Окно",1.2,1.4,0.8,0.2,true))))
  val m=GeometryEngine.metrics(room)
  assertEquals(7.0,m.floorArea,1e-8)
  assertEquals(1.68,m.openingArea,1e-8)
  assertEquals(1,room.openingsByWall[0]?.size)
 }
}
