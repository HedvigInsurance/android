package com.hedvig.android.feature.movingflow.compose

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.Snapshot

@Stable
internal interface ListInput<T> {
  val value: List<T>

  fun updateValue(newValue: List<T>)
}

internal fun <T> ListInput(initialList: List<T>): ListInput<T> = ListInputImpl(initialList)

private class ListInputImpl<T>(initialList: List<T>) : ListInput<T> {
  private val _value = mutableStateListOf<T>().apply {
    addAll(initialList)
  }

  override val value: List<T>
    get() = _value

  override fun updateValue(newValue: List<T>) {
    Snapshot.withMutableSnapshot {
      _value.clear()
      _value.addAll(newValue)
    }
  }
}
