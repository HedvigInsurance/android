package com.hedvig.app.feature.offer.binders

import android.widget.LinearLayout
import androidx.fragment.app.FragmentManager
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.app.BASE_MARGIN_HALF
import com.hedvig.app.R
import com.hedvig.app.feature.dashboard.ui.contractcoverage.PerilsAdapter
import com.hedvig.app.ui.decoration.GridSpacingItemDecoration
import com.hedvig.app.util.svg.buildRequestBuilder
import kotlinx.android.synthetic.main.offer_peril_area.view.*

class PerilBinder(
    private val root: LinearLayout,
    private val fragmentManager: FragmentManager
) {

    fun bind(
        list: List<OfferQuery.Peril>,
        data: OfferQuery.Data
    ) = root.apply {
        val typeOfContract = data.lastQuoteOfMember.asCompleteQuote?.typeOfContract
        val adapter = PerilsAdapter(fragmentManager, context.buildRequestBuilder())
        perilsRecycler.apply {
            this.adapter = adapter
            addItemDecoration(GridSpacingItemDecoration(BASE_MARGIN_HALF))
        }
        adapter.items = list.map { it.fragments.perilFragment }

        when (typeOfContract) {
            TypeOfContract.SE_HOUSE -> {
                root.perilInfo.text =
                    root.context.getString(R.string.OFFER_SCREEN_COVERAGE_BODY_HOUSE)
            }
            TypeOfContract.SE_APARTMENT_BRF,
            TypeOfContract.SE_APARTMENT_STUDENT_BRF,
            TypeOfContract.NO_HOME_CONTENT_OWN,
            TypeOfContract.NO_HOME_CONTENT_YOUTH_OWN
            -> {
                root.perilInfo.text =
                    root.context.getString(R.string.OFFER_SCREEN_COVERAGE_BODY_BRF)
            }
            TypeOfContract.NO_HOME_CONTENT_RENT,
            TypeOfContract.NO_HOME_CONTENT_YOUTH_RENT,
            TypeOfContract.SE_APARTMENT_RENT,
            TypeOfContract.SE_APARTMENT_STUDENT_RENT -> {
                root.perilInfo.text =
                    root.context.getString(R.string.OFFER_SCREEN_COVERAGE_BODY_RENTAL)
            }
            else -> root.perilInfo.text = ""
        }
    }
}
