package com.hedvig.app.feature.keygear

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hedvig.app.R
import com.hedvig.app.feature.keygear.ui.itemdetail.KeyGearValuationViewModelImpl
import com.hedvig.app.feature.keygear.ui.itemdetail.PurchaseDateYearMonthPicker
import com.hedvig.app.util.extensions.observe
import kotlinx.android.synthetic.main.activity_key_gear_valuation.*
import org.koin.android.viewmodel.ext.android.viewModel
import java.text.DateFormatSymbols

class KeyGearValuationActivity : AppCompatActivity() {

    private val model: KeyGearValuationViewModelImpl by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.hedvig.app.R.layout.activity_key_gear_valuation)

        dateInput.setOnClickListener {
            PurchaseDateYearMonthPicker.newInstance(resources.getString(R.string.KEY_GEAR_YEARMONTH_PICKER_TITLE))
                .show(supportFragmentManager, PurchaseDateYearMonthPicker.TAG)
        }

        close.setOnClickListener {
            onBackPressed()
        }

        continueButton.setOnClickListener {
            model.submit()
            onBackPressed()
        }

        model.purchaseDate.observe(this) { yearMonth ->
            yearMonth?.let {
                dateInput.text =
                    "${DateFormatSymbols().months[yearMonth.month.value - 1]} ${yearMonth.year}"
            }
        }
    }
}
