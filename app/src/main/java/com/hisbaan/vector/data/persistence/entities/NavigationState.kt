package com.hisbaan.vector.data.persistence.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "navigation_state")
data class NavigationState(
  @PrimaryKey val id: Int = 0,
  val destinationLatitude: Double,
  val destinationLongitude: Double,
  val destinationName: String,
)