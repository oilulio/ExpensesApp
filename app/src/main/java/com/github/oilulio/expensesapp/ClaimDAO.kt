package com.github.oilulio.expensesapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.IGNORE
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// Maps SQL queries to methods.  Call methods, Room does the rest.
@Dao
interface ClaimDao {
  @Query("SELECT * FROM expenses")
  fun getAll(): Flow<List<Claim>>

  @Query("SELECT SUM(amount) AS unpaidTotal FROM expenses WHERE paid = false")
  fun getUnpaidTotal(): Flow<Unpaid>

  @Query("SELECT SUM(amount) AS dbTotal FROM expenses")
  fun getTotal(): Flow<Total>
//  https://stackoverflow.com/questions/50801617/return-sum-and-average-using-room?rq=3

  @Insert(onConflict = IGNORE)
  suspend fun insert(claim: Claim)
}