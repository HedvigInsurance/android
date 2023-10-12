package com.hedvig.app.util.extensions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

fun ViewGroup.addViews(views: List<View>) = views.forEach { addView(it) }

val ViewGroup.firstChild
  get() = getChildAt(0)

class ViewIterator(private val parent: ViewGroup) : Iterator<View> {

  var length = parent.childCount
  var current = 0

  override fun hasNext() = current < length

  override fun next(): View {
    val ret = parent.getChildAt(current)
    current += 1
    return ret
  }
}

fun ViewGroup.inflate(@LayoutRes layout: Int, attachToRoot: Boolean = false): View =
  LayoutInflater.from(context).inflate(layout, this, attachToRoot)
