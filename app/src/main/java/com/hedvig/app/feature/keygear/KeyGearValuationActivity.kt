package com.hedvig.app.feature.keygear

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import com.hedvig.android.owldroid.type.MonetaryAmountV2Input
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.keygear.ui.itemdetail.KeyGearItemDetailViewModel
import com.hedvig.app.feature.keygear.ui.itemdetail.KeyGearValuationViewModelImpl
import com.hedvig.app.feature.keygear.ui.itemdetail.PurchaseDateYearMonthPicker
import com.hedvig.app.util.extensions.compatColor
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.onChange
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

        val id = intent.getStringExtra(ITEM_ID)
        var date: YearMonth? = null

        continueButton.isEnabled = false
        continueButton.backgroundTintList =
            ColorStateList.valueOf(this.compatColor(R.color.semi_light_gray))

        dateInput.setHapticClickListener {
            PurchaseDateYearMonthPicker.newInstance(resources.getString(R.string.KEY_GEAR_YEARMONTH_PICKER_TITLE))
                .show(supportFragmentManager, PurchaseDateYearMonthPicker.TAG)
        }

        close.setHapticClickListener {
            onBackPressed()
        }

        continueButton.setHapticClickListener {
            val price = priceInput.getText()
            date?.let { date ->
                val monetaryValue =
                    MonetaryAmountV2Input.builder().amount(price).currency("SEK").build()
                itemModel.updatePurchaseDateAndPrice(id, date, monetaryValue)
                onBackPressed()
            }
        }

        priceInput.getEditText().onChange { text ->
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
        if (hasPrice && hasDate) {
            continueButton.isEnabled = true
            continueButton.backgroundTintList =
                ColorStateList.valueOf(this.compatColor(R.color.purple))
        } else {
            continueButton.isEnabled = false
            continueButton.backgroundTintList =
                ColorStateList.valueOf(this.compatColor(R.color.semi_light_gray))
        }
    }

    companion object {

        private const val ITEM_ID = "ITEM_ID"

        fun newInstance(context: Context, id: String) =
            Intent(context, KeyGearValuationActivity::class.java).apply {
                putExtra(ITEM_ID, id)
            }
    }
}
