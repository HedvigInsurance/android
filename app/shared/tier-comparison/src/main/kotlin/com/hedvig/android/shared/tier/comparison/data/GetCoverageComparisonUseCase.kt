package com.hedvig.android.shared.tier.comparison.data

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.LogPriority.ERROR
import com.hedvig.android.logger.logcat
import octopus.CompareCoverageQuery

interface GetCoverageComparisonUseCase {
  suspend fun invoke(termsVersionIds: List<String>): Either<ErrorMessage, ComparisonData>
}

internal class GetCoverageComparisonUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetCoverageComparisonUseCase {
  override suspend fun invoke(termsVersionIds: List<String>): Either<ErrorMessage, ComparisonData> {
    return either {
      val result = apolloClient.query(CompareCoverageQuery(termsVersionIds))
        .safeExecute()
        .mapLeft {
          ErrorMessage("Tried to to ask for coverage comparison but got error from CompareCoverageQuery: $it")
        }
        .onLeft {
          logcat(ERROR) { "Tried to to ask for coverage comparison but got error from CompareCoverageQuery: $it" }
        }
        .bind()
        .productVariantComparison
      logcat { "Coverage comparison result: $result" }
      ensure(result.rows.all { it.cells.size == result.variantColumns.size }) {
        ErrorMessage(
          "Coverage comparison result has inconsistent data. We have #${result.variantColumns.size} variants, yet " +
            "some of the row cells had a different amount of cells. $result",
        )
      }
      ComparisonData(
        coverageLevels = result.variantColumns.mapIndexed { variantColumnIndex, variantColumn ->
          ComparisonData.CoverageLevel(
            displayNameTier = if (variantColumn.displayNameTier != null) {
              variantColumn.displayNameTier
            } else {
              logcat(ERROR) { "Got variant with null title, falling back to displayNameSubtype. $variantColumn" }
              variantColumn.displayNameSubtype
            },
            termsVersion = variantColumn.termsVersion,
            coveredItems = result.rows.map { row ->
              val cellForCurrentVariant = row.cells[variantColumnIndex]
              ComparisonData.CoverageLevel.CoveredItem(
                title = row.title,
                description = row.description,
                coveredStatus = if (!cellForCurrentVariant.isCovered) {
                  ComparisonData.CoverageLevel.CoveredItem.CoveredStatus.NotCovered
                } else if (cellForCurrentVariant.coverageText != null) {
                  ComparisonData.CoverageLevel.CoveredItem.CoveredStatus.CoveredWithDescription(
                    cellForCurrentVariant.coverageText,
                  )
                } else {
                  ComparisonData.CoverageLevel.CoveredItem.CoveredStatus.Covered
                },
              )
            },
          )
        },
      )
    }
  }
}

data class ComparisonData(
  val coverageLevels: List<CoverageLevel>,
) {
  data class CoverageLevel(
    val displayNameTier: String,
    val termsVersion: String,
    val coveredItems: List<CoveredItem>,
  ) {
    data class CoveredItem(
      val title: String,
      val description: String,
      val coveredStatus: CoveredStatus,
    ) {
      sealed interface CoveredStatus {
        object Covered : CoveredStatus

        data class CoveredWithDescription(val coverageText: String) : CoveredStatus

        object NotCovered : CoveredStatus
      }
    }
  }
}
