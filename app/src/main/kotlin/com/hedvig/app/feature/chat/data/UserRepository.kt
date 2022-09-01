package com.hedvig.app.feature.chat.data

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.graphql.AuthStatusSubscription
import com.hedvig.android.apollo.graphql.LogoutMutation
import com.hedvig.android.apollo.graphql.SwedishBankIdAuthMutation
import com.hedvig.app.util.apollo.safeQuery

class UserRepository(
  private val apolloClient: ApolloClient,
) {
  suspend fun fetchAutoStartToken() =
    apolloClient.mutation(SwedishBankIdAuthMutation()).execute()

  fun subscribeAuthStatus() =
    apolloClient.subscription(AuthStatusSubscription()).toFlow()

  suspend fun logout() = apolloClient.mutation(LogoutMutation()).safeQuery()
}
