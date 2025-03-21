package com.hedvig.android.feature.payments.overview.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import octopus.AddDiscountMutation

internal interface AddDiscountUseCase {
  suspend fun invoke(code: String): Either<DiscountError, DiscountSuccess>
}

internal data class AddDiscountUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val cacheManager: NetworkCacheManager,
) : AddDiscountUseCase {
  override suspend fun invoke(code: String): Either<ErrorMessage, DiscountSuccess> = either {
  override suspend fun invoke(code: String): Either<DiscountError, DiscountSuccess> = either {
    val result = apolloClient.mutation(AddDiscountMutation(code))
      .safeExecute(::ErrorMessage)
      .mapLeft(DiscountError::GenericError)
      .bind()

    if (result.memberCampaignsRedeem.userError != null) {
      val userErrorMessage = result.memberCampaignsRedeem.userError.message
      if (userErrorMessage != null) {
        raise(DiscountError.UserError(userErrorMessage))
      } else {
        raise(DiscountError.GenericError(ErrorMessage(userErrorMessage)))
      }
    }

    cacheManager.clearCache()

    DiscountSuccess
  }
}

internal data object DiscountSuccess

internal sealed interface DiscountError {
  data class UserError(val message: String) : DiscountError

  data class GenericError(val errorMessage: ErrorMessage) : DiscountError, ErrorMessage by errorMessage
}
