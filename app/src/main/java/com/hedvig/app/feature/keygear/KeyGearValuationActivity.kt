package com.hedvig.app.feature.keygear

import android.animation.ValueAnimator
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import com.hedvig.android.owldroid.type.MonetaryAmountV2Input
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.keygear.ui.createitem.label
import com.hedvig.app.util.boundedLerp
import com.hedvig.app.util.extensions.dp
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.interpolateTextKey
import com.hedvig.app.util.safeLet
import kotlinx.android.synthetic.main.activity_key_gear_valuation.*
import org.koin.android.viewmodel.ext.android.viewModel
import org.threeten.bp.LocalDate
import java.text.DateFormatSymbols
import java.util.Calendar

class KeyGearValuationActivity : BaseActivity(R.layout.activity_key_gear_valuation) {
    private val model: KeyGearValuationViewModel by viewModel()

    private var isUploading = false
    var id: String = ""
    private var date: LocalDate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        id = intent.getStringExtra(ITEM_ID)

        saveContainer.show()
        model.data.observe(this) { data ->
            data?.let {
                val category =
                    resources.getString(data.fragments.keyGearItemFragment.category.label)
                        .toLowerCase()

                body.text = interpolateTextKey(
                    getString(R.string.KEY_GEAR_ITEM_VIEW_ADD_PURCHASE_DATE_BODY),
                    "ITEM_TYPE" to category
                )
            }
        }
        model.loadItem(id)


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

            }
        }

        priceInput.setOnChangeListener {
            val text = priceInput.getText()
            setButtonState(text.isNotEmpty(), date != null)
        }

        model.finishedUploading.observe(this) { finishedUploading ->
            finishedUploading?.let {
                if (finishedUploading) {
                    id?.let { id ->
                        startActivity(
                            KeyGearValuationInfoActivity.newInstance(
                                this,
                                id
                            )
                        )
                        finish()
                    }
                }
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
            saveContainer.backgroundTintList =
                ContextCompat.getColorStateList(this, R.color.link_purple)
        } else {
            save.isEnabled = false
            saveContainer.backgroundTintList =
                ContextCompat.getColorStateList(this, R.color.semi_light_gray)
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
