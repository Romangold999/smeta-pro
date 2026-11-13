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
  assertEquals(kotlin.math.sqrt(10.0),result.residual,1e-8)
  assertFalse(result.closed)
 }
 @Test fun openingAndRevealsAreCalculatedSeparately() {
  val room=RoomGeometry("Test",listOf(Vec2(0.0,0.0),Vec2(4.0,0.0),Vec2(4.0,3.0),Vec2(0.0,3.0)),
   openingsByWall=mapOf(0 to listOf(Opening("d","Door",0.9,2.1,depth=0.15))))
  val m=GeometryEngine.metrics(room,2.7)
  assertEquals(37.8,m.grossWallArea,1e-8)
  assertEquals(1.89,m.openingArea,1e-8)
  assertEquals(0.765,m.revealArea,1e-8)
 }

 @Test fun validatesOpeningPositionHeightAndOverlap() {
  val room=RoomGeometry("Test",listOf(Vec2(0.0,0.0),Vec2(4.0,0.0),Vec2(4.0,3.0),Vec2(0.0,3.0)),
   wallHeights=listOf(2.7,2.7,2.7,2.7),
   openingsByWall=mapOf(0 to listOf(
    Opening("a","Door A",1.0,2.0,offsetFromStart=0.5),
    Opening("b","Door B",1.0,2.0,offsetFromStart=1.2),
    Opening("c","Window",1.0,1.0,sillHeight=1.0,offsetFromStart=3.5)
   )))
  val errors=GeometryEngine.validateOpenings(room)
  assertTrue(errors.any{it.contains("пересекаются")})
  assertTrue(errors.any{it.contains("выходит за пределы стены")})
 }
 @Test fun detectsNestedOpeningOverlapNotOnlyAdjacentPairs() {
  val room=RoomGeometry("Test",listOf(Vec2(0.0,0.0),Vec2(6.0,0.0),Vec2(6.0,3.0),Vec2(0.0,3.0)),
   openingsByWall=mapOf(0 to listOf(
    Opening("a","Wide",0.9,2.0,offsetFromStart=0.5),
    Opening("b","Small",0.5,1.0,offsetFromStart=1.0),
    Opening("c","Later",0.7,1.0,offsetFromStart=1.8)
   )))
  val errors=GeometryEngine.validateOpenings(room)
  assertTrue(errors.any{it.contains("Wide")&&it.contains("Later")})
 }
 @Test fun openingRejectsNonFiniteDimensions() {
  assertThrows(IllegalArgumentException::class.java) { Opening("bad","Bad",Double.NaN,2.0) }
 }
 @Test fun openingOffsetDefaultsToWallStartForOldProjects() {
  val opening=Opening("legacy","Legacy",0.9,2.1)
  assertEquals(0.0,opening.offsetFromStart,1e-8)
  assertEquals("Проём",opening.kind)
 }
 @Test fun partitionChordSplitsRectangleAndPreservesArea() {
  val room=listOf(Vec2(0.0,0.0),Vec2(4.0,0.0),Vec2(4.0,3.0),Vec2(0.0,3.0))
  val split=GeometryEngine.splitByBoundaryChord(room,Vec2(2.0,0.0),Vec2(2.0,3.0))
  assertNotNull(split)
  assertEquals(6.0,GeometryEngine.area(split!!.first),1e-8)
  assertEquals(6.0,GeometryEngine.area(split.second),1e-8)
  assertEquals(12.0,split.combinedArea,1e-8)
 }
 @Test fun partitionChordCanStartInsideBoundaryEdges() {
  val room=listOf(Vec2(0.0,0.0),Vec2(4.0,0.0),Vec2(4.0,3.0),Vec2(0.0,3.0))
  val split=GeometryEngine.splitByBoundaryChord(room,Vec2(1.0,0.0),Vec2(3.0,3.0))
  assertNotNull(split)
  assertEquals(12.0,split!!.combinedArea,1e-8)
 }
 @Test fun rejectsPartitionChordWithEndpointOutsideRoom() {
  val room=listOf(Vec2(0.0,0.0),Vec2(4.0,0.0),Vec2(4.0,3.0),Vec2(0.0,3.0))
  assertNull(GeometryEngine.splitByBoundaryChord(room,Vec2(2.0,0.0),Vec2(5.0,3.0)))
 }
 @Test fun rejectsPartitionChordWhoseMidpointIsOutsideConcaveRoom() {
  val room=listOf(Vec2(0.0,0.0),Vec2(4.0,0.0),Vec2(4.0,1.0),Vec2(1.0,1.0),Vec2(1.0,4.0),Vec2(0.0,4.0))
  assertNull(GeometryEngine.splitByBoundaryChord(room,Vec2(4.0,0.5),Vec2(0.0,3.5)))
 }
 @Test fun splitRoomMetricsPreserveFloorAndAddBothPartitionFaces() {
  val room=listOf(Vec2(0.0,0.0),Vec2(4.0,0.0),Vec2(4.0,3.0),Vec2(0.0,3.0))
  val split=GeometryEngine.splitByBoundaryChord(room,Vec2(2.0,0.0),Vec2(2.0,3.0))!!
  val first=GeometryEngine.metrics(RoomGeometry("A",split.first,List(split.first.size){2.7}))
  val second=GeometryEngine.metrics(RoomGeometry("B",split.second,List(split.second.size){2.7}))
  assertEquals(12.0,first.floorArea+second.floorArea,1e-8)
  assertEquals(20.0,first.perimeter+second.perimeter,1e-8)
  assertEquals(54.0,(first.grossWallArea+second.grossWallArea),1e-8)
 }
 @Test fun pointOnSlantedPolygonEdgeUsesActualGeometry() {
  val p=listOf(Vec2(1.0,2.0),Vec2(5.0,5.0),Vec2(4.0,8.0))
  val edgeLength=p[0].distanceTo(p[1])
  val midpoint=GeometryEngine.pointOnEdge(p,0,edgeLength/2.0)
  assertNotNull(midpoint)
  assertEquals(3.0,midpoint!!.x,1e-8)
  assertEquals(3.5,midpoint.y,1e-8)
  assertNull(GeometryEngine.pointOnEdge(p,0,edgeLength+1.0))
 }
 @Test fun materialPackagesRoundUp() {
  val r=MaterialCalculator.calculate(20.0,1.2,25.0,10.0,10.0,true)
  assertEquals(264.0,r.required,1e-8)
  assertEquals(11,r.packages)
 }
 @Test fun layoutCountsRowsAndColumns() {
  val r=LayoutCalculator.grid(4000.0,5000.0,600.0,600.0,10.0)
  assertEquals(9,r.rows)
  assertEquals(7,r.columns)
  assertEquals(63,r.pieces)
 }
}
