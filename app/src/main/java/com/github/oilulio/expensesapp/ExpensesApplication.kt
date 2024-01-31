package com.github.oilulio.expensesapp

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class ExpensesApplication : Application() {
  val applicationScope = CoroutineScope(SupervisorJob())

  val database by lazy { ClaimRoomDatabase.getDatabase(this, applicationScope) }
  val repository by lazy { ClaimRepository(database.claimDao()) }
}
