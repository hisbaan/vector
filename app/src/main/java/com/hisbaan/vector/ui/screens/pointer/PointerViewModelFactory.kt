package com.hisbaan.vector.ui.screens.pointer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hisbaan.vector.data.repository.CompassRepository
import com.hisbaan.vector.data.repository.LocationRepository
import com.hisbaan.vector.data.repository.NavigationStateRepository

class PointerViewModelFactory(
  private val context: Context,
  private val locationRepository: LocationRepository,
  private val compassRepository: CompassRepository,
  private val navigationStateRepository: NavigationStateRepository
) : ViewModelProvider.Factory {

  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(PointerScreenViewModel::class.java)) {
      return PointerScreenViewModel(
        context,
        locationRepository,
        compassRepository,
        navigationStateRepository
      ) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
  }
}
