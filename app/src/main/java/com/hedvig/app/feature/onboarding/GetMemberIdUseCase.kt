package com.hedvig.app.feature.onboarding

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.MemberIdQuery
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery

class GetMemberIdUseCase(
    private val apolloClient: ApolloClient
) {
    suspend fun memberId() = when (val result = apolloClient.query(MemberIdQuery()).safeQuery()) {
        is QueryResult.Error -> MemberIdResult.Error
        is QueryResult.Success -> {
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
