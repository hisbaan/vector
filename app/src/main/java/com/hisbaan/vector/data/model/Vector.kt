package com.hisbaan.vector.data.model

import android.location.Location
import com.hisbaan.vector.logi
import java.lang.Math.toDegrees
import kotlin.math.acos
import kotlin.math.pow

data class Vector(
  val x: Double,
  var y: Double,
) {
  fun degreesEastOfNorth(): Double {
    val northVector = Vector(0.toDouble(), 1.toDouble())
    val radians = acos(this.dot(northVector) / this.magnitude())
    val degrees = toDegrees(radians) + if (this.x < 0) 180.toDouble() else 0.toDouble()
    return degrees
  }

  fun magnitude(): Double {
    return this.x.pow(2) + this.y.pow(2)
  }

  fun dot(other: Vector): Double {
    return this.x * other.x + this.y * other.y
  }

  operator fun plus(other: Vector): Vector {
    return Vector(this.x + other.x, this.y + other.y)
  }

  operator fun minus(other: Vector): Vector {
    return Vector(this.x - other.x, this.y - other.y)
  }

  companion object {
    fun fromLatLong(latitude: Double, longitude: Double): Vector {
      return Vector(longitude, latitude)
    }

    fun fromLocation(location: Location): Vector {
      return Vector(location.longitude, location.latitude)
    }
  }
};

