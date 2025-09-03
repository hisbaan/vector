package com.hisbaan.vector.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlin.math.roundToInt

class Preferences(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences(
      "com.hisbaan.vector.settings",
      Context.MODE_PRIVATE
    )

  companion object {
    private const val KEY_GPS_REFRESH_SPEED = "gps_refresh_speed"
    private const val KEY_GPS_BEARING_OVERRIDE_SPEED = "gps_bearing_override_speed"
    private const val KEY_UNITS = "units"
  }

  enum class GpsRefreshSpeed() {
    LOW, MEDIUM, HIGH
  }

  var gpsRefreshSpeed: GpsRefreshSpeed
    get() = GpsRefreshSpeed.valueOf(
      prefs.getString(
        KEY_GPS_REFRESH_SPEED,
        GpsRefreshSpeed.MEDIUM.toString()
      ) ?: GpsRefreshSpeed.MEDIUM.toString()
    )
    set(value) = prefs.edit { putString(KEY_GPS_REFRESH_SPEED, value.toString()) }

  var gpsBearingOverrideSpeed: Float
    get() = prefs.getFloat(KEY_GPS_BEARING_OVERRIDE_SPEED, 5f)
    set(value) = prefs.edit { putFloat(KEY_GPS_BEARING_OVERRIDE_SPEED, value) }

  enum class Units() {
    METRIC {
      override fun getSpeedUnit(metersPerSecond: Float): Double {
        return metersPerSecond * 3.6
      }

      override fun formatDistance(meters: Float): String {
        return if (meters > 1000) "${((meters / 100.0).roundToInt() / 10.0)} km" else "${meters.roundToInt()} m"
      }

      override val speedUnitShort: String = "km/h"
      override val speedUnitLong: String = "kilometers per hour"
    },
    IMPERIAL {
      override fun getSpeedUnit(metersPerSecond: Float): Double {
        return metersPerSecond * 2.23694
      }

      override fun formatDistance(meters: Float): String {
        val metersPerMile = 1609.344f
        val metersPerFoot = 0.3048f

        return if (meters >= metersPerMile) {
          "${(((meters / metersPerMile) * 10.0).roundToInt() / 10.0)} mi"
        } else {
          "${(meters / metersPerFoot).roundToInt()} ft"
        }
      }

      override val speedUnitShort: String = "mph"
      override val speedUnitLong: String = "miles per hour"
    };

    abstract fun getSpeedUnit(metersPerSecond: Float): Double
    abstract fun formatDistance(meters: Float): String

    abstract val speedUnitShort: String
    abstract val speedUnitLong: String
  }

  var units: Units
    get() = Units.valueOf(
      prefs.getString(KEY_UNITS, Units.METRIC.toString()) ?: Units.METRIC.toString()
    )
    set(value) = prefs.edit { putString(KEY_UNITS, value.toString()) }
}