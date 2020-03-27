package com.hedvig.app.feature.offer.binders

import android.widget.LinearLayout
import com.hedvig.android.owldroid.graphql.OfferPreSaleQuery
import com.hedvig.app.BASE_MARGIN_HALF
import com.hedvig.app.ui.decoration.GridSpacingItemDecoration
import kotlinx.android.synthetic.main.offer_peril_area.view.*

class PerilBinder(
    private val root: LinearLayout
) {
    fun bind(list: List<OfferPreSaleQuery.Peril>) = root.apply {
        val adapter = PerilAdapter()
        perilsRecycler.apply {
            this.adapter = adapter
            addItemDecoration(GridSpacingItemDecoration(BASE_MARGIN_HALF))
        }
        adapter.list = list
    }
}
