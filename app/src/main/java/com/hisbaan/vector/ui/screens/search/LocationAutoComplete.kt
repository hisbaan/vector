package com.hisbaan.vector.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.libraries.places.api.model.Place
import kotlinx.coroutines.launch

@Composable
fun LocationAutocomplete(
  viewModel: LocationAutocompleteViewModel,
  onPlaceSelected: (Place) -> Unit,
) {
  // TODO bias results to current location with LocationRepository
  var expanded by remember { mutableStateOf(false) }
  val query = remember { mutableStateOf("") }
  val suggestions = viewModel.suggestions
  val coroutineScope = rememberCoroutineScope()

  Column(Modifier.padding(16.dp)) {
    OutlinedTextField(
      singleLine = true,
      value = query.value,
      onValueChange = {
        query.value = it
        viewModel.onQueryChanged(it)
        expanded = true
      },
      label = { Text("Search location") },
      modifier = Modifier.fillMaxWidth()
    )

    if (expanded && suggestions.isNotEmpty()) {
      LazyColumn {
        items(suggestions) { prediction ->
          // TODO make this text item nicer and look like the google maps selection boxes
          // limit the selection to 5 numbers or so to make sure we don't have suggestions
          // below the keyboard (but > 5 wouldn't really matter because they'd be lower priority)
          Text(
            prediction.getFullText(null).toString(),
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 8.dp)
              .clickable {
                query.value = prediction.getFullText(null).toString()
                expanded = false
                coroutineScope.launch {
                  val place = viewModel.getPlaceDetails(prediction.placeId)
                  if (place != null) {
                    onPlaceSelected(place)
                  }
                }
              }
          )
        }
      }
    }
  }
}
