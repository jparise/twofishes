package com.foursquare.twofishes.util

import com.foursquare.twofishes._
import com.google.common.geometry.{S2LatLng, S2LatLngRect}
import com.vividsolutions.jts.geom.{Coordinate, Geometry, GeometryFactory, Polygon}
import com.vividsolutions.jts.operation.distance.DistanceOp
import com.vividsolutions.jts.util.GeometricShapeFactory

object GeoTools {
  val MetersPerMile: Double = 1609.344
  val RadiusInMeters: Int = 6378100 // Approximately a little less than the Earth's polar radius
  val MetersPerDegreeLatitude: Double = 111111.0
  val MetersPerDegreeLongitude: Double = 110540.0 // I'm assuming this as at the Equator

  def boundingBoxToS2Rect(bounds: GeocodeBoundingBox): S2LatLngRect = {
    S2LatLngRect.fromPointPair(
      S2LatLng.fromDegrees(bounds.ne.lat, bounds.ne.lng),
      S2LatLng.fromDegrees(bounds.sw.lat, bounds.sw.lng)
    )
  }

  def pointToS2LatLng(ll: GeocodePoint): S2LatLng = {
    S2LatLng.fromDegrees(ll.lat, ll.lng)
  }

  def S2LatLngToPoint(ll: S2LatLng): GeocodePoint = {
    GeocodePoint(ll.latDegrees, ll.lngDegrees)
  }

  def pointToGeometry(p: GeocodePoint): Geometry = {
    val geometryFactory = new GeometryFactory()
    val coord = new Coordinate(p.lng, p.lat)
    geometryFactory.createPoint(coord);
  }

  def boundsContains(bounds: GeocodeBoundingBox, ll: GeocodePoint): Boolean = {
    val rect =  boundingBoxToS2Rect(bounds)
    val point = pointToS2LatLng(ll)
    rect.contains(point)
  }

  def boundsIntersect(b1: GeocodeBoundingBox, b2: GeocodeBoundingBox): Boolean = {
    boundingBoxToS2Rect(b1).intersects(boundingBoxToS2Rect(b2))
  }

  // This is incorrect for now
  def makeCircle(ll: GeocodePoint, radiusInMeters: Int): Geometry = {
    val sizeDegrees = radiusInMeters / 111319.9
    val gsf = new GeometricShapeFactory()
    gsf.setSize(sizeDegrees)
    gsf.setNumPoints(100)
    gsf.setCentre(new Coordinate(ll.lng, ll.lat))
    gsf.createCircle()
  }

  def boundsToGeometry(bounds: GeocodeBoundingBox): Geometry = {
    val s2rect = GeoTools.boundingBoxToS2Rect(bounds)
    val geomFactory = new GeometryFactory()
    geomFactory.createLinearRing(Array(
      new Coordinate(s2rect.lng.lo, s2rect.lat.lo),
      new Coordinate(s2rect.lng.hi, s2rect.lat.lo),
      new Coordinate(s2rect.lng.hi, s2rect.lat.hi),
      new Coordinate(s2rect.lng.hi, s2rect.lat.lo),
      new Coordinate(s2rect.lng.lo, s2rect.lat.lo)
    ))
  }

  def distanceFromPointToBounds(p: GeocodePoint, bounds: GeocodeBoundingBox): Double = {
    val point = pointToGeometry(p)
    val geom = boundsToGeometry(bounds)
    DistanceOp.distance(point, geom) * MetersPerDegreeLatitude
  }

  /**
   * @return distance in meters
   */
  def getDistance(geolat1: Double, geolong1: Double, geolat2: Double, geolong2: Double): Int = {
    val theta = geolong1 - geolong2
    val dist = math.sin(math.toRadians(geolat1)) * math.sin(math.toRadians(geolat2)) +
               math.cos(math.toRadians(geolat1)) * math.cos(math.toRadians(geolat2)) * math.cos(math.toRadians(theta))
    (RadiusInMeters * math.acos(dist)).toInt
  }
}
