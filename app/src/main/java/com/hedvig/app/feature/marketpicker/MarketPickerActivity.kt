package com.hedvig.app.feature.marketpicker

import android.os.Bundle
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import kotlinx.android.synthetic.main.activity_market_picker.*
import org.koin.android.viewmodel.ext.android.viewModel

class MarketPickerActivity : BaseActivity(R.layout.activity_market_picker) {
    private val model: MarketPickerViewModelImpl by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        countryList.adapter = MarketAdapter(model)
    }
}
