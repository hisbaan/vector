package com.hisbaan.vector.ui.screens.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hisbaan.vector.data.Preferences
import com.hisbaan.vector.ui.activities.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigate: (Screen) -> Unit) {
  val context = LocalContext.current.applicationContext
  val prefs = remember { Preferences(context) }

  var units by rememberSaveable { mutableStateOf(prefs.units) }
  var gpsRefreshSpeed by rememberSaveable { mutableStateOf(prefs.gpsRefreshSpeed) }
  var gpsBearingOverrideSpeed by rememberSaveable {
    mutableStateOf(prefs.gpsBearingOverrideSpeed.toString())
  }

  var bearingInputError by remember { mutableStateOf<String?>(null) }
  val focusManager = LocalFocusManager.current

  BackHandler { onNavigate(Screen.Search) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Settings") },
        navigationIcon = {
          IconButton(onClick = { onNavigate(Screen.Search) }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        }
      )
    },
    modifier = Modifier.fillMaxSize()
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Units
      RadioCardGroup(
        items = Preferences.Units.entries,
        selectedItem = units,
        onItemSelected = { unit ->
          units = unit
          prefs.units = unit
        },
        label = { it.name },
        title = { Text("Units", style = MaterialTheme.typography.titleMedium) }
      )

      HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

      // GPS Refresh Speed
      RadioCardGroup(
        items = Preferences.GpsRefreshSpeed.entries,
        selectedItem = gpsRefreshSpeed,
        onItemSelected = { speed ->
          gpsRefreshSpeed = speed
          prefs.gpsRefreshSpeed = speed
        },
        label = { it.name },
        title = { Text("GPS Refresh Speed", style = MaterialTheme.typography.titleMedium) }
      )

      HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

      // GPS Bearing Override Speed (Number Input)
      Column(modifier = Modifier.fillMaxWidth()) {
        Text("GPS Bearing Override Speed", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = gpsBearingOverrideSpeed,
          onValueChange = { input ->
            gpsBearingOverrideSpeed = input
            val parsed = input.toFloatOrNull()
            if (input.isBlank()) {
              bearingInputError = "Value required"
            } else if (parsed == null) {
              bearingInputError = "Enter a valid number"
            } else if (parsed < 0f) {
              bearingInputError = "Must be non-negative"
            } else {
              bearingInputError = null
              prefs.gpsBearingOverrideSpeed = parsed
            }
          },
          label = { Text("Speed (${units.speedUnitShort})") },
          singleLine = true,
          isError = bearingInputError != null,
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
          ),
          keyboardActions = KeyboardActions(onDone = {
            focusManager.clearFocus()
          }),
          modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "GPS Bearing Override Speed input" }
        )

        if (bearingInputError != null) {
          Text(
            text = bearingInputError ?: "",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 6.dp)
          )
        } else {
          Text(
            text = "Enter the speed threshold in ${units.speedUnitLong}.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 6.dp)
          )
        }
      }
    }
  }
}

@Composable
fun <T> RadioCardGroup(
  items: List<T>,
  selectedItem: T,
  onItemSelected: (T) -> Unit,
  label: (T) -> String,
  modifier: Modifier = Modifier,
  title: @Composable (() -> Unit)? = null
) {
  Column(modifier = modifier.fillMaxWidth()) {
    if (title != null) {
      title()
      Spacer(modifier = Modifier.height(8.dp))
    }

    val scroll = rememberScrollState()
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(scroll)
        .selectableGroup(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items.forEach { item ->
        val selected = selectedItem == item
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .heightIn(max = 56.dp)
            .selectable(
              selected = selected,
              onClick = { onItemSelected(item) }
            ),
          colors = if (selected) CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
          ) else CardDefaults.cardColors(),
          elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 6.dp else 1.dp)
        ) {
          Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
          ) {
            Text(label(item), style = MaterialTheme.typography.bodyLarge)
          }
        }
      }
    }
  }
}