package com.hedvig.app.util.compose.preview

import com.hedvig.app.feature.claimstatus.model.ClaimStatusDetailData
import com.hedvig.app.feature.home.ui.claimstatus.data.ClaimProgressData
import com.hedvig.app.util.apollo.ThemedIconUrls

fun ThemedIconUrls.Companion.previewData(): ThemedIconUrls = ThemedIconUrls(
    darkUrl = "/app-content-service/all_risk_dark.svg",
    lightUrl = "/app-content-service/all_risk.svg"
)

fun ClaimStatusDetailData.TopInfoData.ClaimType.Companion.previewData(): ClaimStatusDetailData.TopInfoData.ClaimType =
    ClaimStatusDetailData.TopInfoData.ClaimType.Known(
        title = "All-risk",
        insuranceType = "Contents Insurance",
    )

fun ClaimStatusDetailData.CardData.Companion.previewData(): ClaimStatusDetailData.CardData =
    ClaimStatusDetailData.CardData(
        progress = Unit,
        informationText = """
            We got you covered. The payment should be in your bank account by now.
            
            Scroll down to see how we calculated.
        """.trimIndent()
    )

fun ClaimProgressData.Companion.previewData(): List<ClaimProgressData> = listOf(
    ClaimProgressData("Submitted", ClaimProgressData.ClaimProgressType.PastInactive),
    ClaimProgressData("Being handled", ClaimProgressData.ClaimProgressType.CurrentlyActive),
    ClaimProgressData("Closed", ClaimProgressData.ClaimProgressType.FutureInactive),
)
