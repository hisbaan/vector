package com.hisbaan.vector.data.repository

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import android.location.LocationManager
import com.google.android.gms.tasks.CancellationToken

import android.location.LocationRequest as AndroidLocationRequest
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.hisbaan.vector.data.Preferences
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class LocationRepository(private val context: Context) {
  private val fusedClient = LocationServices.getFusedLocationProviderClient(context)
  private var lastLocation: Location? = null;
  private var preferences = Preferences(context.applicationContext);

  // TODO do we want to get this from settings every time, or only on app launch?
  // Is there a way to watch for changes in prefs?
  private var refreshSpeed = preferences.gpsRefreshSpeed;

  private val locationRequest = LocationRequest.Builder(
    Priority.PRIORITY_HIGH_ACCURACY,
    when (refreshSpeed) {
      Preferences.GpsRefreshSpeed.LOW -> 1500L
      Preferences.GpsRefreshSpeed.MEDIUM -> 1000L
      Preferences.GpsRefreshSpeed.HIGH -> 500L
    }
  ).build()

  @SuppressLint("MissingPermission") // caller must ensure permissions
  fun locationFlow(): Flow<Location> = callbackFlow {

    val callback = object : LocationCallback() {
      override fun onLocationResult(result: LocationResult) {
        val location = result.lastLocation
        if (location != null) {
          trySend(location)
        }
      }
    }

    fusedClient.requestLocationUpdates(locationRequest, callback, null)

    // Clean up on cancellation
    awaitClose {
      fusedClient.removeLocationUpdates(callback)
    }
  }

  suspend fun getLocation(): Location? {
    val hasGrantedFineLocationPermission = ContextCompat.checkSelfPermission(
      context,
      Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val hasGrantedCoarseLocationPermission = ContextCompat.checkSelfPermission(
      context,
      Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val locationManager = context.getSystemService(
      Context.LOCATION_SERVICE
    ) as LocationManager;

    val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ||
        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

    if (!isGpsEnabled && !(hasGrantedCoarseLocationPermission || hasGrantedFineLocationPermission)) {
      return null
    }

    return suspendCancellableCoroutine { cont ->
      fusedClient.getCurrentLocation(
        AndroidLocationRequest.QUALITY_HIGH_ACCURACY,
        object : CancellationToken() {
          override fun onCanceledRequested(p0: OnTokenCanceledListener) =
            CancellationTokenSource().token

          override fun isCancellationRequested() = false
        }).apply {
        if (isComplete) {
          if (isSuccessful) {
            lastLocation = result
            cont.resume(result)
          } else {
            lastLocation = null
            cont.resume(null)
          }
        }

        addOnSuccessListener {
          lastLocation = result
          cont.resume(result)
        }

        addOnFailureListener {
          lastLocation = null
          cont.resume(null)
        }

        addOnCanceledListener {
          cont.cancel()
        }
      }
    }
  }

  suspend fun getLastLocation(): Location? {
    if (lastLocation != null) {
      return lastLocation
    }

    return getLocation();
  }
}
