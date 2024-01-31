package com.github.oilulio.expensesapp

import androidx.annotation.WorkerThread

class ClaimRepository(private val claimDao: ClaimDao) {

  val allClaims = claimDao.getAll()
  val total = claimDao.getTotal()
  val unpaidTotal = claimDao.getUnpaidTotal()

  @WorkerThread
  suspend fun insert(claim: Claim) {
    claimDao.insert(claim)
  }
}