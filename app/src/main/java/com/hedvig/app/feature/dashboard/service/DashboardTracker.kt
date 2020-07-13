package com.hedvig.app.feature.dashboard.service

import com.mixpanel.android.mpmetrics.MixpanelAPI

class DashboardTracker(
    private val mixpanel: MixpanelAPI
) {
    fun showRenewal() = mixpanel.track("DASHBOARD_RENEWAL_PROMPTER_CTA")
    fun connectPayment() = mixpanel.track("DASHBOARD_DIRECT_DEBIT_STATUS_NEED_SETUP_BUTTON_LABEL")
    fun insuranceCertificate() = mixpanel.track("MY_DOCUMENTS_INSURANCE_CERTIFICATE")
    fun termsAndConditions() = mixpanel.track("MY_DOCUMENTS_INSURANCE_TERMS")
    fun changeHomeInfo() = mixpanel.track("CONTRACT_DETAIL_HOME_CHANGE_INFO")
    fun changeCoinsuredInfo() = mixpanel.track("CONTRACT_DETAIL_COINSURED_CHANGE_INFO")
}
