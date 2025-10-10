package com.hedvig.android.feature.payments.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import octopus.DiscountsQuery
import octopus.fragment.DiscountsDetailsFragment
import octopus.type.ContractDiscountStatus

internal interface GetDiscountsUseCase {
  suspend fun invoke(): Either<ErrorMessage, List<DiscountedContract>>
}

internal class GetDiscountsUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetDiscountsUseCase {
  override suspend fun invoke(): Either<ErrorMessage, List<DiscountedContract>> = either {
    val result = apolloClient.query(DiscountsQuery())
      .fetchPolicy(FetchPolicy.NetworkFirst)
      .safeExecute(::ErrorMessage)
      .onLeft {
        logcat(LogPriority.ERROR) { "GetDiscountsUseCase returned error: $it" }
      }
      .bind()

    val activeContracts = result.currentMember.activeContracts
    val pendingContracts = result.currentMember.pendingContracts

    val activeDiscountedContracts = buildList {
      activeContracts.forEach { activeContract ->
        val displayName = activeContract.currentAgreement.productVariant.displayNameShort?.let {
          "$it • ${activeContract.exposureDisplayNameShort}"
        } ?: activeContract.exposureDisplayNameShort
        if (activeContract.discountsDetails.discounts.isNotEmpty()) {
          add(
            DiscountedContract(
              contractId = activeContract.id,
              contractDisplayName = displayName,
              discountsDetails = activeContract.discountsDetails.toDiscountsDetails(),
            ),
          )
        }
      }
    }
    val pendingDiscountedContracts = buildList {
      pendingContracts.forEach { pendingContract ->
        val displayName = pendingContract.productVariant.displayNameShort?.let {
          "$it • ${pendingContract.exposureDisplayNameShort}"
        } ?: pendingContract.exposureDisplayNameShort
        if (pendingContract.discountsDetails.discounts.isNotEmpty()) {
          add(
            DiscountedContract(
              contractId = pendingContract.id,
              contractDisplayName = displayName,
              discountsDetails = pendingContract.discountsDetails.toDiscountsDetails(),
            ),
          )
        }
      }
    }
    activeDiscountedContracts + pendingDiscountedContracts
  }
}

private fun DiscountsDetailsFragment.toDiscountsDetails(): DiscountsDetails {
  return DiscountsDetails(
    discountInfo = discountsInfo,
    appliedDiscounts = discounts.map { discount ->
      Discount(
        code = discount.campaignCode,
        description = discount.description,
        status = when (discount.discountStatus) {
          ContractDiscountStatus.ACTIVE -> Discount.DiscountStatus.ACTIVE
          ContractDiscountStatus.PENDING -> Discount.DiscountStatus.PENDING
          ContractDiscountStatus.TERMINATED -> Discount.DiscountStatus.EXPIRED
          ContractDiscountStatus.UNKNOWN__ -> Discount.DiscountStatus.PENDING
        },
        statusDescription = discount.statusDescription,
        amount = null,
        isReferral = false,
      )
    },
  )
}
