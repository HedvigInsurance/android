package com.hedvig.app.feature.profile.data

import arrow.core.Either
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.apolloStore
import com.hedvig.android.owldroid.graphql.ProfileQuery
import com.hedvig.android.owldroid.graphql.SelectCashbackMutation
import com.hedvig.android.owldroid.graphql.UpdateEmailMutation
import com.hedvig.android.owldroid.graphql.UpdatePhoneNumberMutation
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeWatch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProfileRepository(
    private val apolloClient: ApolloClient,
) {
    private val profileQuery = ProfileQuery()

    fun profile(): Flow<Either<QueryResult.Error, ProfileQuery.Data>> = apolloClient
        .query(profileQuery)
        .safeWatch()
        .map(QueryResult<ProfileQuery.Data>::toEither)

    suspend fun updateEmail(input: String) =
        apolloClient.mutation(UpdateEmailMutation(input)).execute()

    suspend fun updatePhoneNumber(input: String) =
        apolloClient.mutation(UpdatePhoneNumberMutation(input)).execute()

    suspend fun writeEmailAndPhoneNumberInCache(email: String?, phoneNumber: String?) {
        val cachedData = apolloClient
            .apolloStore
            .readOperation(profileQuery)
        val newMember = cachedData
            .member
            .copy(
                email = email,
                phoneNumber = phoneNumber
            )

        val newData = cachedData
            .copy(member = newMember)

        apolloClient
            .apolloStore
            .writeOperation(profileQuery, newData)
    }

    suspend fun selectCashback(id: String) =
        apolloClient.mutation(SelectCashbackMutation(id)).execute()

    suspend fun writeCashbackToCache(cashback: SelectCashbackMutation.SelectCashbackOption) {
        val cachedData = apolloClient
            .apolloStore
            .readOperation(profileQuery)

        val newData = cachedData
            .copy(
                cashback = ProfileQuery.Cashback(
                    __typename = "Cashback",
                    fragments = ProfileQuery.Cashback.Fragments(cashback.fragments.cashbackFragment)
                )
            )

        apolloClient
            .apolloStore
            .writeOperation(profileQuery, newData)
    }
}
