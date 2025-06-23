package com.hedvig.android.notification.badge.data.crosssell

import com.apollographql.apollo.ApolloClient
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
      .safeFlow()
      .map { result ->
        result.fold(
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
