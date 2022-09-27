package com.hedvig.app.feature.profile.ui.payment

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.apolloStore
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.apollographql.apollo3.cache.normalized.watch
import com.hedvig.android.apollo.graphql.PaymentQuery
import com.hedvig.android.apollo.graphql.type.PayoutMethodStatus
import com.hedvig.android.language.LanguageService
import kotlinx.coroutines.flow.Flow

class PaymentRepository(
  private val apolloClient: ApolloClient,
  languageService: LanguageService,
) {
  private val paymentQuery = PaymentQuery(languageService.getGraphQLLocale())
  fun payment(): Flow<ApolloResponse<PaymentQuery.Data>> = apolloClient
    .query(paymentQuery)
    .watch()

  suspend fun refresh(): ApolloResponse<PaymentQuery.Data> = apolloClient
    .query(paymentQuery)
    .fetchPolicy(FetchPolicy.NetworkOnly)
    .execute()

  suspend fun writeActivePayoutMethodStatus(status: PayoutMethodStatus) {
    val cachedData = apolloClient
      .apolloStore
      .readOperation(paymentQuery)

    apolloClient
      .apolloStore
      .writeOperation(
        paymentQuery,
        cachedData.copy(
          activePayoutMethods = PaymentQuery.ActivePayoutMethods(status = status),
        ),
      )
  }
}
