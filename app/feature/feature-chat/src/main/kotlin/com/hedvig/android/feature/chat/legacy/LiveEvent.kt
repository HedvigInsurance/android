package com.hedvig.android.feature.chat.legacy

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer

// to be deleted asap when Chat gets reworked. Copied over from :app so we don't expose this everywhere in a common
// module
internal class LiveEvent<T> : MediatorLiveData<T>() {
  private val observers = HashSet<ObserverWrapper<in T>>()

  @MainThread
  override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
    val wrapper = ObserverWrapper(observer)
    observers.add(wrapper)
    super.observe(owner, wrapper)
  }

  @MainThread
  override fun removeObserver(observer: Observer<in T>) {
    if (observers.remove<Observer<in T>>(observer)) {
      super.removeObserver(observer)
      return
    }
    val iterator = observers.iterator()
    while (iterator.hasNext()) {
      val wrapper = iterator.next()
      if (wrapper.observer == observer) {
        iterator.remove()
        super.removeObserver(wrapper)
        break
      }
    }
  }

  @MainThread
  override fun setValue(t: T?) {
    observers.forEach { it.newValue() }
    super.setValue(t)
  }

  private class ObserverWrapper<T>(val observer: Observer<T>) : Observer<T> {

    private var pending = false

    override fun onChanged(value: T) {
      if (pending) {
        pending = false
        observer.onChanged(value)
      }
    }

    fun newValue() {
      pending = true
    }
  }
}
