package com.hedvig.android.feature.businessmodel

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.exception.ApolloException
import com.hedvig.android.apollo.graphql.CharityInformationQuery

internal class GetBusinessModelInformationUseCase(
  private val apolloClient: ApolloClient,
) {
  suspend fun invoke(): BusinessModelInformation? {
    return try {
      val cashback = apolloClient
        .query(CharityInformationQuery())
        .execute()
        .dataAssertNoErrors
        .cashback
      val cashbackName = cashback?.name
      if (cashbackName == null) {
        null
      } else {
        BusinessModelInformation(
          cashbackName,
          cashback.description,
          cashback.imageUrl,
        )
      }
    } catch (exception: ApolloException) {
      null
    }
  }
}

data class BusinessModelInformation(
  val name: String,
  val description: String?,
  val imageUrl: String?,
)
