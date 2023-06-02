package com.hedvig.android.feature.home.legacychangeaddress

import arrow.core.Either
import arrow.core.getOrElse
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import giraffe.ActiveContractBundlesQuery

internal class GetAddressChangeStoryIdUseCase(
  private val apolloClient: ApolloClient,
  private val createQuoteCartUseCase: CreateQuoteCartUseCase,
  private val featureManager: FeatureManager,
) {

  suspend fun invoke(): SelfChangeEligibilityResult {
    if (!featureManager.isFeatureEnabled(Feature.MOVING_FLOW)) {
      return SelfChangeEligibilityResult.Blocked
    }
    val activeContractBundlesQueryData = apolloClient.query(ActiveContractBundlesQuery())
      .safeExecute()
      .toEither()
      .getOrElse { errorQueryResult ->
        return SelfChangeEligibilityResult.Error(errorQueryResult.message)
      }

    val storyId = activeContractBundlesQueryData.activeContractBundles
      .firstOrNull()
      ?.angelStories
      ?.addressChangeV2
      ?: return SelfChangeEligibilityResult.Blocked

    val storyIdWithQuoteCartId = addQuoteCartId(storyId).getOrElse { errorMessage ->
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

// const val QUOTE_CART_EMBARK_STORE_ID_KEY = "quoteCartId" // from :app module
fun appendQuoteCartId(embarkStoryId: String, quoteCartId: String) = if (embarkStoryId.contains("?")) {
  "$embarkStoryId&quoteCartId=$quoteCartId"
} else {
  "$embarkStoryId?quoteCartId=$quoteCartId"
}
