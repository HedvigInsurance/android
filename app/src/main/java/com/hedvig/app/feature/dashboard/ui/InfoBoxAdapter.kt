package com.hedvig.app.feature.dashboard.ui

import org.threeten.bp.LocalDate

sealed class InfoBoxModel {
    data class ImportantInformation(
        val title: String,
        val body: String,
        val actionLabel: String,
        val actionLink: String
    ) : InfoBoxModel()

    data class Renewal(
        val renewalDate: LocalDate,
        val draftCertificateUrl: String
    ) : InfoBoxModel()

    object ConnectPayin : InfoBoxModel()
}
