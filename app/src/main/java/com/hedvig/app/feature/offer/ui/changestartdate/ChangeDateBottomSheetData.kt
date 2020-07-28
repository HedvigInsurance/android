package com.hedvig.app.feature.offer.ui.changestartdate

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ChangeDateBottomSheetData(
    val id: String,
    val hasSwitchableInsurer: Boolean
) : Parcelable
