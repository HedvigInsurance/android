package com.hedvig.app.feature.claimdetail

import com.hedvig.app.feature.home.ui.claimstatus.data.ClaimProgressData

data class ClaimDetailData(
    val claimType: String,
    val submittedText: String,
    val closedText: String,
    val progress: List<ClaimProgressData>,
    val progressText: String,
)
