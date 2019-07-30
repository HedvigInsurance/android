package com.hedvig.app.feature.dismissablepager

fun isPositionLast(position: Int, count: Int) = position == count - 1
fun isPositionNextToLast(position: Int, count: Int) = position + 1 == count - 1
