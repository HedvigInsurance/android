package com.hedvig.app.testdata.feature.referrals.builders

import com.hedvig.android.owldroid.graphql.UpdateReferralCampaignCodeMutation
import com.hedvig.android.owldroid.graphql.type.CodeAlreadyTaken
import com.hedvig.android.owldroid.graphql.type.CodeTooLong
import com.hedvig.android.owldroid.graphql.type.CodeTooShort
import com.hedvig.android.owldroid.graphql.type.ExceededMaximumUpdates
import com.hedvig.android.owldroid.graphql.type.SuccessfullyUpdatedCode

data class EditCodeDataBuilder(
  private val code: String = "EDITEDCODE123",
  private val variant: ResultVariant = ResultVariant.SUCCESS,
  private val maxCharacters: Int = 24,
  private val minCharacters: Int = 1,
  private val maximumNumberOfUpdates: Int = 3,
) {
  fun build() = UpdateReferralCampaignCodeMutation.Data(
    updateReferralCampaignCode = UpdateReferralCampaignCodeMutation.UpdateReferralCampaignCode(
      __typename = variant.typename,
      asSuccessfullyUpdatedCode = if (variant == ResultVariant.SUCCESS) {
        UpdateReferralCampaignCodeMutation.AsSuccessfullyUpdatedCode(
          __typename = variant.typename,
          code = code,
        )
      } else {
        null
      },
      asCodeAlreadyTaken = if (variant == ResultVariant.ALREADY_TAKEN) {
        UpdateReferralCampaignCodeMutation.AsCodeAlreadyTaken(
          __typename = variant.typename,
          code = code,
        )
      } else {
        null
      },
      asCodeTooLong = if (variant == ResultVariant.TOO_LONG) {
        UpdateReferralCampaignCodeMutation.AsCodeTooLong(
          __typename = variant.typename,
          maxCharacters = maxCharacters,
        )
      } else {
        null
      },
      asCodeTooShort = if (variant == ResultVariant.TOO_SHORT) {
        UpdateReferralCampaignCodeMutation.AsCodeTooShort(
          __typename = variant.typename,
          minCharacters = minCharacters,
        )
      } else {
        null
      },
      asExceededMaximumUpdates = if (variant == ResultVariant.EXCEEDED_MAX_UPDATES) {
        UpdateReferralCampaignCodeMutation.AsExceededMaximumUpdates(
          __typename = variant.typename,
          maximumNumberOfUpdates = maximumNumberOfUpdates,
        )
      } else {
        null
      },
    ),
  )

  enum class ResultVariant {
    SUCCESS,
    ALREADY_TAKEN,
    TOO_LONG,
    TOO_SHORT,
    EXCEEDED_MAX_UPDATES,
    UNKNOWN,
    ;

    val typename: String
      get() = when (this) {
        SUCCESS -> SuccessfullyUpdatedCode.type.name
        ALREADY_TAKEN -> CodeAlreadyTaken.type.name
        TOO_LONG -> CodeTooLong.type.name
        TOO_SHORT -> CodeTooShort.type.name
        EXCEEDED_MAX_UPDATES -> ExceededMaximumUpdates.type.name
        UNKNOWN -> ""
      }
  }
}
