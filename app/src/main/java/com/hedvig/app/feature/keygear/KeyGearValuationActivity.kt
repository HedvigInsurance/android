package com.hedvig.app.feature.keygear

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import androidx.core.view.updateLayoutParams
import com.hedvig.android.owldroid.type.MonetaryAmountV2Input
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.keygear.ui.itemdetail.PurchaseDateYearMonthPicker
import com.hedvig.app.util.boundedLerp
import com.hedvig.app.util.extensions.dp
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.safeLet
import kotlinx.android.synthetic.main.activity_key_gear_valuation.*
import org.koin.android.viewmodel.ext.android.viewModel
import org.threeten.bp.YearMonth
import java.text.DateFormatSymbols

class KeyGearValuationActivity : BaseActivity(R.layout.activity_key_gear_valuation) {

    private val model: KeyGearValuationViewModel by viewModel()
    private var isUploading = false
    private var date: YearMonth? = null
    lateinit var id: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        id = intent.getStringExtra(ITEM_ID)
        saveContainer.show()

        dateInput.setHapticClickListener {
            PurchaseDateYearMonthPicker.newInstance(resources.getString(R.string.KEY_GEAR_YEARMONTH_PICKER_TITLE))
                .show(supportFragmentManager, PurchaseDateYearMonthPicker.TAG)
        }

        close.setHapticClickListener {
            onBackPressed()
        }

        save.setHapticClickListener {
            if (isUploading) {
                return@setHapticClickListener
            }
            isUploading = true
            transitionToUploading()

            val price = priceInput.getText()
            safeLet(date, id) { date, id ->
                val monetaryValue =
                    MonetaryAmountV2Input.builder().amount(price).currency("SEK").build()

                model.updatePurchaseDateAndPrice(id, date, monetaryValue)
                Handler().postDelayed(
                    {
                        startActivity(
                            KeyGearValuationInfoActivity.newInstance(
                                applicationContext,
                                id
                            )
                        )
                    },
                    500L
                )

            }
        }

        priceInput.setOnChangeListener {
            val text = priceInput.getText()
            setButtonState(text.isNotEmpty(), date != null)
        }

        model.purchaseDate.observe(this) { yearMonth ->
            setButtonState(priceInput.getText().isNotEmpty(), yearMonth != null)
            yearMonth?.let {
                date = yearMonth
                dateInput.text =
                    "${DateFormatSymbols().months[yearMonth.month.value - 1]} ${yearMonth.year}"
            }
        }
    }

    private fun transitionToUploading() {
        loadingIndicator.show()
        val startCornerRadius = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ((saveContainer.background as RippleDrawable).getDrawable(0) as GradientDrawable).cornerRadius
        } else {
            BUTTON_CORNER_RADIUS
        }
        ValueAnimator.ofInt(saveContainer.width, saveContainer.height).apply {
            interpolator = AccelerateDecelerateInterpolator()
            duration = SAVE_BUTTON_TRANSITION_DURATION
            addUpdateListener { va ->
                saveContainer.updateLayoutParams<LinearLayout.LayoutParams> {
                    width = va.animatedValue as Int
                }
                save.alpha = 1 - va.animatedFraction
                loadingIndicator.alpha = va.animatedFraction
                val backgroundShape =
                    ((saveContainer.background as? RippleDrawable)?.getDrawable(0) as? GradientDrawable)?.mutate() as? GradientDrawable
                backgroundShape?.cornerRadius =
                    boundedLerp(startCornerRadius, saveContainer.height / 2f, va.animatedFraction)
            }
            start()
        }
    }

    private fun setButtonState(hasPrice: Boolean, hasDate: Boolean) {
        if (hasPrice && hasDate) {
            save.isEnabled = true
            saveContainer.backgroundTintList = resources.getColorStateList(R.color.link_purple)
        } else {
            save.isEnabled = false
            saveContainer.backgroundTintList = resources.getColorStateList(R.color.semi_light_gray)
        }
    }

    companion object {
        private const val ITEM_ID = "ITEM_ID"

        private val BUTTON_CORNER_RADIUS = 112.dp.toFloat()

        private const val SAVE_BUTTON_TRANSITION_DURATION = 200L

        fun newInstance(context: Context, id: String) =
            Intent(context, KeyGearValuationActivity::class.java).apply {
                putExtra(ITEM_ID, id)
            }
    }
}
