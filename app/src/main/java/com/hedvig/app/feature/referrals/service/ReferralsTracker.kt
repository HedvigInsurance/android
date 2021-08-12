package com.hedvig.app.feature.referrals.service

import com.hedvig.app.feature.tracking.TrackingFacade

class ReferralsTracker(
    private val trackingFacade: TrackingFacade,
) {
    fun redeemReferralCode() = trackingFacade.track("REFERRAL_STARTSCREEN_BTN_CTA")
    fun skipReferralCode() = trackingFacade.track("REFERRAL_STARTSCREEN_BTN_SKIP")
    fun redeemReferralCodeOverlay() = trackingFacade.track("REFERRAL_ADDCOUPON_BTN_SUBMIT")
    fun termsAndConditions() = trackingFacade.track("referrals_info_sheet.full_terms_and_conditions")
    fun closeActivated() = trackingFacade.track("referrals_intro_screen.button")
    fun share() = trackingFacade.track("referrals_empty.share_code_button")
    fun reload() = trackingFacade.track("referrals_error_button")
    fun editCode() = trackingFacade.track("referrals_empty.edit.code.button")
    fun submitCode() = trackingFacade.track("nav_bar.save")
    fun editCodeConfirmDismissContinue() =
        trackingFacade.track("referrals_edit_code_confirm_dismiss_continue")

    fun editCodeConfirmDismissCancel() =
        trackingFacade.track("referrals_edit_code_confirm_dismiss_cancel")
}
