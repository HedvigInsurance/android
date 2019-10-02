package com.hedvig.app.feature.profile.data

import com.apollographql.apollo.api.Response
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import com.apollographql.apollo.rx2.Rx2Apollo
import com.hedvig.android.owldroid.graphql.BankAccountQuery
import com.hedvig.android.owldroid.graphql.LogoutMutation
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

    fun updateEmail(input: String): Observable<Response<UpdateEmailMutation.Data>> = Rx2Apollo
        .from(apolloClientWrapper.apolloClient.mutate(UpdateEmailMutation(input = input)))

    fun updatePhoneNumber(input: String): Observable<Response<UpdatePhoneNumberMutation.Data>> =
        Rx2Apollo
            .from(apolloClientWrapper.apolloClient.mutate(UpdatePhoneNumberMutation(input = input)))

    fun writeEmailAndPhoneNumberInCache(email: String?, phoneNumber: String?) {
        val cachedData = apolloClientWrapper.apolloClient
            .apolloStore()
            .read(profileQuery)
            .execute()
        val newMemberBuilder = cachedData
            .member
            .copy(email = email, phoneNumber = phoneNumber)

        val newData = cachedData.copy(member = newMemberBuilder)

        apolloClientWrapper.apolloClient
            .apolloStore()
            .writeAndPublish(profileQuery, newData)
            .execute()
    }

    fun selectCashback(id: String): Observable<Response<SelectCashbackMutation.Data>> = Rx2Apollo
        .from(apolloClientWrapper.apolloClient.mutate(SelectCashbackMutation(id = id)))

    fun writeCashbackToCache(cashback: SelectCashbackMutation.SelectCashbackOption) {
        val cachedData = apolloClientWrapper.apolloClient
            .apolloStore()
            .read(profileQuery)
            .execute()

        val newData = cachedData.copy(
            cashback = ProfileQuery.Cashback(
                __typename = cashback.__typename,
                fragments = ProfileQuery.Cashback.Fragments(
                    cashbackFragment = cashback.fragments.cashbackFragment
                )
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

        val newCost = cachedData.insurance.cost?.copy(
            fragments = ProfileQuery.Cost.Fragments(costFragment = costFragment)
        )

        val newData = cachedData.copy(
            insurance = cachedData.insurance.copy(
                cost = newCost
            )
        )

        apolloClientWrapper.apolloClient
            .apolloStore()
            .writeAndPublish(profileQuery, newData)
            .execute()
    }

    fun startTrustlySession(): Observable<StartDirectDebitRegistrationMutation.Data> =
        Rx2Apollo
            .from(apolloClientWrapper.apolloClient.mutate(StartDirectDebitRegistrationMutation()))
            .map { it.data() }

    fun refreshBankAccountInfo(): Observable<Response<BankAccountQuery.Data>> =
        Rx2Apollo
            .from(
                apolloClientWrapper.apolloClient
                    .query(BankAccountQuery())
                    .responseFetcher(ApolloResponseFetchers.NETWORK_ONLY)
            )

    fun writeBankAccountInfoToCache(bankAccount: BankAccountQuery.BankAccount) {
        val cachedData = apolloClientWrapper.apolloClient
            .apolloStore()
            .read(profileQuery)
            .execute()

        val newBankAccount = ProfileQuery.BankAccount(
            __typename = bankAccount.__typename,
            bankName = bankAccount.bankName,
            descriptor = bankAccount.descriptor
        )

        val newData = cachedData.copy(
            bankAccount = newBankAccount
        )

        apolloClientWrapper.apolloClient
            .apolloStore()
            .writeAndPublish(profileQuery, newData)
            .execute()
    }

    fun logout() = Rx2Apollo.from(apolloClientWrapper.apolloClient.mutate(LogoutMutation()))
}
