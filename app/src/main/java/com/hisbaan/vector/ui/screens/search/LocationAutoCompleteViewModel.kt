package com.hisbaan.vector.ui.screens.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.*
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import com.hisbaan.vector.data.repository.LocationRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.math.cos

class LocationAutocompleteViewModel(
  private val placesClient: PlacesClient,
  private val locationRepository: LocationRepository,
) : ViewModel() {
  var query: String = ""
    private set
  var suggestions by mutableStateOf(listOf<AutocompletePrediction>())
    private set

  private var searchJob: Job? = null
  private val token = AutocompleteSessionToken.newInstance()

  fun onQueryChanged(newQuery: String) {
    query = newQuery
    searchJob?.cancel()
    searchJob = viewModelScope.launch {
      delay(300) // debounce
      if (newQuery.isNotBlank()) {
        val location = locationRepository.getLastLocation();
        try {
          var request = FindAutocompletePredictionsRequest.builder()
            .setSessionToken(token)
            .setQuery(newQuery)

          if (location != null) {
            val centerLat = location.latitude
            val centerLng = location.longitude

            // approximate conversions: 1 deg latitude approx 111_320 meters
            val meters = 20_000.0
            val latDegOffset = meters / 111_320.0
            // longitude degrees vary with latitude
            val lonDegOffset = meters / (111_320.0 * cos(Math.toRadians(centerLat)))

            val southwest = LatLng(centerLat - latDegOffset, centerLng - lonDegOffset)
            val northeast = LatLng(centerLat + latDegOffset, centerLng + lonDegOffset)

            val bounds = RectangularBounds.newInstance(southwest, northeast)
            request = request.setLocationBias(bounds).setOrigin(LatLng(centerLat, centerLng))
          }

          val response = placesClient.findAutocompletePredictions(request.build()).await()
          suggestions = response.autocompletePredictions.sortedBy { it.distanceMeters }
        } catch (_: Exception) {
          suggestions = emptyList()
        }
      } else {
        suggestions = emptyList()
      }
    }
  }

  suspend fun getPlaceDetails(placeId: String): Place? {
    val fields = listOf(
      Place.Field.ID,
      Place.Field.NAME,
      Place.Field.ADDRESS,
      Place.Field.LAT_LNG
    )
    return try {
      val request = FetchPlaceRequest.builder(placeId, fields)
        .setSessionToken(token)
        .build()
      val response = placesClient.fetchPlace(request).await()
      response.place
    } catch (e: Exception) {
      println(e.message)
      null
    }
  }
}