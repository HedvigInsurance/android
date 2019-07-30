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
        profileQuery = ProfileQuery
            .builder()
            .build()

        return Rx2Apollo
            .from(apolloClientWrapper.apolloClient.query(profileQuery).watcher())
            .map { it.data() }
    }

    fun refreshProfile() {
        apolloClientWrapper.apolloClient.clearNormalizedCache()
        fetchProfile()
    }

    fun updateEmail(input: String): Observable<Response<UpdateEmailMutation.Data>> {
        val updateEmailMutation = UpdateEmailMutation
            .builder()
            .input(input)
            .build()

        return Rx2Apollo
            .from(apolloClientWrapper.apolloClient.mutate(updateEmailMutation))
    }

    fun updatePhoneNumber(input: String): Observable<Response<UpdatePhoneNumberMutation.Data>> {
        val updatePhoneNumberMutation = UpdatePhoneNumberMutation
            .builder()
            .input(input)
            .build()

        return Rx2Apollo
            .from(apolloClientWrapper.apolloClient.mutate(updatePhoneNumberMutation))
    }

    fun writeEmailAndPhoneNumberInCache(email: String?, phoneNumber: String?) {
        val cachedData = apolloClientWrapper.apolloClient
            .apolloStore()
            .read(profileQuery)
            .execute()
        val newMemberBuilder = cachedData
            .member
            .toBuilder()

        email?.let { newMemberBuilder.email(it) }
        phoneNumber?.let { newMemberBuilder.phoneNumber(it) }

        val newData = cachedData
            .toBuilder()
            .member(newMemberBuilder.build())
            .build()

        apolloClientWrapper.apolloClient
            .apolloStore()
            .writeAndPublish(profileQuery, newData)
            .execute()
    }

    fun selectCashback(id: String): Observable<Response<SelectCashbackMutation.Data>> {
        val selectCashbackMutation = SelectCashbackMutation
            .builder()
            .id(id)
            .build()

        return Rx2Apollo
            .from(apolloClientWrapper.apolloClient.mutate(selectCashbackMutation))
    }

    fun writeCashbackToCache(cashback: SelectCashbackMutation.SelectCashbackOption) {
        val cachedData = apolloClientWrapper.apolloClient
            .apolloStore()
            .read(profileQuery)
            .execute()

        val newCashback = ProfileQuery.Cashback
            .builder()
            .__typename(cashback.__typename)
            .name(cashback.name)
            .imageUrl(cashback.imageUrl)
            .paragraph(cashback.paragraph)
            .build()

        val newData = cachedData
            .toBuilder()
            .cashback(newCashback)
            .build()

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

        val newCost = cachedData.insurance.cost?.toBuilder()
            ?.fragments(ProfileQuery.Cost.Fragments.builder().costFragment(costFragment).build())?.build()

        val newData = cachedData
            .toBuilder()
            .insurance(
                cachedData.insurance.toBuilder().cost(newCost).build()
            )
            .build()

        apolloClientWrapper.apolloClient
            .apolloStore()
            .writeAndPublish(profileQuery, newData)
            .execute()
    }

    fun startTrustlySession(): Observable<StartDirectDebitRegistrationMutation.Data> {
        val startDirectDebitRegistrationMutation = StartDirectDebitRegistrationMutation
            .builder()
            .build()

        return Rx2Apollo
            .from(apolloClientWrapper.apolloClient.mutate(startDirectDebitRegistrationMutation))
            .map { it.data() }
    }

    fun refreshBankAccountInfo(): Observable<Response<BankAccountQuery.Data>> {
        val bankAccountQuery = BankAccountQuery
            .builder()
            .build()

        return Rx2Apollo
            .from(
                apolloClientWrapper.apolloClient
                    .query(bankAccountQuery)
                    .responseFetcher(ApolloResponseFetchers.NETWORK_ONLY)
            )
    }

    fun writeBankAccountInfoToCache(bankAccount: BankAccountQuery.BankAccount) {
        val cachedData = apolloClientWrapper.apolloClient
            .apolloStore()
            .read(profileQuery)
            .execute()

        val newBankAccount = ProfileQuery.BankAccount
            .builder()
            .__typename(bankAccount.__typename)
            .bankName(bankAccount.bankName)
            .descriptor(bankAccount.descriptor)
            .build()

        val newData = cachedData
            .toBuilder()
            .bankAccount(newBankAccount)
            .build()

        apolloClientWrapper.apolloClient
            .apolloStore()
            .writeAndPublish(profileQuery, newData)
            .execute()
    }

    fun logout() = Rx2Apollo.from(apolloClientWrapper.apolloClient.mutate(LogoutMutation()))
}
