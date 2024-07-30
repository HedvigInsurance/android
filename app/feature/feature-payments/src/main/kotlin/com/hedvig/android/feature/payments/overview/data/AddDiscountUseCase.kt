package com.hedvig.android.feature.payments.overview.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import octopus.AddDiscountMutation

internal interface AddDiscountUseCase {
  suspend fun invoke(code: String): Either<ErrorMessage, DiscountSuccess>
}

internal data class AddDiscountUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val cacheManager: NetworkCacheManager,
) : AddDiscountUseCase {
  override suspend fun invoke(code: String): Either<ErrorMessage, DiscountSuccess> = either {
    val result = apolloClient.mutation(AddDiscountMutation(code))
      .safeExecute()
      .toEither(::ErrorMessage)
      .bind()

    if (result.memberCampaignsRedeem.userError != null) {
      raise(ErrorMessage(result.memberCampaignsRedeem.userError.message))
    }

    cacheManager.clearCache()

    DiscountSuccess
  }
}

internal data object DiscountSuccess
