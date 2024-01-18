package com.hedvig.android.feature.help.center.model

import androidx.annotation.StringRes
import hedvig.resources.R
import kotlinx.collections.immutable.persistentListOf

internal enum class Topic(
  @StringRes val titleRes: Int,
  val commonQuestionIds: List<Question>,
  val allQuestionIds: List<Question>,
) {
  PAYMENTS(
    titleRes = R.string.HC_PAYMENTS_TITLE,
    commonQuestionIds = listOf(),
    allQuestionIds = listOf(
      Question.PAYMENTS_Q1,
      Question.PAYMENTS_Q2,
      Question.PAYMENTS_Q3,
      Question.PAYMENTS_Q4,
      Question.PAYMENTS_Q5,
      Question.PAYMENTS_Q6,
      Question.PAYMENTS_Q7,
      Question.PAYMENTS_Q8,
      Question.PAYMENTS_Q9,
      Question.PAYMENTS_Q10,
      Question.PAYMENTS_Q11,
      Question.PAYMENTS_Q12,
      Question.PAYMENTS_Q13,
      Question.PAYMENTS_Q14,
    ),
  ),
  CLAIMS(
    titleRes = R.string.HC_CLAIMS_TITLE,
    commonQuestionIds = emptyList(),
    allQuestionIds = listOf(
      Question.CLAIMS_Q1,
      Question.CLAIMS_Q2,
      Question.CLAIMS_Q3,
      Question.CLAIMS_Q4,
      Question.CLAIMS_Q5,
      Question.CLAIMS_Q6,
      Question.CLAIMS_Q7,
      Question.CLAIMS_Q8,
      Question.CLAIMS_Q9,
      Question.CLAIMS_Q10,
      Question.CLAIMS_Q11,
      Question.CLAIMS_Q12,
    ),
  ),
  INSURANCE(
    titleRes = R.string.HC_INSURANCES_TITLE,
    commonQuestionIds = emptyList(),
    allQuestionIds = listOf(
      Question.INSURANCE_Q1,
      Question.INSURANCE_Q2,
      Question.INSURANCE_Q3,
      Question.INSURANCE_Q4,
      Question.INSURANCE_Q5,
      Question.INSURANCE_Q6,
      Question.INSURANCE_Q7,
      Question.INSURANCE_Q8,
      Question.INSURANCE_Q9,
      Question.INSURANCE_Q10,
    ),
  ),
  COVERAGE(
    titleRes = R.string.HC_COVERAGE_TITLE,
    commonQuestionIds = emptyList(),
    allQuestionIds = listOf(
      Question.COVERAGE_Q1,
      Question.COVERAGE_Q2,
      Question.COVERAGE_Q3,
      Question.COVERAGE_Q4,
      Question.COVERAGE_Q5,
      Question.COVERAGE_Q6,
      Question.COVERAGE_Q7,
      Question.COVERAGE_Q8,
      Question.COVERAGE_Q9,
      Question.COVERAGE_Q10,
      Question.COVERAGE_Q11,
      Question.COVERAGE_Q12,
      Question.COVERAGE_Q13,
      Question.COVERAGE_Q14,
      Question.COVERAGE_Q15,
      Question.COVERAGE_Q16,
      Question.COVERAGE_Q17,
      Question.COVERAGE_Q18,
      Question.COVERAGE_Q19,
      Question.COVERAGE_Q20,
      Question.COVERAGE_Q21,
      Question.COVERAGE_Q22,
    ),
  ),
  OTHER(
    titleRes = R.string.HC_ALL_QUESTION_TITLE,
    commonQuestionIds = emptyList(),
    allQuestionIds = listOf(
      Question.OTHER_Q1,
      Question.OTHER_Q2,
      Question.OTHER_Q3,
      Question.OTHER_Q4,
    ),
  ),
}

internal val commonTopics = persistentListOf(
  Topic.PAYMENTS,
  Topic.CLAIMS,
  Topic.INSURANCE,
  Topic.COVERAGE,
  Topic.OTHER,
)
