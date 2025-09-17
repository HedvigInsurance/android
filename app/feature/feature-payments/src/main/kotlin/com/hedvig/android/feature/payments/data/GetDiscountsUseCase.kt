package com.hedvig.android.feature.payments.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.feature.payments.data.Discount.DiscountStatus
import kotlin.String
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import octopus.DiscountsQuery
import octopus.fragment.DiscountsDetailsFragment
import octopus.type.ContractDiscountStatus
import octopus.type.RedeemedCampaignType

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
      .bind()
    val activeContracts = result.currentMember.activeContracts
    val pendingContracts = result.currentMember.pendingContracts

    val activeDiscountedContracts = buildList {
      activeContracts.forEach { activeContract ->
        if (activeContract.discountsDetails.discounts.isNotEmpty()) {
          add (DiscountedContract(
            contractId = activeContract.id,
            contractDisplayName = activeContract.exposureDisplayNameShort,
            discountsDetails = activeContract.discountsDetails.toDiscountsDetails()
          ))
        }

      }
    }
    val pendingDiscountedContracts = buildList {
      pendingContracts.forEach { pendingContract ->
        if (pendingContract.discountsDetails.discounts.isNotEmpty()) {
          add (DiscountedContract(
            contractId = pendingContract.id,
            contractDisplayName = pendingContract.exposureDisplayNameShort,
            discountsDetails = pendingContract.discountsDetails.toDiscountsDetails()
          ))
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
        isReferral = false
      )
    }
  )
}
