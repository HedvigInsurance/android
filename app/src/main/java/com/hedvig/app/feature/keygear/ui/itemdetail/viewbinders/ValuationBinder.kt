package com.hedvig.app.feature.keygear.ui.itemdetail.viewbinders

import android.widget.LinearLayout
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.scale
import com.hedvig.android.owldroid.graphql.KeyGearItemQuery
import com.hedvig.app.R
import kotlinx.android.synthetic.main.key_gear_item_detail_valuation_section.view.*

class ValuationBinder(
    private val root: LinearLayout
) {
    fun bind(data: KeyGearItemQuery.KeyGearItem) {
        root.deductible.text = buildSpannedString {
            bold {
                scale(2.0f) {
                    // TODO: Get this from backend when available
                    append("1 500")
                }
            }
            append(" ")
            append(root.resources.getString(R.string.KEY_GEAR_ITEM_VIEW_DEDUCTIBLE_CURRENCY))
        }
    }
}
