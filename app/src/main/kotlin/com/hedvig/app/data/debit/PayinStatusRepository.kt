package com.hedvig.app.data.debit

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.apolloStore
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.apollographql.apollo3.cache.normalized.watch
import com.hedvig.android.apollo.graphql.PayinStatusQuery
import kotlinx.coroutines.flow.Flow

class PayinStatusRepository(
  private val apolloClient: ApolloClient,
) {
  private val payinStatusQuery = PayinStatusQuery()

  fun payinStatusFlow(): Flow<ApolloResponse<PayinStatusQuery.Data>> = apolloClient
    .query(payinStatusQuery)
    .watch()

  suspend fun refreshPayinStatus() {
    val response = apolloClient
      .query(payinStatusQuery)
      .fetchPolicy(FetchPolicy.NetworkOnly)
      .execute()

    response.data?.let { data ->
      val cachedData = apolloClient
        .apolloStore
        .readOperation(payinStatusQuery)

      val newData = cachedData.copy(payinMethodStatus = data.payinMethodStatus)
      apolloClient
        .apolloStore
        .writeOperation(payinStatusQuery, newData)
    }
  }
}
