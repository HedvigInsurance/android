package com.hedvig.app.feature.offer.binders

import android.content.Intent
import android.net.Uri
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.startActivity
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.app.R
import com.hedvig.app.feature.offer.OfferTracker
import com.hedvig.app.util.extensions.toContractType
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.interpolateTextKey
import kotlinx.android.synthetic.main.offer_terms_area.view.*

class TermsBinder(
    private val root: LinearLayout,
    private val tracker: OfferTracker
) {

    private var previousInsuranceData: OfferQuery.Insurance? = null
    private var previousQuoteData: OfferQuery.AsCompleteQuote? = null

    fun bind(insurance: OfferQuery.Insurance, quote: OfferQuery.AsCompleteQuote) = root.apply {

    }
}
