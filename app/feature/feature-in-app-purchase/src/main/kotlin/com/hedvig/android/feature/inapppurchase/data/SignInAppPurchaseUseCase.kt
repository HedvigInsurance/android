package com.hedvig.android.feature.inapppurchase.data

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import octopus.InAppPurchaseSignMutation

/**
 * Starts a BankID signing of [offerId]. Poll the returned signing with [GetInAppPurchaseSigningUseCase].
 */
internal interface SignInAppPurchaseUseCase {
  suspend fun invoke(shopSessionId: String, offerId: String): Either<ErrorMessage, InAppPurchaseSigning>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class SignInAppPurchaseUseCaseImpl(
  private val apolloClient: ApolloClient,
) : SignInAppPurchaseUseCase {
  override suspend fun invoke(shopSessionId: String, offerId: String): Either<ErrorMessage, InAppPurchaseSigning> {
    return either {
      val response = apolloClient
        .mutation(InAppPurchaseSignMutation(shopSessionId = shopSessionId, offerId = offerId))
        .safeExecute()
        .onLeft { logcat(LogPriority.ERROR, it) { "InAppPurchaseSignMutation for $shopSessionId failed" } }
        .mapLeft { ErrorMessage() }
        .bind()
        .inAppPurchaseSign
      response.userError?.let { raise(ErrorMessage(it.message)) }
      val signing = ensureNotNull(response.signing) { ErrorMessage() }
      signing.toInAppPurchaseSigning()
    }
  }
}
