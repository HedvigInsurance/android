package com.hedvig.app.feature.offer.usecase.insurelydatacollection

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.coroutines.toFlow
import com.hedvig.android.owldroid.graphql.DataCollectionStatusSubscription
import com.hedvig.app.util.LCE
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.toQueryResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class SubscribeToInsurelyDataCollectionUseCase(
    private val apolloClient: ApolloClient,
) {

    operator fun invoke(referenceUUID: String): Flow<LCE<DataCollectionResult>> {
        return apolloClient
            .subscribe(DataCollectionStatusSubscription(referenceUUID))
            .toFlow()
            .map(Response<DataCollectionStatusSubscription.Data>::toQueryResult)
            .map { queryResult ->
                when (queryResult) {
                    is QueryResult.Error -> LCE.Error
                    is QueryResult.Success -> LCE.Content(
                        DataCollectionResult.fromDto(queryResult.data.dataCollectionStatusV2)
                    )
                }
            }
            .onStart { emit(LCE.Loading) }
    }
}
