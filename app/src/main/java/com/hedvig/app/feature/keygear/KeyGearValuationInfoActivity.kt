package com.hedvig.app.feature.keygear

import android.os.Bundle
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.util.extensions.setMarkdownText
import kotlinx.android.synthetic.main.activity_key_gear_valuation_info.*

class KeyGearValuationInfoActivity : BaseActivity(R.layout.activity_key_gear_valuation_info) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //TODO get valuation percentage
        setPercentage(12)
    }

    private fun setPercentage(percentage: Int) {
        valuationPercentage.setMarkdownText("$percentage%")
    }
}
