package com.hedvig.app.feature.insurablelimits

import android.os.Parcelable
import com.hedvig.android.owldroid.fragment.InsurableLimitsFragment
import kotlinx.parcelize.Parcelize

sealed class InsurableLimitItem {
    sealed class Header : InsurableLimitItem() {
        object Details : Header()
        object MoreInfo : Header()
    }

    @Parcelize
    data class InsurableLimit(
        val label: String,
        val limit: String,
        val description: String,
    ) : InsurableLimitItem(), Parcelable {
        companion object {
            fun from(fragment: InsurableLimitsFragment) = InsurableLimit(
                label = fragment.label,
                limit = fragment.limit,
                description = fragment.description,
            )
        }
    }
}
