package com.hedvig.android.feature.inapppurchase.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import octopus.InAppPurchaseSigningQuery
import octopus.fragment.InAppPurchaseSigningFragment
import octopus.type.ShopSessionSigningStatus

internal interface GetInAppPurchaseSigningUseCase {
  suspend fun invoke(signingId: String): Either<ErrorMessage, InAppPurchaseSigning>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class GetInAppPurchaseSigningUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetInAppPurchaseSigningUseCase {
  override suspend fun invoke(signingId: String): Either<ErrorMessage, InAppPurchaseSigning> {
    return either {
      apolloClient
        .query(InAppPurchaseSigningQuery(signingId))
        .fetchPolicy(FetchPolicy.NetworkOnly)
        .safeExecute()
        .onLeft { logcat(LogPriority.ERROR, it) { "InAppPurchaseSigningQuery for $signingId failed" } }
        .mapLeft { ErrorMessage() }
        .bind()
        .shopSessionSigning
        .toInAppPurchaseSigning()
    }
  }
}

internal fun InAppPurchaseSigningFragment.toInAppPurchaseSigning() = InAppPurchaseSigning(
  id = id,
  status = when (status) {
    ShopSessionSigningStatus.PENDING -> InAppPurchaseSigningStatus.PENDING

    ShopSessionSigningStatus.CREATING -> InAppPurchaseSigningStatus.CREATING

    ShopSessionSigningStatus.SIGNED -> InAppPurchaseSigningStatus.SIGNED

    ShopSessionSigningStatus.FAILED,
    ShopSessionSigningStatus.UNKNOWN__,
    -> InAppPurchaseSigningStatus.FAILED
  },
  autoStartToken = seBankidProperties?.autoStartToken,
  liveQrCodeData = seBankidProperties?.liveQrCodeData,
  bankIdAppOpened = seBankidProperties?.bankidAppOpened == true,
  failureMessage = userError?.message,
)
