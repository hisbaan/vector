package com.hisbaan.vector.data.repository

import com.hisbaan.vector.data.persistence.dao.NavigationStateDao
import com.hisbaan.vector.data.persistence.entities.NavigationState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigationStateRepository @Inject constructor(
  private val dao: NavigationStateDao
) {
  suspend fun get(): NavigationState? = dao.get()

  suspend fun save(state: NavigationState) {
    dao.save(state.copy(id = 0))
  }

  suspend fun clear() {
    dao.clear()
  }

  suspend fun exists(): Boolean = dao.exists()
}
