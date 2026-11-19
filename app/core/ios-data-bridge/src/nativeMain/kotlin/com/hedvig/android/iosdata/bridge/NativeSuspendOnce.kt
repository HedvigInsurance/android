package com.hedvig.android.iosdata.bridge

import com.rickclephas.kmp.nativecoroutines.NativeCancellable
import com.rickclephas.kmp.nativecoroutines.NativeSuspend
import kotlin.concurrent.AtomicInt
import kotlinx.coroutines.CancellationException
import platform.Foundation.NSError

/**
 * Passes on only the first callback, and makes the caller's cancel final: cancelling delivers the cancellation at once
 * when nothing was delivered yet, and drops whatever the call delivers after it. `nativeSuspend` alone can call back
 * twice when a cancel lands as a call finishes.
 */
internal fun <T> NativeSuspend<T>.deliveringOnce(): NativeSuspend<T> = { onResult, onError, onCancelled ->
  val delivered = AtomicInt(0)
  val cancelCall = this(
    { value, unit -> if (delivered.compareAndSet(0, 1)) onResult(value, unit) else unit },
    { error, unit -> if (delivered.compareAndSet(0, 1)) onError(error, unit) else unit },
    { error, unit -> if (delivered.compareAndSet(0, 1)) onCancelled(error, unit) else unit },
  )
  val cancel: NativeCancellable = {
    if (delivered.compareAndSet(0, 1)) onCancelled(callerCancellation(), Unit)
    cancelCall()
  }
  cancel
}

/** The Kotlin throwable inside an `NSError` that a `NativeSuspend` call delivered, if there is one. */
internal fun NSError.kotlinThrowable(): Throwable? = userInfo[KOTLIN_EXCEPTION_KEY] as? Throwable

private fun callerCancellation(): NSError = NSError.errorWithDomain(
  domain = KOTLIN_EXCEPTION_KEY,
  code = 0,
  userInfo = mapOf<Any?, Any>(KOTLIN_EXCEPTION_KEY to CancellationException("Cancelled by the caller")),
)

/** Where `kmp-nativecoroutines-core` puts the Kotlin throwable in the `NSError` it delivers. */
private const val KOTLIN_EXCEPTION_KEY = "KotlinException"
