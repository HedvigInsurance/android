package com.hedvig.app.util.extensions

import com.google.android.gms.tasks.Task
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun <T> Task<T>.await() = suspendCoroutine<T> { continuation ->
    addOnSuccessListener { succ ->
        continuation.resume(succ)
    }
    addOnFailureListener { e ->
        continuation.resumeWithException(e)
    }
}
