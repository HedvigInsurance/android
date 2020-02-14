package com.hedvig.app.feature.keygear

import android.os.Bundle
import com.hedvig.android.owldroid.type.MonetaryAmountV2Input
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.keygear.ui.itemdetail.KeyGearItemDetailViewModel
import com.hedvig.app.feature.keygear.ui.itemdetail.KeyGearValuationViewModelImpl
import com.hedvig.app.feature.keygear.ui.itemdetail.PurchaseDateYearMonthPicker
import com.hedvig.app.util.extensions.makeToast
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.view.setHapticClickListener
import kotlinx.android.synthetic.main.activity_key_gear_valuation.*
import org.koin.android.viewmodel.ext.android.viewModel
import org.threeten.bp.YearMonth
import timber.log.Timber
import java.text.DateFormatSymbols

class KeyGearValuationActivity :
    BaseActivity(R.layout.activity_key_gear_valuation) {

    private val itemModel: KeyGearItemDetailViewModel by viewModel()
    private val valuationModel: KeyGearValuationViewModelImpl by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var date: YearMonth? = null

        dateInput.setHapticClickListener {
            PurchaseDateYearMonthPicker.newInstance(resources.getString(R.string.KEY_GEAR_YEARMONTH_PICKER_TITLE))
                .show(supportFragmentManager, PurchaseDateYearMonthPicker.TAG)
        }

        close.setHapticClickListener {
            onBackPressed()
        }

        continueButton.setHapticClickListener {

            val price = priceInput.getText()
            if (price != "") {
                date?.let { date ->
                    val monetaryValue =
                        MonetaryAmountV2Input.builder().amount(price).currency("sek").build()
                    itemModel.updatePurchaseDateAndPrice("123", date, monetaryValue)
                } ?: run {
                    makeToast("no input")
                }
                // onBackPressed()
            } else {
                makeToast("no input 2")
            }
        }

        valuationModel.purchaseDate.observe(this) { yearMonth ->
            yearMonth?.let {
                date = yearMonth
                dateInput.text =
                    "${DateFormatSymbols().months[yearMonth.month.value - 1]} ${yearMonth.year}"
                Timber.d(yearMonth.toString())
            }
        }
    }
}
