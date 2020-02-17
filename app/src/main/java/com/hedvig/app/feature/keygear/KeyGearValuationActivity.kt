package com.hedvig.app.feature.keygear

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.hedvig.android.owldroid.type.MonetaryAmountV2Input
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.keygear.ui.itemdetail.KeyGearItemDetailViewModel
import com.hedvig.app.feature.keygear.ui.itemdetail.PurchaseDateYearMonthPicker
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.safeLet
import kotlinx.android.synthetic.main.activity_key_gear_valuation.*
import org.koin.android.viewmodel.ext.android.viewModel
import org.threeten.bp.YearMonth
import timber.log.Timber
import java.text.DateFormatSymbols

class KeyGearValuationActivity : BaseActivity(R.layout.activity_key_gear_valuation) {

    private val model: KeyGearItemDetailViewModel by viewModel()
    private val valuationModel: KeyGearValuationViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val id = intent.getStringExtra(ITEM_ID)
        var date: YearMonth? = null

        continueButton.isEnabled = false

        dateInput.setHapticClickListener {
            PurchaseDateYearMonthPicker.newInstance(resources.getString(R.string.KEY_GEAR_YEARMONTH_PICKER_TITLE))
                .show(supportFragmentManager, PurchaseDateYearMonthPicker.TAG)
        }

        close.setHapticClickListener {
            onBackPressed()
        }

        continueButton.setHapticClickListener {
            val price = priceInput.getText()

            safeLet(date, id) { date, id ->
                val monetaryValue =
                    MonetaryAmountV2Input.builder().amount(price).currency("SEK").build()
                model.updatePurchaseDateAndPrice(id, date, monetaryValue)
                onBackPressed()
            }
        }

        priceInput.setOnChangeListener {
            val text = priceInput.getText()
            setButtonState(text.isNotEmpty(), date != null)
        }

        valuationModel.purchaseDate.observe(this) { yearMonth ->
            setButtonState(priceInput.getText().isNotEmpty(), yearMonth != null)
            yearMonth?.let {
                date = yearMonth
                dateInput.text =
                    "${DateFormatSymbols().months[yearMonth.month.value - 1]} ${yearMonth.year}"
                Timber.d(yearMonth.toString())
            }
        }
    }

    private fun setButtonState(hasPrice: Boolean, hasDate: Boolean) {
        continueButton.isEnabled = hasPrice && hasDate
    }

    companion object {
        private const val ITEM_ID = "ITEM_ID"

        fun newInstance(context: Context, id: String) =
            Intent(context, KeyGearValuationActivity::class.java).apply {
                putExtra(ITEM_ID, id)
            }
    }
}
