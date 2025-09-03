package com.hisbaan.vector.data.repository

import android.content.Context
import com.hisbaan.vector.data.persistence.AppDatabase

fun provideLocationRepository(context: Context): LocationRepository = LocationRepository(context)

fun provideCompassRepository(context: Context) = CompassRepository(context)

fun provideNavigationStateRepository(context: Context) =
  NavigationStateRepository(AppDatabase.getDatabase(context).navigationStateDao())
