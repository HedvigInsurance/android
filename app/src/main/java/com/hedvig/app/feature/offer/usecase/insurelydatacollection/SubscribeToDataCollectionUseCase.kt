package com.hedvig.app.feature.offer.usecase.insurelydatacollection

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.DataCollectionStatusSubscription
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeSubscription
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SubscribeToDataCollectionUseCase(
    private val apolloClient: ApolloClient,
) {

    sealed class Status {
        abstract val referenceUuid: String

        data class Error(override val referenceUuid: String) : Status()
        data class Content(
            override val referenceUuid: String,
            val dataCollectionResult: DataCollectionResult,
        ) : Status()
    }

    operator fun invoke(referenceUuid: String): Flow<Status> {
        return apolloClient
            .subscribe(DataCollectionStatusSubscription(referenceUuid))
            .safeSubscription()
            .map { queryResult ->
                when (queryResult) {
                    is QueryResult.Error -> Status.Error(referenceUuid)
                    is QueryResult.Success -> Status.Content(
                        referenceUuid,
                        DataCollectionResult.fromDto(queryResult.data.dataCollectionStatusV2),
                    )
                }
            }
    }
}
