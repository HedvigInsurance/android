package com.hedvig.android.notification.badge.data.crosssell

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.logger.logcat
import octopus.CrossSellTypesQuery

/**
 * Returns a set of unique identifiers per cross-sell that exists for the current member as returned from the backend.
 */
interface GetCrossSellIdentifiersUseCase {
  suspend fun invoke(): Set<CrossSellIdentifier>
}

internal class GetCrossSellIdentifiersUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetCrossSellIdentifiersUseCase {
  override suspend fun invoke(): Set<CrossSellIdentifier> {
    return apolloClient
      .query(CrossSellTypesQuery())
      .safeExecute()
      .fold(
        {
          logcat(throwable = it.throwable) {
            "Error when loading potential cross-sells: $it"
          }
          emptySet()
        },
        { data ->
          data.currentMember.crossSells.map { it.type.rawValue }.map(::CrossSellIdentifier).toSet()
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
