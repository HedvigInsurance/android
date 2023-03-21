package com.hedvig.app.service.push

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import arrow.core.Either
import arrow.core.continuations.either
import arrow.fx.coroutines.parZip
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.apollo.giraffe.di.giraffeClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.auth.AuthTokenService
import giraffe.NotificationRegisterDeviceMutation
import giraffe.RegisterPushTokenMutation
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import slimber.log.e
import slimber.log.i

class PushNotificationWorker(
  context: Context,
  params: WorkerParameters,
) : CoroutineWorker(context, params), KoinComponent {

  private val apolloClient: ApolloClient by inject(giraffeClient)
  private val authTokenService: AuthTokenService by inject()

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

  private suspend fun hasHedvigToken(): Boolean {
    try {
      val hedvigToken = authTokenService.getTokens()
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
      .onLeft { queryResultError ->
        e { "Failed to register push token: $queryResultError" }
      }
      .onRight {
        i { "Successfully registered push token" }
      }
  }

  companion object {
    const val PUSH_TOKEN = "push_token"
  }
}
