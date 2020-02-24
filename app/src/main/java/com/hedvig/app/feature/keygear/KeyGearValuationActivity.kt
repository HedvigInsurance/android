package com.hedvig.app.feature.keygear

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.hedvig.android.owldroid.type.MonetaryAmountV2Input
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.safeLet
import kotlinx.android.synthetic.main.activity_key_gear_valuation.*
import org.koin.android.viewmodel.ext.android.viewModel
import org.threeten.bp.LocalDate
import java.text.DateFormatSymbols
import java.util.Calendar

class KeyGearValuationActivity : BaseActivity(R.layout.activity_key_gear_valuation) {
    private val model: KeyGearValuationViewModel by viewModel()

    private var date: LocalDate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val id = intent.getStringExtra(ITEM_ID)

        dateInput.setHapticClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                    date = LocalDate.of(year, month, dayOfMonth)

                    val monthText = DateFormatSymbols().months[month]
                    dateInput.text = "$dayOfMonth $monthText $year"

                    setButtonState(priceInput.getText().isNotEmpty(), date != null)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.maxDate = calendar.time.time
                show()
            }
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
                //TODO
                onBackPressed()
            }
        }

        priceInput.setOnChangeListener {
            val text = priceInput.getText()
            setButtonState(text.isNotEmpty(), date != null)
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
