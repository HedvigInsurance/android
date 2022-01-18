package com.hedvig.app.feature.claimdetail.data

import arrow.core.Either
import arrow.core.right
import com.hedvig.android.owldroid.graphql.ClaimDetailsQuery
import com.hedvig.app.feature.claimdetail.model.ClaimDetailUiState
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive

class GetClaimDetailUiStateFlowUseCase(
    private val getClaimDetailUseCase: GetClaimDetailUseCase,
) {
    /**
     * If the first invocation is a failure, simply returns the error and stops the flow.
     * If the first invocation is successful, starts polling the information, but does not stop or return the error.
     * This is so that the screen doesn't show the error state since we already have something to show which might be
     * stale, but is still more relevant than an error state.
     */
    operator fun invoke(claimId: String): Flow<Either<GetClaimDetailUseCase.Error, ClaimDetailUiState>> {
        return flow {
            val firstResult = getClaimDetailUseCase.invoke(claimId)
            emit(firstResult)
            if (firstResult.isLeft()) return@flow
            coroutineScope {
                while (isActive) {
                    getClaimDetailUseCase
                        .invoke(claimId)
                        .tap { claimDetail -> emit(claimDetail.right()) }
                    delay(5_000L)
                }
            }
        }.map { result: Either<GetClaimDetailUseCase.Error, ClaimDetailsQuery.ClaimDetail> ->
            result.map(ClaimDetailUiState::fromDto)
        }
    }
}
