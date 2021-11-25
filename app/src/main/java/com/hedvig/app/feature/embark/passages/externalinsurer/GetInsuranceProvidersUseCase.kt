package com.hedvig.app.feature.embark.passages.externalinsurer

interface GetInsuranceProvidersUseCase {
    suspend fun getInsuranceProviders(): InsuranceProvidersResult
}

sealed class InsuranceProvidersResult {
    data class Success(val providers: List<InsuranceProvider>) : InsuranceProvidersResult()
    sealed class Error : InsuranceProvidersResult() {
        object NetworkError : Error()
        object QueryError : Error()
    }
}

data class InsuranceProvider(
    val id: String,
    val name: String
)

class GetInsuranceProvidersUseCaseImpl : GetInsuranceProvidersUseCase {
    override suspend fun getInsuranceProviders(): InsuranceProvidersResult {
        TODO("Not yet implemented")
    }
}
