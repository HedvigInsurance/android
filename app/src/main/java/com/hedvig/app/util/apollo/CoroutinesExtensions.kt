package com.hedvig.app.util.apollo

/**
 * Copied from https://github.com/apollo-android as we are unable to upgrade to v1.4.4 due to a
 * bug in that version.
 *
 * Copyright (c) 2016 Meteor Development Group, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloPrefetch
import com.apollographql.apollo.ApolloQueryWatcher
import com.apollographql.apollo.ApolloSubscriptionCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Converts an [ApolloCall] to an [Flow].
 *
 * @param <T>  the value type.
 * @return a flow which emits [Responses<T>]
 */
@ExperimentalCoroutinesApi
fun <T> ApolloCall<T>.toFlow(): Flow<Response<T>> = callbackFlow {
    clone().enqueue(
        object : ApolloCall.Callback<T>() {
            override fun onResponse(response: Response<T>) {
                runCatching {
                    offer(response)
                }
            }

            override fun onFailure(e: ApolloException) {
                close(e)
            }

            override fun onStatusEvent(event: ApolloCall.StatusEvent) {
                if (event == ApolloCall.StatusEvent.COMPLETED) {
                    close()
                }
            }
        }
    )
    awaitClose { this@toFlow.cancel() }
}

/**
 * Converts an [ApolloQueryWatcher] to an [Flow].
 *
 * @param <T>  the value type.
 * @return a flow which emits [Responses<T>]
 */
@ExperimentalCoroutinesApi
fun <T> ApolloQueryWatcher<T>.toFlow(): Flow<Response<T>> = callbackFlow {
    clone().enqueueAndWatch(
        object : ApolloCall.Callback<T>() {
            override fun onResponse(response: Response<T>) {
                runCatching {
                    offer(response)
                }
            }

            override fun onFailure(e: ApolloException) {
                close(e)
            }

            override fun onStatusEvent(event: ApolloCall.StatusEvent) {
                if (event == ApolloCall.StatusEvent.COMPLETED) {
                    close()
                }
            }
        }
    )
    awaitClose { this@toFlow.cancel() }
}

/**
 * Converts an [ApolloCall] to an [Deferred]. This is a convenience method that will only return the first value emitted.
 * If the more than one response is required, for an example to retrieve cached and network response, use [toFlow] instead.
 *
 * @param <T>  the value type.
 * @return the deferred
 */
fun <T> ApolloCall<T>.toDeferred(): Deferred<Response<T>> {
    val deferred = CompletableDeferred<Response<T>>()

    deferred.invokeOnCompletion {
        if (deferred.isCancelled) {
            cancel()
        }
    }
    enqueue(object : ApolloCall.Callback<T>() {
        override fun onResponse(response: Response<T>) {
            if (deferred.isActive) {
                deferred.complete(response)
            }
        }

        override fun onFailure(e: ApolloException) {
            if (deferred.isActive) {
                deferred.completeExceptionally(e)
            }
        }
    })

    return deferred
}

/**
 * Converts an [ApolloSubscriptionCall] to an [Flow].
 *
 * @param <T>  the value type.
 * @return a flow which emits [Responses<T>]
 */
@ExperimentalCoroutinesApi
fun <T> ApolloSubscriptionCall<T>.toFlow(): Flow<Response<T>> = callbackFlow {
    clone().execute(
        object : ApolloSubscriptionCall.Callback<T> {
            override fun onConnected() {
            }

            override fun onResponse(response: Response<T>) {
                runCatching {
                    channel.offer(response)
                }
            }

            override fun onFailure(e: ApolloException) {
                channel.close(e)
            }

            override fun onCompleted() {
                channel.close()
            }

            override fun onTerminated() {
                channel.close()
            }
        }
    )
    awaitClose { this@toFlow.cancel() }
}

/**
 * Converts an [ApolloPrefetch] to [Job].
 *
 * @param <T>  the value type.
 * @return the converted job
 */
fun ApolloPrefetch.toJob(): Job {
    val deferred = CompletableDeferred<Unit>()

    deferred.invokeOnCompletion {
        if (deferred.isCancelled) {
            cancel()
        }
    }

    enqueue(object : ApolloPrefetch.Callback() {
        override fun onSuccess() {
            if (deferred.isActive) {
                deferred.complete(Unit)
            }
        }

        override fun onFailure(e: ApolloException) {
            if (deferred.isActive) {
                deferred.completeExceptionally(e)
            }
        }
    })

    return deferred
}

