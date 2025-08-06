package com.hedvig.android.notification.badge.data.crosssell

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeFlow
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import octopus.CrossSellTypesQuery

/**
 * Returns a set of unique identifiers per cross-sell that exists for the current member as returned from the backend.
 */
interface GetCrossSellRecommendationIdUseCase {
  fun invoke(): Flow<CrossSellIdentifier?>
}

internal class GetCrossSellRecommendationIdUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetCrossSellRecommendationIdUseCase {
  override fun invoke(): Flow<CrossSellIdentifier?> {
    return apolloClient
      .query(CrossSellTypesQuery())
      .fetchPolicy(FetchPolicy.NetworkOnly)
      .safeFlow()
      .map { result ->
        result.fold(
          {
            logcat(operationError = it) {
              "Error when loading potential cross-sells: $it"
            }
            null
          },
          { data ->
            val result = data.currentMember
              .crossSell.recommendedCrossSell?.crossSell?.id?.let {
                CrossSellIdentifier(it)
              }
            result
          },
        )
      }
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
