package com.hedvig.android.feature.odyssey.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
@JvmInline
value class ItemType(val name: String) : Parcelable

@Parcelize
@JvmInline
value class ItemProblem(val name: String) : Parcelable
