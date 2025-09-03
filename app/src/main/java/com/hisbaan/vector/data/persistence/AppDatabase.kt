package com.hisbaan.vector.data.persistence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.hisbaan.vector.data.persistence.dao.NavigationStateDao
import com.hisbaan.vector.data.persistence.entities.NavigationState

@Database(entities = [NavigationState::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
  abstract fun navigationStateDao(): NavigationStateDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "app_database"
        ).build()
        INSTANCE = instance
        instance
      }
    }
  }
}
