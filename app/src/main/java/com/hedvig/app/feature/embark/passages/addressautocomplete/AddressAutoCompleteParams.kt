package com.hedvig.app.feature.embark.passages.addressautocomplete

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AddressAutoCompleteParams(
    val messages: List<String>,
    val key: String,
    val placeholder: String,
    val link: String
) : Parcelable
