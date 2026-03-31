package com.hedvig.android.feature.purchase.apartment.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import octopus.ApartmentShopSessionSigningQuery
import octopus.type.ShopSessionSigningStatus

internal interface PollSigningStatusUseCase {
  suspend fun invoke(signingId: String): Either<ErrorMessage, SigningPollResult>
}

internal class PollSigningStatusUseCaseImpl(
  private val apolloClient: ApolloClient,
) : PollSigningStatusUseCase {
  override suspend fun invoke(signingId: String): Either<ErrorMessage, SigningPollResult> {
    return either {
      apolloClient
        .query(ApartmentShopSessionSigningQuery(signingId = signingId))
        .fetchPolicy(FetchPolicy.NetworkOnly)
        .safeExecute()
        .fold(
          ifLeft = {
            logcat(LogPriority.ERROR) { "Failed to poll signing status: $it" }
            raise(ErrorMessage())
          },
          ifRight = { result ->
            val signing = result.shopSessionSigning
            val status = when (signing.status) {
              ShopSessionSigningStatus.SIGNED -> SigningStatus.SIGNED

              ShopSessionSigningStatus.FAILED -> SigningStatus.FAILED

              ShopSessionSigningStatus.PENDING,
              ShopSessionSigningStatus.CREATING,
              ShopSessionSigningStatus.UNKNOWN__,
              -> SigningStatus.PENDING
            }
            SigningPollResult(
              status = status,
              liveQrCodeData = signing.seBankidProperties?.liveQrCodeData,
            )
          },
        )
    }
  }
}
