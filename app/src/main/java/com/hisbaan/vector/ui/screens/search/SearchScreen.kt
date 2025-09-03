package com.hisbaan.vector.ui.screens.search

import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.libraries.places.api.Places
import com.hisbaan.vector.BuildConfig
import com.hisbaan.vector.data.repository.provideCompassRepository
import com.hisbaan.vector.data.repository.provideLocationRepository
import com.hisbaan.vector.data.repository.provideNavigationStateRepository
import com.hisbaan.vector.ui.activities.Screen
import com.hisbaan.vector.ui.screens.pointer.PointerScreenViewModel
import com.hisbaan.vector.ui.screens.pointer.PointerViewModelFactory

@Composable
fun SearchScreen(
  onNavigate: (Screen) -> Unit,
) {
  val context = LocalContext.current

  Places.initialize(LocalContext.current, BuildConfig.PLACES_API_KEY)
  if (!Places.isInitialized()) {
    println("Places not initialized")
  }
  val placesClient = Places.createClient(context)

  val locationRepo = remember { provideLocationRepository(context) }
  val compassRepo = remember { provideCompassRepository(context) }
  val navStateRepo = remember { provideNavigationStateRepository(context) }

  val viewModel = remember { LocationAutocompleteViewModel(placesClient, locationRepo) }

  val pointerViewModel: PointerScreenViewModel = viewModel(
    context as ComponentActivity,
    factory = PointerViewModelFactory(context, locationRepo, compassRepo, navStateRepo)
  )

  Scaffold(modifier = Modifier.fillMaxSize()) { internalPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(internalPadding)
    ) {
      LocationAutocomplete(
        viewModel = viewModel,
        onPlaceSelected = { place ->
          println("Selected Place: ${place.displayName}")
          println("Address: ${place.formattedAddress}")
          println("LatLng: ${place.location?.latitude}, ${place.location?.longitude}")

          onNavigate(Screen.Pointer)
          pointerViewModel.setSelectedPlace(place)
        }
      )
      Icon(
        Icons.Rounded.Settings,
        "Pointer",
        modifier = Modifier
          .clickable { onNavigate(Screen.Settings) }
          .size(16.dp)
      )
    }
  }
}
