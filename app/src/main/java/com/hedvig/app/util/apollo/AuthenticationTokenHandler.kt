package com.hedvig.app.util.apollo

import android.content.SharedPreferences
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.NewSessionMutation
import e
import i
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

const val SHARED_PREFERENCE_AUTHENTICATION_TOKEN = "shared_preference_authentication_token"

class AuthenticationTokenHandler(
    private val apolloClient: ApolloClient,
    private val sharedPreferences: SharedPreferences
) {
    private val mutex = Mutex()

    suspend fun acquireAuthenticationToken() {
        mutex.withLock {
            if (hasAuthenticationToken()) {
                return
            }

            val response = runCatching {
                apolloClient.mutate(NewSessionMutation()).await()
            }
            if (response.isFailure) {
                response.exceptionOrNull()?.let { e { "Failed to register a hedvig token: $it" } }
                return
            }

            response.getOrNull()?.data?.createSessionV2?.token?.let { hedvigToken ->
                sharedPreferences.edit().putString(SHARED_PREFERENCE_AUTHENTICATION_TOKEN, hedvigToken).apply()
                apolloClient.subscriptionManager.reconnect()
                i { "Successfully saved hedvig token" }
            } ?: e { "createSession returned no token" }
        }
    }

    fun getStoredAuthenticationToken() = sharedPreferences.getString(SHARED_PREFERENCE_AUTHENTICATION_TOKEN, null)

    fun hasAuthenticationToken() = getStoredAuthenticationToken() != null

    suspend fun removeAuthenticationToken() {
        mutex.withLock {
            sharedPreferences.edit().putString(SHARED_PREFERENCE_AUTHENTICATION_TOKEN, null).apply()
        }
    }
}
