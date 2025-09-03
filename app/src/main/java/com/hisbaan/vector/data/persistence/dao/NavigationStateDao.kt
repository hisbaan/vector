package com.hisbaan.vector.data.persistence.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hisbaan.vector.data.persistence.entities.NavigationState

@Dao
interface NavigationStateDao {
  @Query("SELECT * FROM navigation_state WHERE id = 0")
  suspend fun get(): NavigationState?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun save(state: NavigationState)

  @Query("DELETE FROM navigation_state")
  suspend fun clear()

  @Query("SELECT EXISTS (SELECT 1 from navigation_state)")
  suspend fun exists(): Boolean
}
