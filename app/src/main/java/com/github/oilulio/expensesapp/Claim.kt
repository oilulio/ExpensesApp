package com.github.oilulio.expensesapp

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// Defines the database table fields

@Entity(tableName = "expenses")
data class Claim(
  @PrimaryKey(autoGenerate = true) val uid: Int = 0,
  @ColumnInfo val date: String,
  @ColumnInfo val amount: Float,
  @ColumnInfo val reason: String,
  @ColumnInfo val paid: Boolean = false,
  @ColumnInfo val receiptUri: String,
  // Seems that autogenerate gets overridden if you specify uid. 0=Unspecified for Int (use null for Integer)
  // see https://developer.android.com/reference/androidx/room/PrimaryKey?hl=en#autoGenerate()
)