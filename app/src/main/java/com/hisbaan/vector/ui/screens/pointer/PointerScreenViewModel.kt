package com.hisbaan.vector.ui.screens.pointer

import android.content.Context
import android.location.Location
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.places.api.model.Place
import com.hisbaan.vector.data.Preferences
import com.hisbaan.vector.data.model.DestinationModel
import com.hisbaan.vector.data.model.PointerModel
import com.hisbaan.vector.data.persistence.entities.NavigationState
import com.hisbaan.vector.data.repository.CompassRepository
import com.hisbaan.vector.data.repository.LocationRepository
import com.hisbaan.vector.data.repository.NavigationStateRepository
import kotlinx.coroutines.flow.*

class PointerScreenViewModel(
  context: Context,
  locationRepository: LocationRepository,
  compassRepository: CompassRepository,
  private val navigationStateRepository: NavigationStateRepository,
) : ViewModel() {
  private val preferences = Preferences(context.applicationContext);
  private val gpsBearingOverrideSpeed = preferences.gpsBearingOverrideSpeed;
  private val units = preferences.units;

  private val _destinationState = MutableStateFlow(
    DestinationModel(
      name = "",
      location = Location("manual")
    )
  )
  val destinationState: StateFlow<DestinationModel> = _destinationState.asStateFlow()

  val state: StateFlow<PointerModel> =
    combine(
      locationRepository.locationFlow(),
      compassRepository.bearingFlow(),
      _destinationState.filter { it.name.isNotEmpty() }
    ) { currentLocation, bearingToNorth, destination ->
      val speedInUnit = units.getSpeedUnit(currentLocation.speed)
      val useGpsHeading = speedInUnit > gpsBearingOverrideSpeed
      val heading = if (useGpsHeading && currentLocation.hasBearing()) {
        currentLocation.bearing
      } else {
        bearingToNorth
      }

      PointerModel(
        bearingToDestination = (currentLocation.bearingTo(destination.location) - heading + 360) % 360,
        heading = heading,
        currentLocation = currentLocation,
        distance = currentLocation.distanceTo(destination.location),
        speed = speedInUnit
      )
    }.stateIn(
      viewModelScope, SharingStarted.Eagerly,
      PointerModel(
        bearingToDestination = 0f,
        heading = 0f,
        currentLocation = Location("manual"),
        distance = 0f,
        speed = 0.0
      )
    )

  fun setSelectedPlace(place: Place) {
    if (place.location == null) return
    setDestination(
      place.location!!.latitude,
      place.location!!.longitude,
      place.displayName ?: place.formattedAddress ?: ""
    )
  }

  fun setDestination(latitude: Double, longitude: Double, name: String) {
    val destLocation = Location("manual").apply {
      this.latitude = latitude
      this.longitude = longitude
    }
    _destinationState.value = DestinationModel(name = name, location = destLocation)
  }

  suspend fun restoreNavigationState() {
    navigationStateRepository.get()?.let { state ->
       // TODO just make the state undefined if this is the case instead of checking for falsy values
       if (_destinationState.value.location.latitude == 0.0 && _destinationState.value.location.longitude == 0.0 && _destinationState.value.name == "") {
         println("restoring navigation state")
         setDestination(state.destinationLatitude, state.destinationLongitude, state.destinationName)
       }
    }
  }

  suspend fun saveNavigationState() {
    val dest = _destinationState.value
    navigationStateRepository.save(
      NavigationState(
        destinationLatitude = dest.location.latitude,
        destinationLongitude = dest.location.longitude,
        destinationName = dest.name
      )
    )
  }

  suspend fun clearNavigationState() {
    navigationStateRepository.clear()
  }

  suspend fun handleLifecycleEvent(event: Lifecycle.Event) {
    when (event) {
      Lifecycle.Event.ON_PAUSE -> onPause()
      Lifecycle.Event.ON_RESUME -> onResume()
      Lifecycle.Event.ON_DESTROY -> onDestroy()
      Lifecycle.Event.ON_START -> {}
      Lifecycle.Event.ON_STOP -> {}
      Lifecycle.Event.ON_CREATE -> {}
      Lifecycle.Event.ON_ANY -> {}
    }
  }

  private suspend fun onPause() {
    saveNavigationState()
  }

  private suspend fun onResume() {
    restoreNavigationState()
  }

  private suspend fun onDestroy() {
    clearNavigationState()
  }
}
