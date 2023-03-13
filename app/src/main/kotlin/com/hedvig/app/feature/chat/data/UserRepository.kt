package com.hedvig.app.feature.chat.data

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import giraffe.LogoutMutation

class UserRepository(
  private val apolloClient: ApolloClient,
) {
  suspend fun logout() = apolloClient.mutation(LogoutMutation()).safeExecute()
}
