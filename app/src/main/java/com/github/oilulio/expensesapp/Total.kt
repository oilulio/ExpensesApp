package com.github.oilulio.expensesapp

import kotlin.reflect.KProperty

data class Total(var dbTotal: Float? = null) {
  operator fun getValue(nothing: Nothing?, property: KProperty<*>): Any {
    return this
  }

  fun getTotal(): Float? {
    return dbTotal
  }
}