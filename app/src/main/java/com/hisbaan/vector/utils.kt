package com.hisbaan.vector

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

inline fun <reified T> T.logi(message: String) = Log.i(T::class.java.simpleName, message)