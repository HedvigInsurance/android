package com.hedvig.app.feature.profile.data

import com.apollographql.apollo.api.Response
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
import com.hedvig.app.util.apollo.toDeferred
import com.hedvig.app.util.apollo.toFlow
import kotlinx.coroutines.flow.Flow
import org.jetbrains.annotations.Nullable

class ProfileRepository(private val apolloClientWrapper: ApolloClientWrapper) {
    private lateinit var profileQuery: ProfileQuery

    suspend fun fetchProfile(): @Nullable ProfileQuery.Data? {
        profileQuery = ProfileQuery()

        return apolloClientWrapper.apolloClient.query(profileQuery).toDeferred().await().data()
    }

    suspend fun refreshProfile() {
        apolloClientWrapper.apolloClient.clearNormalizedCache()
        fetchProfile()
    }

    suspend fun updateEmail(input: String): Response<UpdateEmailMutation.Data> {
        val updateEmailMutation = UpdateEmailMutation(input)

        return apolloClientWrapper.apolloClient.mutate(updateEmailMutation).toDeferred().await()
    }

    suspend fun updatePhoneNumber(input: String): Response<UpdatePhoneNumberMutation.Data> {
        val updatePhoneNumberMutation = UpdatePhoneNumberMutation(input)

        return apolloClientWrapper.apolloClient.mutate(updatePhoneNumberMutation).toDeferred()
            .await()
    }

    fun writeEmailAndPhoneNumberInCache(email: String?, phoneNumber: String?) {
        val cachedData = apolloClientWrapper.apolloClient
            .apolloStore()
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
            .apolloStore()
            .writeAndPublish(profileQuery, newData)
            .execute()
    }

    fun selectCashback(id: String): Flow<Response<SelectCashbackMutation.Data>> {
        val selectCashbackMutation = SelectCashbackMutation(id = id)

        return apolloClientWrapper.apolloClient.mutate(selectCashbackMutation).toFlow()
    }

    fun writeCashbackToCache(cashback: SelectCashbackMutation.SelectCashbackOption) {
        val cachedData = apolloClientWrapper.apolloClient
            .apolloStore()
            .read(profileQuery)
            .execute()

        val newData = cachedData
            .copy(
                cashback = ProfileQuery.Cashback(
                    fragments = ProfileQuery.Cashback.Fragments(cashback.fragments.cashbackFragment)
                )
            )

        apolloClientWrapper.apolloClient
            .apolloStore()
            .writeAndPublish(profileQuery, newData)
            .execute()
    }

    fun writeRedeemedCostToCache(data: RedeemReferralCodeMutation.Data) {
        val cachedData = apolloClientWrapper.apolloClient
            .apolloStore()
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
            .apolloStore()
            .writeAndPublish(profileQuery, newData)
            .execute()
    }

    suspend fun startTrustlySession(): @Nullable StartDirectDebitRegistrationMutation.Data? {
        val startDirectDebitRegistrationMutation = StartDirectDebitRegistrationMutation()

        return apolloClientWrapper.apolloClient.mutate(startDirectDebitRegistrationMutation)
            .toDeferred().await().data()
    }

    suspend fun refreshPayinMethod() {
        val response = apolloClientWrapper
            .apolloClient
            .query(PayinMethodQuery())
            .responseFetcher(ApolloResponseFetchers.NETWORK_ONLY)
            .toDeferred()
            .await()

        response.data()?.let { newData ->
            newData.bankAccount?.let { newBankAccount ->
                val cachedData = apolloClientWrapper
                    .apolloClient
                    .apolloStore()
                    .read(profileQuery)
                    .execute()

                apolloClientWrapper
                    .apolloClient
                    .apolloStore()
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
                    .apolloStore()
                    .read(profileQuery)
                    .execute()

                apolloClientWrapper
                    .apolloClient
                    .apolloStore()
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

    suspend fun logout() =
        apolloClientWrapper.apolloClient.mutate(LogoutMutation()).toDeferred().await()
}
