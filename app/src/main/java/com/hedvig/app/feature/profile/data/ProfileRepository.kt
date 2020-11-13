package com.hedvig.app.feature.profile.data

import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.coroutines.toDeferred
import com.apollographql.apollo.coroutines.toFlow
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import com.hedvig.android.owldroid.graphql.LogoutMutation
import com.hedvig.android.owldroid.graphql.PayinMethodQuery
import com.hedvig.android.owldroid.graphql.ProfileQuery
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.android.owldroid.graphql.SelectCashbackMutation
import com.hedvig.android.owldroid.graphql.UpdateEmailMutation
import com.hedvig.android.owldroid.graphql.UpdatePhoneNumberMutation
import com.hedvig.app.ApolloClientWrapper

class ProfileRepository(private val apolloClientWrapper: ApolloClientWrapper) {
    private val profileQuery = ProfileQuery()

    fun profile() = apolloClientWrapper
        .apolloClient
        .query(profileQuery)
        .watcher()
        .toFlow()

    suspend fun refreshProfile() = apolloClientWrapper
        .apolloClient
        .query(profileQuery)
        .toBuilder()
        .responseFetcher(ApolloResponseFetchers.NETWORK_ONLY)
        .build()
        .await()

    suspend fun updateEmail(input: String) =
        apolloClientWrapper.apolloClient.mutate(UpdateEmailMutation(input)).await()

    suspend fun updatePhoneNumber(input: String) =
        apolloClientWrapper.apolloClient.mutate(UpdatePhoneNumberMutation(input)).await()

    fun writeEmailAndPhoneNumberInCache(email: String?, phoneNumber: String?) {
        val cachedData = apolloClientWrapper.apolloClient
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

        apolloClientWrapper.apolloClient
            .apolloStore
            .writeAndPublish(profileQuery, newData)
            .execute()
    }

    suspend fun selectCashback(id: String) =
        apolloClientWrapper.apolloClient.mutate(SelectCashbackMutation(id)).await()

    fun writeCashbackToCache(cashback: SelectCashbackMutation.SelectCashbackOption) {
        val cachedData = apolloClientWrapper.apolloClient
            .apolloStore
            .read(profileQuery)
            .execute()

        val newData = cachedData
            .copy(
                cashback = ProfileQuery.Cashback(
                    fragments = ProfileQuery.Cashback.Fragments(cashback.fragments.cashbackFragment)
                )
            )

        apolloClientWrapper.apolloClient
            .apolloStore
            .writeAndPublish(profileQuery, newData)
            .execute()
    }

    fun writeRedeemedCostToCache(data: RedeemReferralCodeMutation.Data) {
        val cachedData = apolloClientWrapper.apolloClient
            .apolloStore
            .read(profileQuery)
            .execute()

        val costFragment = data.redeemCode.cost.fragments.costFragment

        val newCost = cachedData.insuranceCost?.copy(
            fragments = ProfileQuery.InsuranceCost.Fragments(costFragment = costFragment)
        )

        val newData = cachedData
            .copy(
                insuranceCost = newCost
            )

        apolloClientWrapper.apolloClient
            .apolloStore
            .writeAndPublish(profileQuery, newData)
            .execute()
    }

    suspend fun refreshPayinMethod() {
        val response = apolloClientWrapper
            .apolloClient
            .query(PayinMethodQuery())
            .toBuilder()
            .responseFetcher(ApolloResponseFetchers.NETWORK_ONLY)
            .build()
            .await()

        response.data?.let { newData ->
            newData.bankAccount?.let { newBankAccount ->
                val cachedData = apolloClientWrapper
                    .apolloClient
                    .apolloStore
                    .read(profileQuery)
                    .execute()

                apolloClientWrapper
                    .apolloClient
                    .apolloStore
                    .writeAndPublish(
                        profileQuery,
                        cachedData.copy(
                            bankAccount = ProfileQuery.BankAccount(
                                fragments = ProfileQuery.BankAccount.Fragments(newBankAccount.fragments.bankAccountFragment)
                            )
                        )
                    )
                    .execute()
            }

            newData.activePaymentMethods?.let { newActivePaymentMethods ->
                val cachedData = apolloClientWrapper
                    .apolloClient
                    .apolloStore
                    .read(profileQuery)
                    .execute()

                apolloClientWrapper
                    .apolloClient
                    .apolloStore
                    .writeAndPublish(
                        profileQuery,
                        cachedData.copy(
                            activePaymentMethods = ProfileQuery.ActivePaymentMethods(
                                fragments = ProfileQuery.ActivePaymentMethods.Fragments(
                                    newActivePaymentMethods.fragments.activePaymentMethodsFragment
                                )
                            )
                        )
                    )
                    .execute()
            }
        }
    }

    fun logoutAsync() =
        apolloClientWrapper.apolloClient.mutate(LogoutMutation()).toDeferred()
}
