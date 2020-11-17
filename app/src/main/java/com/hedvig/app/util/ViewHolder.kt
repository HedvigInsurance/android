package com.hedvig.app.util

import androidx.recyclerview.widget.RecyclerView
import e

fun RecyclerView.ViewHolder.invalidData(data: Any) {
    e { "Invalid data passed to ${this.javaClass.name}::bind - type is ${data.javaClass.name}" }
}
