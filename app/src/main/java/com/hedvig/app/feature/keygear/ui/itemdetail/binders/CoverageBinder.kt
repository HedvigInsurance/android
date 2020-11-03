package com.hedvig.app.feature.keygear.ui.itemdetail.binders

import com.hedvig.android.owldroid.graphql.KeyGearItemQuery
import com.hedvig.app.databinding.KeyGearItemDetailCoverageSectionBinding

class CoverageBinder(
    private val root: KeyGearItemDetailCoverageSectionBinding
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
