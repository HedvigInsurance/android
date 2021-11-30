package com.hedvig.app.feature.embark.passages.externalinsurer.askforprice

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class InsuranceProviderParameter(
    val selectedInsuranceProviderCollectionId: String
) : Parcelable
