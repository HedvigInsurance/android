package com.hedvig.android.feature.payments.data

import arrow.core.Either
import arrow.core.raise.either
import arrow.fx.coroutines.parZip
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.feature.payments.overview.data.ForeverInformation
import com.hedvig.android.feature.payments.overview.data.GetForeverInformationUseCase
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

internal interface GetDiscountsOverviewUseCase {
  suspend fun invoke(): Either<ErrorMessage, DiscountsOverview>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class GetDiscountsOverviewUseCaseImpl(
  private val getDiscountsUseCase: GetDiscountsUseCase,
  private val getForeverInformationUseCase: GetForeverInformationUseCase,
) : GetDiscountsOverviewUseCase {
  override suspend fun invoke(): Either<ErrorMessage, DiscountsOverview> {
    return either {
      parZip(
        { getForeverInformationUseCase.invoke().bind() },
        { getDiscountsUseCase.invoke().bind() },
      ) { foreverInformation, discountedContracts ->
        DiscountsOverview(
          discountedContracts = discountedContracts,
          foreverInformation = foreverInformation,
        )
      }
    }
  }
}

internal data class DiscountsOverview(
  val discountedContracts: List<DiscountedContract>,
  val foreverInformation: ForeverInformation?,
)
