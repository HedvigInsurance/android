package com.hedvig.app.feature.home.ui.changeaddress

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.SelfChangeEligibilityQuery
import com.hedvig.android.owldroid.type.SelfChangeBlocker
import com.hedvig.app.feature.home.ui.changeaddress.GetSelfChangeEligibilityUseCase.SelfChangeEligibilityResult.Blocked
import com.hedvig.app.feature.home.ui.changeaddress.GetSelfChangeEligibilityUseCase.SelfChangeEligibilityResult.Eligible
import com.hedvig.app.feature.home.ui.changeaddress.GetSelfChangeEligibilityUseCase.SelfChangeEligibilityResult.Error
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery

class GetSelfChangeEligibilityUseCase(
    private val apolloClient: ApolloClient,
) {

    suspend operator fun invoke(): SelfChangeEligibilityResult {
        return when (val result = apolloClient.query(SelfChangeEligibilityQuery()).safeQuery()) {
            is QueryResult.Success -> {
                val blockers = result.data?.selfChangeEligibility?.blockers
                if (!blockers.isNullOrEmpty()) {
                    Blocked(blockers)
                } else {
                    Eligible
                }
            }
            is QueryResult.Error -> Error(result.message)
        }
    }

    sealed class SelfChangeEligibilityResult {
        object Eligible: SelfChangeEligibilityResult()
        data class Blocked(val blockers: List<SelfChangeBlocker>?) : SelfChangeEligibilityResult()
        data class Error(val message: String?) : SelfChangeEligibilityResult()
    }
}
