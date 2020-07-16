package com.hedvig.app.feature.referrals.service

import com.mixpanel.android.mpmetrics.MixpanelAPI

class ReferralsTracker(
    private val mixpanel: MixpanelAPI
) {
    fun redeemReferralCode() = mixpanel.track("REFERRAL_STARTSCREEN_BTN_CTA")
    fun skipReferralCode() = mixpanel.track("REFERRAL_STARTSCREEN_BTN_SKIP")
    fun redeemReferralCodeOverlay() = mixpanel.track("REFERRAL_ADDCOUPON_BTN_SUBMIT")
    fun termsAndConditions() = mixpanel.track("referrals_info_sheet.full_terms_and_conditions")
    fun closeActivated() = mixpanel.track("referrals_intro_screen.button")
    fun share() = mixpanel.track("referrals_empty.share_code_button")
    fun reload() = mixpanel.track("referrals_error_button")
    fun editCode() = mixpanel.track("referrals_empty.edit.code.button")
}
