package com.hedvig.app.feature.keygear.data

import com.apollographql.apollo.api.Response
import com.apollographql.apollo.coroutines.toChannel
import com.apollographql.apollo.coroutines.toDeferred
import com.apollographql.apollo.rx2.Rx2Apollo
import com.hedvig.android.owldroid.fragment.KeyGearItemFragment
import com.hedvig.android.owldroid.graphql.KeyGearItemQuery
import com.hedvig.android.owldroid.graphql.KeyGearItemsQuery
import com.hedvig.android.owldroid.graphql.UpdateKeyGearPriceAndDateMutation
import com.hedvig.android.owldroid.type.MonetaryAmountV2Input
import com.hedvig.app.ApolloClientWrapper
import kotlinx.coroutines.channels.Channel
import org.threeten.bp.YearMonth

class KeyGearItemsRepository(
    private val apolloClientWrapper: ApolloClientWrapper
) {
    private lateinit var keyGearItemQuery: KeyGearItemQuery

    fun keyGearItems() = Rx2Apollo.from(
        apolloClientWrapper
            .apolloClient
            .query(KeyGearItemsQuery())
            .watcher()
    )

    fun keyGearItem(id: String): Channel<Response<KeyGearItemQuery.Data>> {
        keyGearItemQuery = KeyGearItemQuery(id)
        return apolloClientWrapper.apolloClient.query(keyGearItemQuery).watcher().toChannel()
    }

    suspend fun updatePurchasePriceAndDateAsync(
        id: String,
        date: YearMonth,
        price: MonetaryAmountV2Input
    ) {
        val mutation = UpdateKeyGearPriceAndDateMutation(id, date, price)
        val response = apolloClientWrapper.apolloClient.mutate(mutation).toDeferred().await()
        val newPrice =
            response.data()?.updatePurchasePriceForKeyGearItem?.purchasePrice?.amount ?: return
        val newDate =
            response.data()?.updateTimeOfPurchaseForKeyGearItem?.timeOfPurchase ?: return

        val cachedData =
            apolloClientWrapper
                .apolloClient
                .apolloStore()
                .read(keyGearItemQuery)
                .execute()
        

        cachedData.keyGearItem?.let { keyGearItem ->
            val newData = cachedData
                .toBuilder()
                .keyGearItem(
                    keyGearItem.toBuilder().fragments(
                        KeyGearItemQuery.KeyGearItem.Fragments(
                            keyGearItem.fragments.keyGearItemFragment.toBuilder().purchasePrice(
                                KeyGearItemFragment.PurchasePrice("MonetaryAmountV2", newPrice)
                            ).timeOfPurchase(newDate).build()
                        )
                    ).build()
                ).build()

            apolloClientWrapper
                .apolloClient
                .apolloStore()
                .writeAndPublish(keyGearItemQuery, newData)
                .execute()
        }
    }
}
