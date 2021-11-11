package com.hedvig.app.util.compose.preview

import com.hedvig.app.feature.claimstatus.model.ClaimStatusDetailData
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
