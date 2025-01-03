package com.hedvig.android.feature.help.center.model

import androidx.annotation.StringRes
import hedvig.resources.R
import kotlinx.serialization.Serializable

@Serializable
internal enum class Question(
  @StringRes val titleRes: Int,
  @StringRes val questionRes: Int,
  @StringRes val answerRes: Int,
  val relatedQuestionIds: List<Question> = listOf(),
) {
  // CLAIMS
  CLAIMS_Q1(
    R.string.HC_CLAIMS_TITLE,
    R.string.HC_CLAIMS_Q_01,
    R.string.HC_CLAIMS_A_01,
  ),
  CLAIMS_Q2(
    R.string.HC_CLAIMS_TITLE,
    R.string.HC_CLAIMS_Q_02,
    R.string.HC_CLAIMS_A_02,
  ),
  CLAIMS_Q3(
    R.string.HC_CLAIMS_TITLE,
    R.string.HC_CLAIMS_Q_03,
    R.string.HC_CLAIMS_A_03,
  ),
  CLAIMS_Q4(
    R.string.HC_CLAIMS_TITLE,
    R.string.HC_CLAIMS_Q_04,
    R.string.HC_CLAIMS_A_04,
  ),
  CLAIMS_Q5(
    R.string.HC_CLAIMS_TITLE,
    R.string.HC_CLAIMS_Q_05,
    R.string.HC_CLAIMS_A_05,
  ),
  CLAIMS_Q6(
    R.string.HC_CLAIMS_TITLE,
    R.string.HC_CLAIMS_Q_06,
    R.string.HC_CLAIMS_A_06,
  ),
  CLAIMS_Q7(
    R.string.HC_CLAIMS_TITLE,
    R.string.HC_CLAIMS_Q_07,
    R.string.HC_CLAIMS_A_07,
  ),
  CLAIMS_Q8(
    R.string.HC_CLAIMS_TITLE,
    R.string.HC_CLAIMS_Q_08,
    R.string.HC_CLAIMS_A_08,
  ),
  CLAIMS_Q9(
    R.string.HC_CLAIMS_TITLE,
    R.string.HC_CLAIMS_Q_09,
    R.string.HC_CLAIMS_A_09,
  ),
  CLAIMS_Q10(
    R.string.HC_CLAIMS_TITLE,
    R.string.HC_CLAIMS_Q_10,
    R.string.HC_CLAIMS_A_10,
  ),
  CLAIMS_Q11(
    R.string.HC_CLAIMS_TITLE,
    R.string.HC_CLAIMS_Q_11,
    R.string.HC_CLAIMS_A_11,
  ),
  CLAIMS_Q12(
    R.string.HC_CLAIMS_TITLE,
    R.string.HC_CLAIMS_Q_12,
    R.string.HC_CLAIMS_A_12,
  ),

  // COVERAGE
  COVERAGE_Q1(
    R.string.HC_COVERAGE_TITLE,
    R.string.HC_COVERAGE_Q_01,
    R.string.HC_COVERAGE_A_01,
  ),
  COVERAGE_Q2(
    R.string.HC_COVERAGE_TITLE,
    R.string.HC_COVERAGE_Q_02,
    R.string.HC_COVERAGE_A_02,
  ),
  COVERAGE_Q3(
    R.string.HC_COVERAGE_TITLE,
    R.string.HC_COVERAGE_Q_03,
    R.string.HC_COVERAGE_A_03,
  ),
  COVERAGE_Q4(
    R.string.HC_COVERAGE_TITLE,
    R.string.HC_COVERAGE_Q_04,
    R.string.HC_COVERAGE_A_04,
  ),
  COVERAGE_Q5(
    R.string.HC_COVERAGE_TITLE,
    R.string.HC_COVERAGE_Q_05,
    R.string.HC_COVERAGE_A_05,
  ),
  COVERAGE_Q6(
    R.string.HC_COVERAGE_TITLE,
    R.string.HC_COVERAGE_Q_06,
    R.string.HC_COVERAGE_A_06,
  ),
  COVERAGE_Q7(
    R.string.HC_COVERAGE_TITLE,
    R.string.HC_COVERAGE_Q_07,
    R.string.HC_COVERAGE_A_07,
  ),
  COVERAGE_Q8(
    R.string.HC_COVERAGE_TITLE,
    R.string.HC_COVERAGE_Q_08,
    R.string.HC_COVERAGE_A_08,
  ),
  COVERAGE_Q9(
    R.string.HC_COVERAGE_TITLE,
    R.string.HC_COVERAGE_Q_09,
    R.string.HC_COVERAGE_A_09,
  ),
  COVERAGE_Q10(
    R.string.HC_COVERAGE_TITLE,
    R.string.HC_COVERAGE_Q_10,
    R.string.HC_COVERAGE_A_10,
  ),
  COVERAGE_Q11(
    R.string.HC_COVERAGE_TITLE,
    R.string.HC_COVERAGE_Q_11,
    R.string.HC_COVERAGE_A_11,
  ),
  COVERAGE_Q12(
    R.string.HC_COVERAGE_TITLE,
    R.string.HC_COVERAGE_Q_12,
    R.string.HC_COVERAGE_A_12,
  ),
  COVERAGE_Q13(
    R.string.HC_COVERAGE_TITLE,
    R.string.HC_COVERAGE_Q_13,
    R.string.HC_COVERAGE_A_13,
  ),
  COVERAGE_Q14(
    R.string.HC_COVERAGE_TITLE,
    R.string.HC_COVERAGE_Q_14,
    R.string.HC_COVERAGE_A_14,
  ),
  COVERAGE_Q15(
    R.string.HC_COVERAGE_TITLE,
    R.string.HC_COVERAGE_Q_15,
    R.string.HC_COVERAGE_A_15,
  ),
  COVERAGE_Q17(
    R.string.HC_COVERAGE_TITLE,
    R.string.HC_COVERAGE_Q_17,
    R.string.HC_COVERAGE_A_17,
  ),
  COVERAGE_Q18(
    R.string.HC_COVERAGE_TITLE,
    R.string.HC_COVERAGE_Q_18,
    R.string.HC_COVERAGE_A_18,
  ),
  COVERAGE_Q19(
    R.string.HC_COVERAGE_TITLE,
    R.string.HC_COVERAGE_Q_19,
    R.string.HC_COVERAGE_A_19,
  ),
  COVERAGE_Q20(
    R.string.HC_COVERAGE_TITLE,
    R.string.HC_COVERAGE_Q_20,
    R.string.HC_COVERAGE_A_20,
  ),
  COVERAGE_Q21(
    R.string.HC_COVERAGE_TITLE,
    R.string.HC_COVERAGE_Q_21,
    R.string.HC_COVERAGE_A_21,
  ),
  COVERAGE_Q22(
    R.string.HC_COVERAGE_TITLE,
    R.string.HC_COVERAGE_Q_22,
    R.string.HC_COVERAGE_A_22,
  ),
  COVERAGE_Q23(
    titleRes = R.string.HC_COVERAGE_TITLE,
    questionRes = R.string.HC_COVERAGE_Q_23,
    answerRes = R.string.HC_COVERAGE_A_23),
  COVERAGE_Q24(
    titleRes = R.string.HC_COVERAGE_TITLE,
    questionRes = R.string.HC_COVERAGE_Q_24,
    answerRes = R.string.HC_COVERAGE_A_24),
  COVERAGE_Q25(
    titleRes = R.string.HC_COVERAGE_TITLE,
    questionRes = R.string.HC_COVERAGE_Q_25,
    answerRes = R.string.HC_COVERAGE_A_25
  ),

  // INSURANCE
  INSURANCE_Q1(
    R.string.HC_INSURANCES_TITLE,
    R.string.HC_INSURANCE_Q_01,
    R.string.HC_INSURANCE_A_01,
  ),
  INSURANCE_Q2(
    R.string.HC_INSURANCES_TITLE,
    R.string.HC_INSURANCE_Q_02,
    R.string.HC_INSURANCE_A_02,
  ),
  INSURANCE_Q3(
    R.string.HC_INSURANCES_TITLE,
    R.string.HC_INSURANCE_Q_03,
    R.string.HC_INSURANCE_A_03,
  ),
  INSURANCE_Q4(
    R.string.HC_INSURANCES_TITLE,
    R.string.HC_INSURANCE_Q_04,
    R.string.HC_INSURANCE_A_04,
  ),
  INSURANCE_Q5(
    R.string.HC_INSURANCES_TITLE,
    R.string.HC_INSURANCE_Q_05,
    R.string.HC_INSURANCE_A_05,
  ),
  INSURANCE_Q6(
    R.string.HC_INSURANCES_TITLE,
    R.string.HC_INSURANCE_Q_06,
    R.string.HC_INSURANCE_A_06,
  ),
  INSURANCE_Q7(
    R.string.HC_INSURANCES_TITLE,
    R.string.HC_INSURANCE_Q_07,
    R.string.HC_INSURANCE_A_07,
  ),
  INSURANCE_Q8(
    R.string.HC_INSURANCES_TITLE,
    R.string.HC_INSURANCE_Q_08,
    R.string.HC_INSURANCE_A_08,
  ),
  INSURANCE_Q9(
    R.string.HC_INSURANCES_TITLE,
    R.string.HC_INSURANCE_Q_09,
    R.string.HC_INSURANCE_A_09,
  ),
  INSURANCE_Q10(
    R.string.HC_INSURANCES_TITLE,
    R.string.HC_INSURANCE_Q_10,
    R.string.HC_INSURANCE_A_10,
  ),

  // OTHER
  OTHER_Q1(
    R.string.HC_INSURANCES_TITLE,
    R.string.HC_OTHER_Q_01,
    R.string.HC_OTHER_A_01,
  ),
  OTHER_Q2(
    R.string.HC_INSURANCES_TITLE,
    R.string.HC_OTHER_Q_02,
    R.string.HC_OTHER_A_02,
  ),
  OTHER_Q3(
    R.string.HC_INSURANCES_TITLE,
    R.string.HC_OTHER_Q_03,
    R.string.HC_OTHER_A_03,
  ),
  OTHER_Q4(
    R.string.HC_INSURANCES_TITLE,
    R.string.HC_OTHER_Q_04,
    R.string.HC_OTHER_A_04,
  ),

  // PAYMENTS
  PAYMENTS_Q1(
    R.string.HC_PAYMENTS_TITLE,
    R.string.HC_PAYMENTS_Q_01,
    R.string.HC_PAYMENTS_A_01,
  ),
  PAYMENTS_Q2(
    R.string.HC_PAYMENTS_TITLE,
    R.string.HC_PAYMENTS_Q_02,
    R.string.HC_PAYMENTS_A_02,
  ),
  PAYMENTS_Q3(
    R.string.HC_PAYMENTS_TITLE,
    R.string.HC_PAYMENTS_Q_03,
    R.string.HC_PAYMENTS_A_03,
  ),
  PAYMENTS_Q4(
    R.string.HC_PAYMENTS_TITLE,
    R.string.HC_PAYMENTS_Q_04,
    R.string.HC_PAYMENTS_A_04,
  ),
  PAYMENTS_Q5(
    R.string.HC_PAYMENTS_TITLE,
    R.string.HC_PAYMENTS_Q_05,
    R.string.HC_PAYMENTS_A_05,
  ),
  PAYMENTS_Q6(
    R.string.HC_PAYMENTS_TITLE,
    R.string.HC_PAYMENTS_Q_06,
    R.string.HC_PAYMENTS_A_06,
  ),
  PAYMENTS_Q7(
    R.string.HC_PAYMENTS_TITLE,
    R.string.HC_PAYMENTS_Q_07,
    R.string.HC_PAYMENTS_A_07,
  ),
  PAYMENTS_Q8(
    R.string.HC_PAYMENTS_TITLE,
    R.string.HC_PAYMENTS_Q_08,
    R.string.HC_PAYMENTS_A_08,
  ),
  PAYMENTS_Q9(
    R.string.HC_PAYMENTS_TITLE,
    R.string.HC_PAYMENTS_Q_09,
    R.string.HC_PAYMENTS_A_09,
  ),
  PAYMENTS_Q10(
    R.string.HC_PAYMENTS_TITLE,
    R.string.HC_PAYMENTS_Q_10,
    R.string.HC_PAYMENTS_A_10,
  ),
  PAYMENTS_Q11(
    R.string.HC_PAYMENTS_TITLE,
    R.string.HC_PAYMENTS_Q_11,
    R.string.HC_PAYMENTS_A_11,
  ),
  PAYMENTS_Q12(
    R.string.HC_PAYMENTS_TITLE,
    R.string.HC_PAYMENTS_Q_12,
    R.string.HC_PAYMENTS_A_12,
  ),
  PAYMENTS_Q13(
    R.string.HC_PAYMENTS_TITLE,
    R.string.HC_PAYMENTS_Q_13,
    R.string.HC_PAYMENTS_A_13,
  ),
  PAYMENTS_Q14(
    R.string.HC_PAYMENTS_TITLE,
    R.string.HC_PAYMENTS_Q_14,
    R.string.HC_PAYMENTS_A_14,
  ),
}

internal val commonQuestions = listOf(
  Question.CLAIMS_Q1,
  Question.INSURANCE_Q5,
  Question.PAYMENTS_Q1,
  Question.INSURANCE_Q3,
  Question.INSURANCE_Q1,
)
