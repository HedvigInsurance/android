package com.hedvig.app.util.compose.preview

import com.hedvig.app.feature.home.ui.claimstatus.data.ClaimProgressData
import com.hedvig.app.feature.home.ui.claimstatus.data.PillData

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
