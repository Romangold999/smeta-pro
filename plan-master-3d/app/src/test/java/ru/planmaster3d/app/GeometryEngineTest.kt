package ru.planmaster3d.app

import org.junit.Assert.*
import org.junit.Test

class GeometryEngineTest {
 @Test fun polygonAreaAndPerimeterForRectangle() {
  val p=listOf(Vec2(0.0,0.0),Vec2(4.0,0.0),Vec2(4.0,3.0),Vec2(0.0,3.0))
  assertEquals(12.0,GeometryEngine.area(p),1e-8)
  assertEquals(14.0,GeometryEngine.perimeter(p),1e-8)
 }
 @Test fun arbitraryPolygonArea() {
  val p=listOf(Vec2(0.0,0.0),Vec2(4.0,0.0),Vec2(3.0,2.0),Vec2(0.0,2.0))
  assertEquals(7.0,GeometryEngine.area(p),1e-8)
 }
 @Test fun detectsSelfIntersection() {
  val p=listOf(Vec2(0.0,0.0),Vec2(2.0,2.0),Vec2(0.0,2.0),Vec2(2.0,0.0))
  assertTrue(GeometryEngine.selfIntersects(p))
 }
 @Test fun closureResidualIsNotSilentlyAdjusted() {
  val result=GeometryEngine.closure(listOf(Vec2(4.0,0.0),Vec2(0.0,3.0),Vec2(-3.0,0.0)))
  assertEquals(1.0,result.residual,1e-8)
  assertFalse(result.closed)
 }
 @Test fun openingAndRevealsAreCalculatedSeparately() {
  val room=RoomGeometry("Test",listOf(Vec2(0.0,0.0),Vec2(4.0,0.0),Vec2(4.0,3.0),Vec2(0.0,3.0)),
   openingsByWall=mapOf(0 to listOf(Opening("d","Door",0.9,2.1,depth=0.15))))
  val m=GeometryEngine.metrics(room,2.7)
  assertEquals(37.8,m.grossWallArea,1e-8)
  assertEquals(1.89,m.openingArea,1e-8)
  assertEquals(0.7425,m.revealArea,1e-8)
 }
 @Test fun materialPackagesRoundUp() {
  val r=MaterialCalculator.calculate(20.0,1.2,25.0,10.0,10.0,true)
  assertEquals(10.56,r.required,1e-8)
  assertEquals(1,r.packages)
 }
 @Test fun layoutCountsRowsAndColumns() {
  val r=LayoutCalculator.grid(4000.0,5000.0,600.0,600.0,10.0)
  assertEquals(7,r.rows)
  assertEquals(9,r.columns)
  assertEquals(63,r.pieces)
 }
}
