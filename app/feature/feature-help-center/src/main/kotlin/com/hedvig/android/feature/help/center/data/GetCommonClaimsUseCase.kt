package com.hedvig.android.feature.help.center.data

import arrow.core.Either
import arrow.core.flatten
import arrow.core.raise.either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.help.center.commonclaim.CommonClaim
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import octopus.CommonClaimsQuery
import octopus.CommonClaimsQuery.Data.CurrentMember.ActiveContract.CurrentAgreement.ProductVariant.CommonClaimDescription.Layout.Companion.asCommonClaimLayoutEmergency
import octopus.CommonClaimsQuery.Data.CurrentMember.ActiveContract.CurrentAgreement.ProductVariant.CommonClaimDescription.Layout.Companion.asCommonClaimLayoutTitleAndBulletPoints

internal class GetCommonClaimsUseCase(
  private val apolloClient: ApolloClient,
) {
  suspend fun invoke(): Either<ErrorMessage, PersistentList<CommonClaim>> = either {
    val commonClaims = apolloClient.query(CommonClaimsQuery())
      .safeExecute()
      .toEither(::ErrorMessage)
      .onLeft { logcat(LogPriority.ERROR) { it.message ?: "Could not fetch common claims" } }
      .bind()
      .currentMember
      .activeContracts
      .map { it.toCommonClaims() }

    commonClaims.flatten()
      .distinctBy {
        when (it) {
          is CommonClaim.Emergency -> it.title
          is CommonClaim.Generic -> it.id
        }
      }
      .toPersistentList()
  }

  private fun CommonClaimsQuery.Data.CurrentMember.ActiveContract.toCommonClaims() =
    currentAgreement.productVariant.commonClaimDescriptions.mapNotNull {
      if (it.layout.asCommonClaimLayoutTitleAndBulletPoints() != null) {
        it.layout.asCommonClaimLayoutTitleAndBulletPoints()?.let { layout ->
          CommonClaim.Generic.from(
            id = it.id,
            title = it.title,
            layout = layout,
          )
        }
      } else {
        it.layout.asCommonClaimLayoutEmergency()?.let { layout ->
          CommonClaim.Emergency.from(
            title = it.title,
            layout = layout,
          )
        }
      }
    }
}
