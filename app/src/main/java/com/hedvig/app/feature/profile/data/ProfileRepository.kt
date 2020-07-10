package com.hedvig.app.feature.profile.data

import com.apollographql.apollo.api.Response
import com.apollographql.apollo.coroutines.toDeferred
import com.apollographql.apollo.coroutines.toFlow
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import com.hedvig.android.owldroid.graphql.LogoutMutation
import com.hedvig.android.owldroid.graphql.PayinMethodQuery
import com.hedvig.android.owldroid.graphql.ProfileQuery
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.android.owldroid.graphql.SelectCashbackMutation
import com.hedvig.android.owldroid.graphql.StartDirectDebitRegistrationMutation
import com.hedvig.android.owldroid.graphql.UpdateEmailMutation
import com.hedvig.android.owldroid.graphql.UpdatePhoneNumberMutation
import com.hedvig.app.ApolloClientWrapper
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow

class ProfileRepository(private val apolloClientWrapper: ApolloClientWrapper) {
    private val profileQuery = ProfileQuery()

    fun profile() = apolloClientWrapper
        .apolloClient
        .query(profileQuery)
        .watcher()
        .toFlow()

    fun refreshProfileAsync() = apolloClientWrapper
        .apolloClient
        .query(profileQuery)
        .responseFetcher(ApolloResponseFetchers.NETWORK_ONLY)
        .toDeferred()

    fun updateEmailAsync(input: String): Deferred<Response<UpdateEmailMutation.Data>> {
        val updateEmailMutation = UpdateEmailMutation(input)

        return apolloClientWrapper.apolloClient.mutate(updateEmailMutation).toDeferred()
    }

    fun updatePhoneNumberAsync(input: String): Deferred<Response<UpdatePhoneNumberMutation.Data>> {
        val updatePhoneNumberMutation = UpdatePhoneNumberMutation(input)

        return apolloClientWrapper.apolloClient.mutate(updatePhoneNumberMutation).toDeferred()
    }

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

    fun selectCashback(id: String): Flow<Response<SelectCashbackMutation.Data>> {
        val selectCashbackMutation = SelectCashbackMutation(id = id)

        return apolloClientWrapper.apolloClient.mutate(selectCashbackMutation).toFlow()
    }

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

    fun startTrustlySessionAsync(): Deferred<Response<StartDirectDebitRegistrationMutation.Data>> {
        val startDirectDebitRegistrationMutation = StartDirectDebitRegistrationMutation()

        return apolloClientWrapper.apolloClient.mutate(startDirectDebitRegistrationMutation)
            .toDeferred()
    }

    suspend fun refreshPayinMethod() {
        val response = apolloClientWrapper
            .apolloClient
            .query(PayinMethodQuery())
            .responseFetcher(ApolloResponseFetchers.NETWORK_ONLY)
            .toDeferred()
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
