package com.hedvig.app.util.compose.preview

import com.hedvig.app.feature.claimdetail.ClaimDetailParameter
import com.hedvig.app.feature.home.ui.claimstatus.data.ClaimProgressData
import com.hedvig.app.feature.home.ui.claimstatus.data.PillData
import java.time.Duration
import java.time.Instant

fun PillData.Companion.previewData(): List<PillData> {
    return PillData.PillType.values().dropLast(1).map { pillType ->
        PillData(pillType.name, pillType)
    }
}

fun ClaimProgressData.Companion.previewData(): List<ClaimProgressData> {
    return ClaimProgressData.ClaimProgressType.values().dropLast(1).map { progressType ->
        ClaimProgressData(progressType.name, progressType)
    }
}

fun ClaimDetailParameter.Companion.previewData() = ClaimDetailParameter(
    claimType = "Insurance case",
    submittedAt = Instant.now().minus(Duration.ofMinutes(1)),
    closedAt = null,
    progressSegments = ClaimProgressData.previewData(),
    statusParagraph = "We have received your claim and will start reviewing it soon.",
)
