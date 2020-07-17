package com.hedvig.app.testdata.feature.referrals.builders

import com.hedvig.android.owldroid.graphql.UpdateReferralCampaignCodeMutation

data class EditCodeDataBuilder(
    private val code: String = "EDITEDCODE123",
    private val variant: ResultVariant = ResultVariant.SUCCESS,
    private val maxCharacters: Int = 24,
    private val minCharacters: Int = 1,
    private val maximumNumberOfUpdates: Int = 3
) {
    fun build() = UpdateReferralCampaignCodeMutation.Data(
        updateReferralCampaignCode = UpdateReferralCampaignCodeMutation.UpdateReferralCampaignCode(
            asSuccessfullyUpdatedCode = if (variant == ResultVariant.SUCCESS) {
                UpdateReferralCampaignCodeMutation.AsSuccessfullyUpdatedCode(
                    code = code
                )
            } else {
                null
            },
            asCodeAlreadyTaken = if (variant == ResultVariant.ALREADY_TAKEN) {
                UpdateReferralCampaignCodeMutation.AsCodeAlreadyTaken(
                    code = code
                )
            } else {
                null
            },
            asCodeTooLong = if (variant == ResultVariant.TOO_LONG) {
                UpdateReferralCampaignCodeMutation.AsCodeTooLong(maxCharacters = maxCharacters)
            } else {
                null
            },
            asCodeTooShort = if (variant == ResultVariant.TOO_SHORT) {
                UpdateReferralCampaignCodeMutation.AsCodeTooShort(minCharacters = minCharacters)
            } else {
                null
            },
            asExceededMaximumUpdates = if (variant == ResultVariant.EXCEEDED_MAX_UPDATES) {
                UpdateReferralCampaignCodeMutation.AsExceededMaximumUpdates(maximumNumberOfUpdates = maximumNumberOfUpdates)
            } else {
                null
            }
        )
    )

    enum class ResultVariant {
        SUCCESS,
        ALREADY_TAKEN,
        TOO_LONG,
        TOO_SHORT,
        EXCEEDED_MAX_UPDATES,
        UNKNOWN
    }
}
