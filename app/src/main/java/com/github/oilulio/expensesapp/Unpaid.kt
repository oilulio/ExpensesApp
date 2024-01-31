package com.github.oilulio.expensesapp

import kotlin.reflect.KProperty

data class Unpaid(var unpaidTotal: Float? = null) {
  operator fun getValue(nothing: Nothing?, property: KProperty<*>): Any {
    return this
  }

  fun getTotal(): Float? {
    return unpaidTotal
  }
}