package com.hedvig.core.common

import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.jvm.Throws
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response

/**
 * Suspend extension that allows to await a [Response] from a [Call] in a suspending context coroutine.
 *
 * @return Result of request or throw exception
 */
@Throws(IOException::class)
suspend fun Call.await(): Response {
  return suspendCancellableCoroutine { continuation ->
    enqueue(
      object : Callback {
        override fun onResponse(call: Call, response: Response) {
          continuation.resume(response)
        }

        override fun onFailure(call: Call, e: IOException) {
          // Don't bother with resuming the continuation if it is already cancelled.
          if (continuation.isCancelled) return
          continuation.resumeWithException(e)
        }
      },
    )

    continuation.invokeOnCancellation {
      try {
        cancel()
      } catch (ignored: Throwable) {
      }
    }
  }
}
