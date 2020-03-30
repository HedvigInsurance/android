package com.hedvig.app.feature.offer.binders

import android.widget.LinearLayout
import androidx.fragment.app.FragmentManager
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.app.BASE_MARGIN_HALF
import com.hedvig.app.R
import com.hedvig.app.ui.decoration.GridSpacingItemDecoration
import kotlinx.android.synthetic.main.offer_peril_area.view.*

class PerilBinder(
    private val root: LinearLayout,
    private val fragmentManager: FragmentManager
) {
    var contract: TypeOfContract? = null
        set(value) {
            field = value
            when (value) {
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

    fun bind(list: List<OfferQuery.Peril>) = root.apply {
        val adapter = PerilAdapter(fragmentManager)
        perilsRecycler.apply {
            this.adapter = adapter
            addItemDecoration(GridSpacingItemDecoration(BASE_MARGIN_HALF))
        }
        adapter.list = list
    }
}
