package com.hisbaan.vector.data.model

import android.location.Location

data class PointerModel(
  var bearingToDestination: Float,
  var currentLocation: Location,
  var heading: Float,
  var distance: Float,
  var speed: Double,
)

