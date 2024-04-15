package com.hedvig.android.feature.payments.data

import arrow.core.Either
import arrow.core.raise.either
import arrow.fx.coroutines.parZip
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.paying.member.GetOnlyHasNonPayingContractsUseCase
import com.hedvig.android.feature.payments.overview.data.ForeverInformation
import com.hedvig.android.feature.payments.overview.data.GetForeverInformationUseCase

internal interface GetDiscountsOverviewUseCase {
  suspend fun invoke(): Either<ErrorMessage, DiscountsOverview>
}

internal class GetDiscountsOverviewUseCaseImpl(
  private val getDiscountsUseCase: GetDiscountsUseCase,
  private val getForeverInformationUseCase: GetForeverInformationUseCase,
  private val getOnlyHasNonPayingContractsUseCase: GetOnlyHasNonPayingContractsUseCase,
) : GetDiscountsOverviewUseCase {
  override suspend fun invoke(): Either<ErrorMessage, DiscountsOverview> {
    return either {
      parZip(
        { getForeverInformationUseCase.invoke().bind() },
        { getDiscountsUseCase.invoke().bind() },
        { getOnlyHasNonPayingContractsUseCase.invoke().bind() },
      ) { foreverInformation, discounts, onlyHasNonPayingContracts ->
        DiscountsOverview(
          discounts = discounts,
          foreverInformation = if (onlyHasNonPayingContracts) {
            null
          } else {
            foreverInformation
          },
        )
      }
    }
  }
}

internal data class DiscountsOverview(
  val discounts: List<Discount>,
  val foreverInformation: ForeverInformation?,
)
