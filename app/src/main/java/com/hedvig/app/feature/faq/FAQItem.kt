package com.hedvig.app.feature.faq

import android.os.Parcelable
import com.hedvig.android.owldroid.fragment.CrossSellFragment
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.util.safeLet
import kotlinx.parcelize.Parcelize

@Parcelize
data class FAQItem(
    val headline: String,
    val body: String,
) : Parcelable {

    companion object {
        fun from(data: CrossSellFragment.Faq) = FAQItem(
            headline = data.headline,
            body = data.body,
        )

        fun from(data: OfferQuery.FrequentlyAskedQuestion) = safeLet(data.headline, data.body) { headline, body ->
            FAQItem(
                headline = headline,
                body = body,
            )
        }
    }
}
