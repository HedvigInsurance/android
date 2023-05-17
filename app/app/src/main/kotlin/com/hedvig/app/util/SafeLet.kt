package com.hedvig.app.util

fun <T1, T2, R> safeLet(a: T1?, b: T2?, action: (T1, T2) -> R) =
  if (a != null && b != null) action(a, b) else null

fun <T1, T2, T3, R> safeLet(a: T1?, b: T2?, c: T3?, action: (T1, T2, T3) -> R) =
  if (a != null && b != null && c != null) action(a, b, c) else null
