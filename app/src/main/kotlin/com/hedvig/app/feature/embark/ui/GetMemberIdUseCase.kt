package com.hedvig.app.feature.embark.ui

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.apollo.safeExecute
import giraffe.MemberIdQuery

class GetMemberIdUseCase(
  private val apolloClient: ApolloClient,
) {
  suspend fun memberId() = when (val result = apolloClient.query(MemberIdQuery()).safeExecute()) {
    is OperationResult.Error -> MemberIdResult.Error
    is OperationResult.Success -> {
      val id = result.data.member.id
      if (id != null) {
        MemberIdResult.Success(id)
      } else {
        MemberIdResult.Error
      }
    }
  }

  sealed class MemberIdResult {
    data class Success(val id: String) : MemberIdResult()
    object Error : MemberIdResult()
  }
}
