package com.hedvig.android.feature.change.tier.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature.TIER
import com.hedvig.android.logger.LogPriority.ERROR
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.flow.first
import octopus.CompareCoverageQuery

internal interface GetCoverageComparisonUseCase {
  suspend fun invoke(termsVersionIds: List<String>): Either<ErrorMessage, ComparisonData>
}

internal class GetCoverageComparisonUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val featureManager: FeatureManager,
) : GetCoverageComparisonUseCase {
  override suspend fun invoke(termsVersionIds: List<String>): Either<ErrorMessage, ComparisonData> {
//    val mockIds = listOf("SE_DOG_BASIC-20230330-HEDVIG-null",
//      "SE_DOG_STANDARD-20230330-HEDVIG-null",)

    return either {
      val isTierEnabled = featureManager.isFeatureEnabled(TIER).first()
      if (!isTierEnabled) {
        logcat(ERROR) { "Tried to ask for coverage comparison when feature flag is disabled" }
        raise(ErrorMessage("Tried to ask for coverage comparison when feature flag is disabled"))
      }
      logcat { "Mariia: sending termsVersions: $termsVersionIds" }
      val result = apolloClient.query(CompareCoverageQuery(termsVersionIds))
        .safeExecute()
        .mapLeft {
          ErrorMessage("Tried to to ask for coverage comparison but got error from CompareCoverageQuery: $it")
        }
        .onLeft {
          logcat(ERROR) { "Tried to to ask for coverage comparison but got error from CompareCoverageQuery: $it" }
        }
        .bind()
      logcat { "Mariia: coverage comparison result: $result " }
      ComparisonData(
        columns = result.productVariantComparison.variantColumns.map { it.displayNameTier },
        rows = result.productVariantComparison.rows.map { row ->
          ComparisonRow(
            title = row.title,
            description = row.description,
            cells = row.cells.map { cell ->
              ComparisonCell(
                coverageText = cell.coverageText,
                isCovered = cell.isCovered,
              )
            },
          )
        },
      )
    }
  }
}

internal data class ComparisonData(
  val columns: List<String?>,
  val rows: List<ComparisonRow>,
)

internal data class ComparisonRow(
  val title: String,
  val description: String,
  val cells: List<ComparisonCell>,
)

internal data class ComparisonCell(
  val coverageText: String?,
  val isCovered: Boolean,
)

internal val mockComparisonData = ComparisonData(
  columns = listOf("Student", "Basic", "Standard", "Premium"),
  rows = listOf(
    ComparisonRow(
      title = "Veterinary care",
      description = "We ensure that you receive compensation for the examination, care and treatment your pet needs if it gets ill or injured in the event accident.",
      cells = listOf(
        ComparisonCell("30 000 kr", true),
        ComparisonCell("30 000 kr", true),
        ComparisonCell("60 000 kr", true),
        ComparisonCell("120 000 kr", true),
      ),
    ),
    ComparisonRow(
      title = "Advanced diagnostics",
      description = "If your pet needs diagnostic examination prescribed by a veterinarian for further care, we compensate costs that have been approved in advance by Hedvig.",
      cells = listOf(
        ComparisonCell(null, false),
        ComparisonCell(null, false),
        ComparisonCell(null, true),
        ComparisonCell(null, true),
      ),
    ),
    ComparisonRow(
      title = "Care of pet at home",
      description = "Compensation for loss of income if you need to stay home from work to take care of your sick or injured pet.",
      cells = listOf(
        ComparisonCell(null, false),
        ComparisonCell(null, false),
        ComparisonCell("500 kr", true),
        ComparisonCell("2 000 kr", true),
      ),
    ),
    ComparisonRow(
      title = "Life insurance",
      description = "If your pet were to die as a result of illness or injury. Or if your pet must be euthanized according to a veterinarian. You also get compensation if your pet is stolen or lost.",
      cells = listOf(
        ComparisonCell(null, false),
        ComparisonCell(null, false),
        ComparisonCell(null, false),
        ComparisonCell("30 000 kr", true),
      ),
    ),
    ComparisonRow(
      title = "Veterinary care",
      description = "We ensure that you receive compensation for the examination, care and treatment your pet needs if it gets ill or injured in the event accident.",
      cells = listOf(
        ComparisonCell("30 000 kr", true),
        ComparisonCell("30 000 kr", true),
        ComparisonCell("60 000 kr", true),
        ComparisonCell("120 000 kr", true),
      ),
    ),
    ComparisonRow(
      title = "Advanced diagnostics",
      description = "If your pet needs diagnostic examination prescribed by a veterinarian for further care, we compensate costs that have been approved in advance by Hedvig.",
      cells = listOf(
        ComparisonCell(null, false),
        ComparisonCell(null, false),
        ComparisonCell(null, true),
        ComparisonCell(null, true),
      ),
    ),
    ComparisonRow(
      title = "Care of pet at home",
      description = "Compensation for loss of income if you need to stay home from work to take care of your sick or injured pet.",
      cells = listOf(
        ComparisonCell(null, false),
        ComparisonCell(null, false),
        ComparisonCell("500 kr", true),
        ComparisonCell("2 000 kr", true),
      ),
    ),
    ComparisonRow(
      title = "Life insurance",
      description = "If your pet were to die as a result of illness or injury. Or if your pet must be euthanized according to a veterinarian. You also get compensation if your pet is stolen or lost.",
      cells = listOf(
        ComparisonCell(null, false),
        ComparisonCell(null, false),
        ComparisonCell(null, false),
        ComparisonCell("30 000 kr", true),
      ),
    ),
  ),
)
