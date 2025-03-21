package com.hedvig.android.feature.payments.overview.data

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import kotlinx.coroutines.flow.first
import octopus.AddDiscountMutation

internal interface AddDiscountUseCase {
  suspend fun invoke(code: String): Either<DiscountError, DiscountSuccess>
}

internal data class AddDiscountUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val cacheManager: NetworkCacheManager,
  private val featureManager: FeatureManager,
) : AddDiscountUseCase {
  override suspend fun invoke(code: String): Either<DiscountError, DiscountSuccess> = either {
    ensure(featureManager.isFeatureEnabled(Feature.DISABLE_REDEEM_CAMPAIGN).first() == true) {
      DiscountError.GenericError(ErrorMessage("Redeeming a campaign feature is disabled"))
    }
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
