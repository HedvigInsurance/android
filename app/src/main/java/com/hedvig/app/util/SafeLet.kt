package com.hedvig.app.util

fun <T1, T2, R> safeLet(a: T1?, b: T2?, action: (T1, T2) -> R) =
    if (a != null && b != null) action(a, b) else null

fun <T1, T2, T3, R> safeLet(a: T1?, b: T2?, c: T3?, action: (T1, T2, T3) -> R) =
    if (a != null && b != null && c != null) action(a, b, c) else null

fun <T1, T2, T3, T4, R> safeLet(a: T1?, b: T2?, c: T3?, d: T4?, action: (T1, T2, T3, T4) -> R) =
    if (a != null && b != null && c != null && d != null) action(a, b, c, d) else null

fun <T1, T2, T3, T4, T5, T6, R> safeLet(
    a: T1?,
    b: T2?,
    c: T3?,
    d: T4?,
    e: T5?,
    f: T6?,
    action: (T1, T2, T3, T4, T5, T6) -> R
) =
    if (a != null && b != null && c != null && d != null && e != null && f != null) action(
        a,
        b,
        c,
        d,
        e,
        f
    ) else null
