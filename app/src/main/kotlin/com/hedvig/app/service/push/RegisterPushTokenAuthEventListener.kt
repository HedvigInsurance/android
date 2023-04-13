package com.hedvig.app.service.push

import android.content.SharedPreferences
import arrow.core.Either
import arrow.core.continuations.either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.auth.event.AuthEventListener
import giraffe.NotificationRegisterDeviceMutation
import slimber.log.e
import slimber.log.i

class RegisterPushTokenAuthEventListener(
  private val apolloClient: ApolloClient,
  private val sharedPreferences: SharedPreferences,
) : AuthEventListener {

  override suspend fun loggedIn(accessToken: String) {
    val token = sharedPreferences.getString("notification_token", null)
    if (token != null) {
      registerPushToken(token)
    } else {
      e { "Could not find push token in shared preferences" }
    }
  }

  override suspend fun loggedOut() {

  }

 private suspend fun registerPushToken(pushToken: String): Either<OperationResult.Error, NotificationRegisterDeviceMutation.Data> {
    i { "Registering push token" }
    return either {
      apolloClient.mutation(NotificationRegisterDeviceMutation(pushToken)).safeExecute().toEither().bind()
    }
      .onLeft { queryResultError ->
        e { "Failed to register push token: $queryResultError" }
      }
      .onRight {
        i { "Successfully registered push token" }
      }
  }

}
