package com.hedvig.android.core.common.android

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@JvmInline
@Parcelize
value class QuoteCartId(val id: String) : Parcelable
