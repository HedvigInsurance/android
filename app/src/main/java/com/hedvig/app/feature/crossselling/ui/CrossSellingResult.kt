package com.hedvig.app.feature.crossselling.ui

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

sealed class CrossSellingResult : Parcelable {
    @Parcelize
    data class Success(
        val startingDate: LocalDate,
        val insuranceType: String,
    ) : CrossSellingResult()

    @Parcelize
    object Error : CrossSellingResult()
}
