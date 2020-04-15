package com.hedvig.app.util.extensions

import e
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

val timberLoggingCoroutineExceptionHandler =
    CoroutineExceptionHandler { _, throwable ->
        if (throwable !is CancellationException) {
            e(throwable) { "Caught potentially fatal exception in Coroutine" }
        }
    }

fun CoroutineScope.safeLaunch(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
) = launch(timberLoggingCoroutineExceptionHandler + context, start, block)
