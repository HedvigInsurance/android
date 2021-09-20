package com.hedvig.app.feature.crossselling.ui

import android.os.Parcelable
import com.hedvig.app.feature.offer.OfferViewModel
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

sealed class CrossSellingResult : Parcelable {
    @Parcelize
    data class Success(
        val startingDate: LocalDate,
        val insuranceType: String,
    ) : CrossSellingResult() {
        companion object {
            @Suppress("RemoveRedundantQualifierName")
            fun from(event: OfferViewModel.Event.ApproveSuccessful): CrossSellingResult.Success =
                CrossSellingResult.Success(
                    startingDate = event.startDate ?: LocalDate.now(),
                    insuranceType = event.bundleDisplayName,
                )
        }
    }

    @Parcelize
    object Error : CrossSellingResult()
}
