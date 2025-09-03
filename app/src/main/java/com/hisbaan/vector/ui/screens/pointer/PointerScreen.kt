package com.hisbaan.vector.ui.screens.pointer;

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.roundToInt
import android.app.Activity
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hisbaan.vector.data.Preferences
import com.hisbaan.vector.data.repository.provideCompassRepository
import com.hisbaan.vector.data.repository.provideLocationRepository
import com.hisbaan.vector.data.repository.provideNavigationStateRepository
import com.hisbaan.vector.ui.activities.Screen
import kotlinx.coroutines.launch

@Composable
fun PointerScreen(
  onNavigate: (Screen) -> Unit,
  lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
) {
  val context = LocalContext.current
  val preferences = Preferences(context.applicationContext)

  val units = preferences.units

  val locationRepo = remember { provideLocationRepository(context) }
  val compassRepo = remember { provideCompassRepository(context) }
  val navStateRepo = remember { provideNavigationStateRepository(context) }

  val viewModel: PointerScreenViewModel = viewModel(
    context as ComponentActivity,
    factory = PointerViewModelFactory(context, locationRepo, compassRepo, navStateRepo)
  )

  val state by viewModel.state.collectAsStateWithLifecycle()
  val destinationState by viewModel.destinationState.collectAsStateWithLifecycle()
  val scope = rememberCoroutineScope()

  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      scope.launch { viewModel.handleLifecycleEvent(event) }
    }

    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }

  KeepScreenOn()

  BackHandler { onNavigate(Screen.Search) }

  Scaffold { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Text(destinationState.name)
      // TODO imperial conversions here
      Text(units.formatDistance(state.distance))
      Box(
        contentAlignment = Alignment.Center,
      ) {
        Pointer(state.bearingToDestination)
        Compass(-state.heading)
      }
      // TODO imperial conversions here
      Text("${state.speed.roundToInt()} ${units.speedUnitShort}")
    }
  }
}

@Composable
fun Pointer(rotationDegrees: Float) {
  val prevRotation = remember { mutableFloatStateOf(rotationDegrees) }
  val animatedRotation = remember { mutableFloatStateOf(rotationDegrees) }

  val delta = ((rotationDegrees - prevRotation.floatValue + 540) % 360) - 180
  val targetRotation = animatedRotation.floatValue + delta

  val rotationAngle by animateFloatAsState(
    targetValue = targetRotation,
    animationSpec = tween(durationMillis = 150),
    label = "rotationAnimation"
  )

  LaunchedEffect(rotationDegrees) {
    prevRotation.floatValue = rotationDegrees
    animatedRotation.floatValue = targetRotation
  }

  Icon(
    Icons.Rounded.KeyboardArrowUp,
    "Pointer",
    modifier = Modifier
      .size(256.dp)
      .graphicsLayer(rotationZ = rotationAngle)
  )
}

@Composable
fun Compass(rotationDegrees: Float) {
  val prevRotation = remember { mutableFloatStateOf(rotationDegrees) }
  val animatedRotation = remember { mutableFloatStateOf(rotationDegrees) }

  val delta = ((rotationDegrees - prevRotation.floatValue + 540) % 360) - 180
  val targetRotation = animatedRotation.floatValue + delta

  val rotationAngle by animateFloatAsState(
    targetValue = targetRotation,
    animationSpec = tween(durationMillis = 150),
    label = "rotationAnimation"
  )

  LaunchedEffect(rotationDegrees) {
    prevRotation.floatValue = rotationDegrees
    animatedRotation.floatValue = targetRotation
  }

  val radius = 96.dp
  val dotSize = 16.dp
  val containerSize = radius * 2 + dotSize

  Box(
    modifier = Modifier.size(containerSize),
    contentAlignment = Alignment.Center
  ) {
    Box(
      modifier = Modifier
        .graphicsLayer {
          rotationZ = rotationAngle
        }
        .fillMaxSize()
    ) {
      Box(
        modifier = Modifier
          .offset(x = radius)
          .size(dotSize)
          .background(Color.Red, CircleShape)
      )
    }
  }
}

@Composable
fun KeepScreenOn() {
  val context = LocalContext.current
  val window = (context as? Activity)?.window

  DisposableEffect(Unit) {
    window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    onDispose {
      window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
  }
}