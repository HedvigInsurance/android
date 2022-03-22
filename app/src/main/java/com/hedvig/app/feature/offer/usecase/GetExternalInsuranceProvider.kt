package com.hedvig.app.feature.offer.usecase

import com.hedvig.app.feature.offer.usecase.datacollectionresult.DataCollectionResult
import com.hedvig.app.feature.offer.usecase.datacollectionresult.GetDataCollectionResultUseCase
import com.hedvig.app.feature.offer.usecase.datacollectionstatus.SubscribeToDataCollectionStatusUseCase
import com.hedvig.app.feature.offer.usecase.providerstatus.GetProviderDisplayNameUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class GetExternalInsuranceProviderUseCase(
    private val subscribeToDataCollectionStatusUseCase: SubscribeToDataCollectionStatusUseCase,
    private val getDataCollectionResultUseCase: GetDataCollectionResultUseCase,
    private val getProviderDisplayNameUseCase: GetProviderDisplayNameUseCase,
) {

    fun observeExternalProviderOrNull(id: String?): Flow<ExternalProvider?> {
        return if (id == null) {
            flow<ExternalProvider?> { emit(null) }
        } else {
            subscribeToDataCollectionStatusUseCase.invoke(id)
                .map { dataCollectionStatus ->
                    coroutineScope {
                        val dataCollectionResult = async {
                            getDataCollectionResultUseCase
                                .invoke(id)
                                .let { result ->
                                    (result as? GetDataCollectionResultUseCase.Result.Success)?.data
                                }
                        }
                        val insuranceProviderDisplayName = async {
                            if (dataCollectionStatus is SubscribeToDataCollectionStatusUseCase.Status.Content) {
                                val insuranceCompany =
                                    dataCollectionStatus.dataCollectionStatus.insuranceCompany
                                getProviderDisplayNameUseCase.invoke(insuranceCompany)
                            } else {
                                null
                            }
                        }
                        ExternalProvider(
                            dataCollectionStatus = dataCollectionStatus,
                            dataCollectionResult = dataCollectionResult.await(),
                            insuranceProviderDisplayName = insuranceProviderDisplayName.await()
                        )
                    }
                }
        }
    }
}

data class ExternalProvider(
    val dataCollectionStatus: SubscribeToDataCollectionStatusUseCase.Status?,
    val dataCollectionResult: DataCollectionResult?,
    val insuranceProviderDisplayName: String?,
)
