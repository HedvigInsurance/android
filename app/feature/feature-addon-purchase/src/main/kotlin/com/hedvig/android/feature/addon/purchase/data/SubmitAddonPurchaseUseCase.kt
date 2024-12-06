package com.hedvig.android.feature.addon.purchase.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.featureflags.FeatureManager
import kotlinx.datetime.LocalDate

internal interface SubmitAddonPurchaseUseCase {
  suspend fun invoke(quoteId: String, addonId: String): Either<ErrorMessage, LocalDate>
}

internal class SubmitAddonPurchaseUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val featureManager: FeatureManager,
): SubmitAddonPurchaseUseCase {
  override suspend fun invoke(
    quoteId: String,
    addonId: String,
  ): Either<ErrorMessage, LocalDate> {
    //TODO: REMOVE MOCK!
    return either {
      LocalDate(2025,1,1)
    }
  }

}
