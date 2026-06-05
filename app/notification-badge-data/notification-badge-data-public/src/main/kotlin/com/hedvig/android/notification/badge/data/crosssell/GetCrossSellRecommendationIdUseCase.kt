package com.hedvig.android.notification.badge.data.crosssell

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeFlow
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import octopus.CrossSellTypesQuery

/**
 * Returns a set of unique identifiers per cross-sell that exists for the current member as returned from the backend.
 */
internal interface GetCrossSellRecommendationIdUseCase {
  fun invoke(): Flow<CrossSellIdentifier?>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
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
              .crossSellV2.recommendedCrossSell?.crossSell?.id?.let {
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
