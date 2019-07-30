package com.hedvig.app.viewmodel

import androidx.lifecycle.ViewModel
import com.hedvig.android.owldroid.type.CampaignInput
import com.hedvig.app.data.analytics.AnalyticsRepository
import com.hedvig.app.util.anyNotNull
import org.json.JSONException
import org.json.JSONObject


class AnalyticsViewModel(private val analyticsRepository: AnalyticsRepository) : ViewModel() {

    fun handleBranchReferringParams(referringParams: JSONObject) {
        val utmSource = referringParams.getStringOrNull("~channel")
        val utmMedium = referringParams.getStringOrNull("~feature")
        val utmContent = referringParams.getStringOrNull("~tags")
        val utmCampaign = referringParams.getStringOrNull("~campaign")
        val utmTerm = referringParams.getStringOrNull("~keywords")

        anyNotNull(utmSource, utmMedium, utmContent, utmCampaign, utmTerm) {
            val campaignInput = CampaignInput.builder()
                .source(utmSource)
                .medium(utmMedium)
                .content(utmContent)
                .name(utmCampaign)
                .term(utmTerm)
                .build()

            analyticsRepository.registerBranchCampaign(campaignInput)
        }
    }

    private fun JSONObject.getStringOrNull(key: String) = try {
        this.getString(key)
    } catch (e: JSONException) {
        null
    }
}
