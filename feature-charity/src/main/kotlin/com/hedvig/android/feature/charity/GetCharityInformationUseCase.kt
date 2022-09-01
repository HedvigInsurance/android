package com.hedvig.android.feature.charity

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.exception.ApolloException
import com.hedvig.android.apollo.graphql.CharityInformationQuery

internal class GetCharityInformationUseCase(
  private val apolloClient: ApolloClient,
) {
  suspend fun invoke(): CharityInformation? {
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
        CharityInformation(
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

data class CharityInformation(
  val name: String,
  val description: String?,
  val imageUrl: String?,
)
