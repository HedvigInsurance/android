package com.hedvig.app.feature.claimdetail

import android.content.Context
import android.os.Parcelable
import android.text.format.DateUtils
import com.hedvig.app.feature.home.ui.claimstatus.data.ClaimProgressData
import kotlinx.parcelize.Parcelize
import java.time.Instant

@Parcelize
data class ClaimDetailParameter(
    val claimType: String,
    val submittedAt: Instant,
    val closedAt: Instant?,
    val progressSegments: List<ClaimProgressData>,
    val statusParagraph: String,
) : Parcelable {
    fun toClaimDetailData(context: Context) = ClaimDetailData(
        claimType = claimType,
        // TODO: Determine when to use relative time, and when to present the full Date
        submittedText = DateUtils.getRelativeTimeSpanString(context, submittedAt.toEpochMilli()).toString(),
        closedText = closedAt?.let {
            DateUtils.getRelativeTimeSpanString(context, it.toEpochMilli()).toString()
        } ?: "—",
        progress = progressSegments,
        progressText = statusParagraph,
    )

    companion object
}
