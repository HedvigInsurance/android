package com.hedvig.app.feature.home.ui.changeaddress

import arrow.core.Either
import arrow.core.getOrHandle
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.graphql.ActiveContractBundlesQuery
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.app.feature.embark.QUOTE_CART_ID_KEY
import com.hedvig.app.feature.embark.quotecart.CreateQuoteCartUseCase
import com.hedvig.app.util.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither

class GetAddressChangeStoryIdUseCase(
  private val createQuoteCartUseCase: CreateQuoteCartUseCase,
  private val apolloClient: ApolloClient,
  private val featureManager: FeatureManager,
) {

  suspend fun invoke(): SelfChangeEligibilityResult {
    if (!featureManager.isFeatureEnabled(Feature.MOVING_FLOW)) {
      return SelfChangeEligibilityResult.Blocked
    }
    val activeContractBundlesQueryData = apolloClient.query(ActiveContractBundlesQuery())
      .safeExecute()
      .toEither()
      .getOrHandle { errorQueryResult ->
        return SelfChangeEligibilityResult.Error(errorQueryResult.message)
      }

    val storyId = activeContractBundlesQueryData.activeContractBundles
      .firstOrNull()
      ?.angelStories
      ?.addressChangeV2
      ?: return SelfChangeEligibilityResult.Blocked

    val storyIdWithQuoteCartId = addQuoteCartId(storyId).getOrHandle { errorMessage ->
      return SelfChangeEligibilityResult.Error(errorMessage.message)
    }
    return SelfChangeEligibilityResult.Eligible(storyIdWithQuoteCartId)
  }

  private suspend fun addQuoteCartId(storyId: String): Either<ErrorMessage, String> {
    return createQuoteCartUseCase.invoke().map { quoteCartId -> appendQuoteCartId(storyId, quoteCartId.id) }
  }

  sealed class SelfChangeEligibilityResult {
    data class Eligible(val embarkStoryId: String) : SelfChangeEligibilityResult()
    object Blocked : SelfChangeEligibilityResult()
    data class Error(val message: String?) : SelfChangeEligibilityResult()
  }
}

fun appendQuoteCartId(embarkStoryId: String, quoteCartId: String) = if (embarkStoryId.contains("?")) {
  "$embarkStoryId&$QUOTE_CART_ID_KEY=$quoteCartId"
} else {
  "$embarkStoryId?$QUOTE_CART_ID_KEY=$quoteCartId"
}
