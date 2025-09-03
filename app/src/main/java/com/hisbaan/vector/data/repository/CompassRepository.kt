//package com.hisbaan.vector.data.repository
//
//import android.content.Context
//import android.hardware.GeomagneticField
//import android.hardware.Sensor
//import android.hardware.SensorEvent
//import android.hardware.SensorEventListener
//import android.hardware.SensorManager
//import android.location.Location
//import kotlinx.coroutines.channels.awaitClose
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.callbackFlow
//
//class CompassRepository(context: Context) : SensorEventListener {
//  private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
//  private val rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
//
//  private val rotationMatrix = FloatArray(9)
//  private val orientationAngles = FloatArray(3)
//
//  fun startCompassUpdates(callback: (Double) -> Unit) { /* ... */ }
//  fun stopCompassUpdates() { /* ... */ }
//
//  fun bearingFlow(): Flow<Double> = callbackFlow {
//    val callback: (Double) -> Unit = { bearing ->
//      trySend(bearing)
//    }
//
//    startCompassUpdates(callback)
//
//    awaitClose {
//      stopCompassUpdates()
//    }
//  }
//
//  fun onStart() {
//    sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_UI)
//  }
//
//  fun onStop() {
//    sensorManager.unregisterListener(this)
//  }
//
//  override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
//    // Do something here if sensor accuracy changes.
//  }
//
//  override fun onSensorChanged(event: SensorEvent) {
//    if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
//      SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
//      SensorManager.getOrientation(rotationMatrix, orientationAngles)
//    }
//  }
//
//  fun getBearingToNorth(location: Location): Double {
//    val azimuthDegrees = Math.toDegrees(orientationAngles[0].toDouble())
//    val magneticNorth = (azimuthDegrees + 360) % 360
//
//    val geoField = GeomagneticField(
//      location.latitude.toFloat(),
//      location.longitude.toFloat(),
//      location.altitude.toFloat(),
//      System.currentTimeMillis()
//    )
//
//    // Add declination to get true north
//    return (magneticNorth + geoField.declination + 360) % 360
//  }
//}
package com.hisbaan.vector.data.repository

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Singleton

@Singleton
class CompassRepository(context: Context) {
  private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
  private val rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

  fun bearingFlow(): Flow<Float> = callbackFlow {
    val listener = object : SensorEventListener {
      private val rotationMatrix = FloatArray(9)
      private val orientationValues = FloatArray(3)

      override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
          SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

          val remappedRotationMatrix = FloatArray(9)
          SensorManager.remapCoordinateSystem(
            rotationMatrix,
            SensorManager.AXIS_X,
            SensorManager.AXIS_Y, // AXIS_Z for vertical phone placement
            remappedRotationMatrix
          )

          SensorManager.getOrientation(remappedRotationMatrix, orientationValues)

          var azimuthDegrees = Math.toDegrees(orientationValues[0].toDouble()).toFloat()
          azimuthDegrees = (azimuthDegrees + 360) % 360  // normalize to [0, 360)

          trySend(azimuthDegrees)
        }
      }

      override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // ignore
      }
    }

    rotationVectorSensor?.also { sensor ->
      sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
    }

    awaitClose {
      sensorManager.unregisterListener(listener)
    }
  }
}