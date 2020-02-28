package com.hedvig.app.feature.keygear.ui.itemdetail.binders

import android.widget.LinearLayout
import com.hedvig.android.owldroid.graphql.KeyGearItemQuery
import kotlinx.android.synthetic.main.key_gear_item_detail_coverage_section.view.*

class CoverageBinder(
    private val root: LinearLayout
) {
    init {
        root.covered.adapter = CoverageAdapter(false)

        root.exceptions.adapter = CoverageAdapter(true)
    }

    fun bind(data: KeyGearItemQuery.KeyGearItem) {
        (root.covered.adapter as? CoverageAdapter)?.items =
            data.fragments.keyGearItemFragment.covered.mapNotNull {
                it.title?.translations?.getOrNull(0)?.text
            }
        (root.exceptions.adapter as? CoverageAdapter)?.items =
            data.fragments.keyGearItemFragment.exceptions.mapNotNull {
                it.title?.translations?.getOrNull(0)?.text
            }
    }
}
