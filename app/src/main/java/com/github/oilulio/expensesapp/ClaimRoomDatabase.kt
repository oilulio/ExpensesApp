package com.github.oilulio.expensesapp

// Based on https://developer.android.com/codelabs/android-room-with-a-view-kotlin#0

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope

@Database(entities = [Claim::class], version = 16, exportSchema = false)
abstract class ClaimRoomDatabase : RoomDatabase() {

  abstract fun claimDao(): ClaimDao

  companion object {
    @Volatile
    private var INSTANCE: ClaimRoomDatabase? = null

    fun getDatabase(
      context: Context,
      scope: CoroutineScope
    ): ClaimRoomDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          ClaimRoomDatabase::class.java,
          "expenses_db"
        )
          .fallbackToDestructiveMigration()
          .addCallback(ClaimDatabaseCallback(scope))
          .build()
        INSTANCE = instance
        return instance
      }
    }

    private class ClaimDatabaseCallback(
      private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
      override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
      }
    }
  }
}
