package com.hedvig.app.feature.profile.data

import com.apollographql.apollo.api.Response
import com.apollographql.apollo.coroutines.toDeferred
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import com.apollographql.apollo.rx2.Rx2Apollo
import com.hedvig.android.owldroid.graphql.LogoutMutation
import com.hedvig.android.owldroid.graphql.PayinMethodQuery
import com.hedvig.android.owldroid.graphql.ProfileQuery
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.android.owldroid.graphql.SelectCashbackMutation
import com.hedvig.android.owldroid.graphql.StartDirectDebitRegistrationMutation
import com.hedvig.android.owldroid.graphql.UpdateEmailMutation
import com.hedvig.android.owldroid.graphql.UpdatePhoneNumberMutation
import com.hedvig.app.ApolloClientWrapper
import io.reactivex.Observable

class ProfileRepository(private val apolloClientWrapper: ApolloClientWrapper) {
    private lateinit var profileQuery: ProfileQuery
    fun fetchProfile(): Observable<ProfileQuery.Data?> {
        profileQuery = ProfileQuery()

        return Rx2Apollo
            .from(apolloClientWrapper.apolloClient.query(profileQuery).watcher())
            .map { it.data() }
    }

    fun refreshProfile() {
        apolloClientWrapper.apolloClient.clearNormalizedCache()
        fetchProfile()
    }

    fun updateEmail(input: String): Observable<Response<UpdateEmailMutation.Data>> {
        val updateEmailMutation = UpdateEmailMutation(input)

        return Rx2Apollo
            .from(apolloClientWrapper.apolloClient.mutate(updateEmailMutation))
    }

    fun updatePhoneNumber(input: String): Observable<Response<UpdatePhoneNumberMutation.Data>> {
        val updatePhoneNumberMutation = UpdatePhoneNumberMutation(input)

        return Rx2Apollo
            .from(apolloClientWrapper.apolloClient.mutate(updatePhoneNumberMutation))
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

    fun selectCashback(id: String): Observable<Response<SelectCashbackMutation.Data>> {
        val selectCashbackMutation = SelectCashbackMutation(id = id)

        return Rx2Apollo
            .from(apolloClientWrapper.apolloClient.mutate(selectCashbackMutation))
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

    fun startTrustlySession(): Observable<StartDirectDebitRegistrationMutation.Data> {
        val startDirectDebitRegistrationMutation = StartDirectDebitRegistrationMutation()

        return Rx2Apollo
            .from(apolloClientWrapper.apolloClient.mutate(startDirectDebitRegistrationMutation))
            .map { it.data() }
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

    fun logout() = Rx2Apollo.from(apolloClientWrapper.apolloClient.mutate(LogoutMutation()))
}
