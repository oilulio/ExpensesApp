package com.github.oilulio.expensesapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ClaimViewModel(private val repository: ClaimRepository) : ViewModel() {

  val allClaims: Flow<List<Claim>> = repository.allClaims
  val total: Flow<Total> = repository.total
  val unpaidTotal: Flow<Unpaid> = repository.unpaidTotal

  fun insert(claim: Claim) = viewModelScope.launch {
    repository.insert(claim)
  }
}

class ClaimViewModelFactory(private val repository: ClaimRepository) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(ClaimViewModel::class.java)) {
      @Suppress("UNCHECKED_CAST")
      return ClaimViewModel(repository) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}
