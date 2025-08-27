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
import com.hedvig.android.feature.payments.data.Discount.ExpiredState
import kotlin.String
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import octopus.DiscountsQuery
import octopus.type.RedeemedCampaignType

internal interface GetDiscountsUseCase {
  suspend fun invoke(): Either<ErrorMessage, Set<DiscountedContract>>
}

internal class GetDiscountsUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val clock: Clock,
) : GetDiscountsUseCase {
  override suspend fun invoke(): Either<ErrorMessage, Set<DiscountedContract>> = either {
    val result = apolloClient.query(DiscountsQuery())
      .fetchPolicy(FetchPolicy.NetworkFirst)
      .safeExecute(::ErrorMessage)
      .bind()
    // todo: temporary, waiting for the new API
    val discounts = result.currentMember
      .redeemedCampaigns
      .filter { it.type == RedeemedCampaignType.VOUCHER }
      .map {
        DiscountElement(
          code = it.code,
          description = it.description,
          expiredState = Discount.ExpiredState.from(it.expiresAt, clock), // todo: here add starts in the future, pending
          amount = null,
          isReferral = false,
          contractIdAndNames = it.onlyApplicableToContracts?.map { contract ->
            contract.id to contract.currentAgreement.productVariant.displayName
          } ?: listOf(),
        )
      }
    val discountedContractsIdsAndNames = result.currentMember
      .redeemedCampaigns
      .filter { it.type == RedeemedCampaignType.VOUCHER }
      .flatMap { discount ->
        discount.onlyApplicableToContracts?.map { contract ->
          contract.id to
            contract.currentAgreement.productVariant.displayName
        }
          ?: emptyList()
      }.toSet()
    val discountedContracts = buildSet {
      discountedContractsIdsAndNames.forEach { contract ->
        val appliedDiscounts = discounts.filter { it.contractIdAndNames.contains(contract) }
        add(
          DiscountedContract(
            contractId = contract.first,
            contractDisplayName = contract.second,
            appliedDiscounts = appliedDiscounts.map {
              Discount(
                code = it.code,
                description = it.description,
                expiredState = it.expiredState,
                amount = null,
                isReferral = it.isReferral,
              )
            },
          ),
        )
      }
    }
    discountedContracts
  }
}

private fun Discount.ExpiredState.Companion.from(expirationDate: LocalDate?, clock: Clock): Discount.ExpiredState {
  if (expirationDate == null) {
    return Discount.ExpiredState.NotExpired
  }
  val today = clock.todayIn(TimeZone.currentSystemDefault())
  return if (expirationDate < today) {
    Discount.ExpiredState.AlreadyExpired(expirationDate)
  } else {
    Discount.ExpiredState.ExpiringInTheFuture(expirationDate)
  }
}

private data class DiscountElement(
  val code: String,
  val description: String?,
  val expiredState: ExpiredState,
  val amount: UiMoney?,
  val isReferral: Boolean,
  val contractIdAndNames: List<Pair<String, String>>,
)
