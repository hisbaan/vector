package com.hisbaan.vector.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import com.hisbaan.vector.ui.theme.VectorTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.hisbaan.vector.data.persistence.AppDatabase
import com.hisbaan.vector.data.repository.NavigationStateRepository
import com.hisbaan.vector.ui.screens.pointer.PointerScreen
import com.hisbaan.vector.ui.screens.search.SearchScreen
import com.hisbaan.vector.ui.screens.settings.SettingsScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      VectorTheme {
        ScreenSwitcher()
      }
    }
  }
}

enum class Screen {
  Search, Pointer, Settings
}

@Composable
fun ScreenSwitcher() {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val navigationStateRepository = remember { NavigationStateRepository( AppDatabase.getDatabase(context).navigationStateDao() ) }
  var currentScreen by remember { mutableStateOf(Screen.Search) }

  LaunchedEffect(Unit) {
    val exists = scope.run { navigationStateRepository.exists() }
    if (exists) {
      currentScreen = Screen.Pointer
    }
  }

  when (currentScreen) {
    Screen.Search -> SearchScreen(onNavigate = { currentScreen = it })
    Screen.Pointer -> PointerScreen(onNavigate = { currentScreen = it })
    Screen.Settings -> SettingsScreen(onNavigate = { currentScreen = it })
  }
}