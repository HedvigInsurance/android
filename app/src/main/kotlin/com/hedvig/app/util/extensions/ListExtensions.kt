package com.hedvig.app.util.extensions

import java.util.ArrayList

fun <T> List<T>.toArrayList() = ArrayList(this)

fun <T> List<T>.replace(newValue: T, block: (T) -> Boolean): List<T> {
  return map {
    if (block(it)) newValue else it
  }
}
