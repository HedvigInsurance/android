package com.hedvig.android.feature.movingflow

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.ApolloRequest
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.exception.ApolloNetworkException
import com.apollographql.apollo.network.NetworkTransport
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import octopus.feature.movingflow.MoveIntentV2RequestMutation
import octopus.type.MoveIntentRequestInput

/**
 * Preferences held in memory, so a write and the emission it causes run on the test scheduler. Like the real store, it
 * emits only when a write changes the preferences.
 */
internal class InMemoryPreferencesDataStore : DataStore<Preferences> {
  private val preferences = MutableStateFlow(emptyPreferences())

  override val data: Flow<Preferences> = preferences

  /** How many writes were started. */
  var writes = 0
    private set

  /** When set, each write waits for one entry before it reads and changes the preferences, in the order started. */
  var gate: Channel<Unit>? = null

  /** Removes everything stored, as if the flow was never started. */
  fun clear() {
    preferences.value = emptyPreferences()
  }

  override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
    writes++
    gate?.receive()
    val updated = transform(preferences.value)
    preferences.value = updated
    return updated
  }
}

/**
 * Answers [MoveIntentV2RequestMutation] in place of the network. Each request is recorded in [inputs] and waits for one
 * entry in [responses]: the response data, or null for a network failure. The client runs on [Dispatchers.Unconfined]
 * so requests stay on the test scheduler.
 */
internal class FakeMoveIntentRequestTransport : NetworkTransport {
  val responses = Channel<MoveIntentV2RequestMutation.Data?>(Channel.UNLIMITED)
  val inputs = mutableListOf<MoveIntentRequestInput>()

  /** How many requests stopped waiting for their response because they were cancelled. */
  var cancelled = 0
    private set

  override fun <D : Operation.Data> execute(request: ApolloRequest<D>): Flow<ApolloResponse<D>> = flow {
    val operation = request.operation as MoveIntentV2RequestMutation
    inputs += operation.moveIntentRequestInput
    val data = try {
      responses.receive()
    } catch (cancellation: CancellationException) {
      cancelled++
      throw cancellation
    }
    val builder = ApolloResponse.Builder(request.operation, request.requestUuid)
    val response = if (data == null) {
      builder.exception(ApolloNetworkException("Fake network failure"))
    } else {
      @Suppress("UNCHECKED_CAST")
      builder.data(data as D)
    }
    emit(response.isLast(true).build())
  }

  override fun dispose() {}

  fun apolloClient(): ApolloClient = ApolloClient.Builder()
    .networkTransport(this)
    .dispatcher(Dispatchers.Unconfined)
    .build()
}
