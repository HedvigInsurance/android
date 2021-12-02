package com.hedvig.app.feature.offer.usecase.insurelydatacollection

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.coroutines.toFlow
import com.hedvig.android.owldroid.graphql.DataCollectionStatusSubscription
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.toQueryResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SubscribeToDataCollectionUseCase(
    private val apolloClient: ApolloClient,
) {

    sealed class Status {
        abstract val id: String

        data class Error(override val id: String) : Status()
        data class Content(
            override val id: String,
            val dataCollectionResult: DataCollectionResult,
        ) : Status()
    }

    operator fun invoke(referenceUUID: String): Flow<Status> {
        return apolloClient
            .subscribe(DataCollectionStatusSubscription(referenceUUID))
            .toFlow()
            .map(Response<DataCollectionStatusSubscription.Data>::toQueryResult)
            .map { queryResult ->
                when (queryResult) {
                    is QueryResult.Error -> Status.Error(referenceUUID)
                    is QueryResult.Success -> Status.Content(
                        referenceUUID,
                        DataCollectionResult.fromDto(queryResult.data.dataCollectionStatusV2),
                    )
                }
            }
    }
}
