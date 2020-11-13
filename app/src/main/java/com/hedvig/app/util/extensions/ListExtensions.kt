package com.hedvig.app.util.extensions

import java.util.ArrayList

val <T> List<T>.tail: List<T>
    get() = subList(1, size)

val <T> List<T>.head: T
    get() = first()

fun <T> List<T>.toArrayList() = ArrayList(this)
