package com.hedvig.app.feature.keygear.ui.itemdetail

import com.apollographql.apollo.api.Input
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.rx2.Rx2Apollo
import com.hedvig.android.owldroid.graphql.KeyGearItemQuery
import com.hedvig.android.owldroid.graphql.UpdateTimeOfPurchaseForKeyGearItemMutation
import com.hedvig.app.ApolloClientWrapper
import io.reactivex.Observable

class KeyGearValuationRepository(
    private val apolloClientWrapper: ApolloClientWrapper
) {

    private lateinit var keyGearItemQuery: KeyGearItemQuery

    fun loadKeyGearItem(): Observable<Response<KeyGearItemQuery.Data>> {
        //TODO
        keyGearItemQuery = KeyGearItemQuery("?")

        return Rx2Apollo
            .from(apolloClientWrapper.apolloClient.query(keyGearItemQuery).watcher())
    }

    fun updateTimeOfPurchase(id: String, date: Input<Any>) = Rx2Apollo
        .from(
            apolloClientWrapper.apolloClient.mutate(
                UpdateTimeOfPurchaseForKeyGearItemMutation(
                    id,
                    date
                )
            )
        )

    fun writeTimeOfPurchaseToCache(data: UpdateTimeOfPurchaseForKeyGearItemMutation.Data) {
        val cachedData = apolloClientWrapper
            .apolloClient
            .apolloStore()
            .read(keyGearItemQuery)
            .execute()

        val newDate = data.updateTimeOfPurchaseForKeyGearItem.timeOfPurchase

    }
}
