package com.hedvig.app.feature.profile.data

import arrow.core.Either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.ProfileQuery
import com.hedvig.android.owldroid.graphql.SelectCashbackMutation
import com.hedvig.android.owldroid.graphql.UpdateEmailMutation
import com.hedvig.android.owldroid.graphql.UpdatePhoneNumberMutation
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProfileRepository(
    private val apolloClient: ApolloClient,
) {
    private val profileQuery = ProfileQuery()

    fun profile(): Flow<Either<QueryResult.Error, ProfileQuery.Data>> = apolloClient
        .query(profileQuery)
        .watcher()
        .safeFlow()
        .map(QueryResult<ProfileQuery.Data>::toEither)

    suspend fun updateEmail(input: String) =
        apolloClient.mutate(UpdateEmailMutation(input)).await()

    suspend fun updatePhoneNumber(input: String) =
        apolloClient.mutate(UpdatePhoneNumberMutation(input)).await()

    fun writeEmailAndPhoneNumberInCache(email: String?, phoneNumber: String?) {
        val cachedData = apolloClient
            .apolloStore
            .read(profileQuery)
            .execute()
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
            .writeAndPublish(profileQuery, newData)
            .execute()
    }

    suspend fun selectCashback(id: String) =
        apolloClient.mutate(SelectCashbackMutation(id)).await()

    fun writeCashbackToCache(cashback: SelectCashbackMutation.SelectCashbackOption) {
        val cachedData = apolloClient
            .apolloStore
            .read(profileQuery)
            .execute()

        val newData = cachedData
            .copy(
                cashback = ProfileQuery.Cashback(
                    fragments = ProfileQuery.Cashback.Fragments(cashback.fragments.cashbackFragment)
                )
            )

        apolloClient
            .apolloStore
            .writeAndPublish(profileQuery, newData)
            .execute()
    }
}
