package com.hedvig.android.feature.change.tier.data

import arrow.core.Either
import com.hedvig.android.core.common.ErrorMessage

internal interface GetCoverageComparisonUseCase {
  suspend fun invoke(): Either<ErrorMessage, ComparisonData>
}


internal data class ComparisonData(
  val columns: List<String>,
  val rows: List<ComparisonRow>
)

internal data class ComparisonRow(
  val title: String,
  val description: String,
  val cells: List<ComparisonCell>
)

internal data class ComparisonCell(
  val coverageText: String,
  val isCovered: Boolean
)
