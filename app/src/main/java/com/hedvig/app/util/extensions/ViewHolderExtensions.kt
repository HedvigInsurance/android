package com.hedvig.app.util.extensions

import androidx.recyclerview.widget.RecyclerView
import e

inline fun <reified T> RecyclerView.ViewHolder.invalid(_data: T) =
    e { "Invalid data passed to ${this.javaClass.name}::bind - type is ${T::class.java.name}" }
