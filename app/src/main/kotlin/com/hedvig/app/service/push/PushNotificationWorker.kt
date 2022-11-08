package com.hedvig.app.service.push

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import arrow.core.Either
import arrow.core.continuations.either
import arrow.fx.coroutines.parZip
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.apollo.graphql.NotificationRegisterDeviceMutation
import com.hedvig.android.apollo.graphql.RegisterPushTokenMutation
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.app.authenticate.AuthenticationTokenService
import e
import i
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PushNotificationWorker(
  context: Context,
  params: WorkerParameters,
) : CoroutineWorker(context, params), KoinComponent {

  private val apolloClient: ApolloClient by inject()
  private val authenticationTokenService: AuthenticationTokenService by inject()

  override suspend fun doWork(): Result {
    val pushToken = inputData.getString(PUSH_TOKEN) ?: throw Exception("No token provided")
    if (!hasHedvigToken()) {
      return Result.retry()
    }
    return when (registerPushToken(pushToken)) {
      is Either.Left -> Result.retry()
      is Either.Right -> Result.success()
    }
  }

  private fun hasHedvigToken(): Boolean {
    try {
      val hedvigToken = authenticationTokenService.authenticationToken
      if (hedvigToken != null) {
        return true
      }
    } catch (exception: Exception) {
      e(exception)
    }
    return false
  }

  private suspend fun registerPushToken(pushToken: String): Either<OperationResult.Error, Unit> {
    i { "Registering push token" }
    return either {
      parZip(
        { apolloClient.mutation(RegisterPushTokenMutation(pushToken)).safeExecute().toEither().bind() },
        { apolloClient.mutation(NotificationRegisterDeviceMutation(pushToken)).safeExecute().toEither().bind() },
      ) { _, _ -> }
    }
      .tapLeft { queryResultError ->
        e { "Failed to register push token: $queryResultError" }
      }
      .tap {
        i { "Successfully registered push token" }
      }
  }

  companion object {
    const val PUSH_TOKEN = "push_token"
  }
}
