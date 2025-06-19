package com.hedvig.android.notification.badge.data.crosssell

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.logger.logcat
import octopus.CrossSellTypesQuery

/**
 * Returns a set of unique identifiers per cross-sell that exists for the current member as returned from the backend.
 */
interface GetCrossSellRecommendationIdUseCase {
  suspend fun invoke(): CrossSellIdentifier?
}

internal class GetCrossSellRecommendationIdUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetCrossSellRecommendationIdUseCase {
  override suspend fun invoke(): CrossSellIdentifier? {
    return apolloClient
      .query(CrossSellTypesQuery())
      .safeExecute()
      .fold(
        {
          logcat(throwable = it.throwable) {
            "Error when loading potential cross-sells: $it"
          }
          null
        },
        { data ->
          data.currentMember
            .crossSell.recommendedCrossSell?.crossSell?.id?.let { CrossSellIdentifier(it) }
        },
      )
  }
}

@JvmInline
value class CrossSellIdentifier(val rawValue: String) {
  /**
   * With GraphQL, we may get a cross-sell that we don't know about. This is a safety check to make sure that just
   * ignore such cases.
   */
  val isKnownCrossSell: Boolean
    get() = rawValue != "UNKNOWN__"
}
