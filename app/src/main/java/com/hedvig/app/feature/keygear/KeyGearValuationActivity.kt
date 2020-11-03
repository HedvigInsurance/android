package com.hedvig.app.feature.keygear

import android.animation.ValueAnimator
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import com.hedvig.android.owldroid.graphql.KeyGearItemQuery
import com.hedvig.android.owldroid.type.MonetaryAmountV2Input
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityKeyGearValuationBinding
import com.hedvig.app.feature.keygear.ui.ValuationData
import com.hedvig.app.feature.keygear.ui.createitem.label
import com.hedvig.app.util.extensions.colorAttr
import com.hedvig.app.util.extensions.onChange
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.view.updateMargin
import com.hedvig.app.util.extensions.view.updatePadding
import com.hedvig.app.util.extensions.viewBinding
import com.hedvig.app.util.safeLet
import com.hedvig.app.util.spring
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import dev.chrisbanes.insetter.setEdgeToEdgeSystemUiFlags
import e
import org.koin.android.viewmodel.ext.android.viewModel
import java.text.DateFormatSymbols
import java.time.LocalDate
import java.util.Calendar
import java.util.Locale

class KeyGearValuationActivity : BaseActivity(R.layout.activity_key_gear_valuation) {
    private val model: KeyGearValuationViewModel by viewModel()
    private val binding by viewBinding(ActivityKeyGearValuationBinding::bind)

    private var isUploading = false
    private var date: LocalDate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val id = intent.getStringExtra(ITEM_ID)
        if (id == null) {
            e { "Programmer error: No ID passed to ${this.javaClass}" }
            return
        }
        var maxInsurableAmount = 0

        binding.apply {
            saveContainer.measure(
                View.MeasureSpec.makeMeasureSpec(
                    root.width,
                    View.MeasureSpec.EXACTLY
                ), View.MeasureSpec.makeMeasureSpec(root.height, View.MeasureSpec.AT_MOST)
            )

            scrollView.updatePadding(
                bottom = scrollView.paddingBottom + saveContainer.measuredHeight
            )

            root.setEdgeToEdgeSystemUiFlags(true)

            topBar.doOnApplyWindowInsets { view, insets, initialState ->
                view.updatePadding(top = initialState.paddings.top + insets.systemWindowInsetTop)
            }

            scrollView.doOnApplyWindowInsets { view, insets, initialState ->
                view.updatePadding(bottom = initialState.paddings.bottom + insets.systemWindowInsetBottom)
            }

            saveContainer.doOnApplyWindowInsets { view, insets, initialState ->
                view.updateMargin(bottom = initialState.margins.bottom + insets.systemWindowInsetBottom)
            }

            model.data.observe(this@KeyGearValuationActivity) { data ->
                safeLet(
                    data,
                    data?.fragments?.keyGearItemFragment?.maxInsurableAmount?.amount
                ) { d, amount ->
                    maxInsurableAmount = amount.toBigDecimal().toInt()
                    val category =
                        resources.getString(d.fragments.keyGearItemFragment.category.label)
                            .toLowerCase(Locale.ROOT)
                    noCoverage.text = getString(R.string.KEY_GEAR_NOT_COVERED, category)
                    body.text =
                        getString(R.string.KEY_GEAR_ITEM_VIEW_ADD_PURCHASE_DATE_BODY, category)

                }
            }
            model.loadItem(id)


            dateInput.setHapticClickListener {
                val calendar = Calendar.getInstance()
                DatePickerDialog(
                    this@KeyGearValuationActivity,
                    DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                        date = LocalDate.of(year, month + 1, dayOfMonth)

                        val monthText = DateFormatSymbols().months[month]
                        dateInput.text = "$dayOfMonth $monthText $year"

                        setButtonState(!priceInput.text.isNullOrEmpty(), date != null)
                    },
                    date?.year ?: calendar.get(Calendar.YEAR),
                    date?.monthValue ?: calendar.get(Calendar.MONTH),
                    date?.dayOfMonth ?: calendar.get(Calendar.DAY_OF_MONTH)
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

                val price = priceInput.text.toString()
                safeLet(date, id) { date, id ->
                    val monetaryValue =
                        MonetaryAmountV2Input(amount = price, currency = "SEK")

                    model.updatePurchaseDateAndPrice(id, date, monetaryValue)
                }
            }

            priceInput.onChange {
                val text = priceInput.text.toString()
                setButtonState(text.isNotEmpty(), date != null)
                if (!text.isBlank()) {
                    try {
                        val value = text.toDouble()
                        if (value > maxInsurableAmount.toDouble()) {
                            animateDateDown()
                            noCoverage.show()
                        } else {
                            animateDateUp()
                            noCoverage.remove()
                        }
                    } catch (e: Exception) {
                    }
                }
            }
        }

        model.uploadResult.observe(this) { uploadResult ->
            safeLet(
                uploadResult?.keyGearItem,
                uploadResult?.keyGearItem?.fragments?.keyGearItemFragment?.purchasePrice?.amount
            ) { item, amount ->
                val type = valuationType(item)
                if (type == ValuationType.FIXED) {
                    val valuation =
                        item.fragments.keyGearItemFragment.fragments.keyGearItemValuationFragment.valuation?.asKeyGearItemValuationFixed
                            ?: return@safeLet
                    startActivity(
                        KeyGearValuationInfoActivity.newInstance(
                            this,
                            item.fragments.keyGearItemFragment.category,
                            ValuationData.from(
                                amount,
                                type,
                                valuation.ratio,
                                valuation.valuation.amount
                            )
                        )
                    )
                    finish()
                } else if (type == ValuationType.MARKET_PRICE) {
                    val ratio =
                        item.fragments.keyGearItemFragment.fragments.keyGearItemValuationFragment.valuation?.asKeyGearItemValuationMarketValue?.ratio
                            ?: return@safeLet
                    startActivity(
                        KeyGearValuationInfoActivity.newInstance(
                            this,
                            item.fragments.keyGearItemFragment.category,
                            ValuationData.from(
                                amount,
                                type,
                                ratio
                            )
                        )
                    )
                    finish()
                }
            }
        }
    }

    private fun animateDateDown() {
        binding.apply {
            dateInput.spring(
                SpringAnimation.TRANSLATION_Y,
                SpringForce.STIFFNESS_HIGH,
                SpringForce.DAMPING_RATIO_NO_BOUNCY
            ).animateToFinalPosition(getNoCoverageHeight())

            saveContainer.spring(
                SpringAnimation.TRANSLATION_Y,
                SpringForce.STIFFNESS_HIGH,
                SpringForce.DAMPING_RATIO_NO_BOUNCY
            ).animateToFinalPosition(getNoCoverageHeight())
        }
    }

    private fun animateDateUp() {
        binding.apply {
            dateInput.spring(
                SpringAnimation.TRANSLATION_Y,
                SpringForce.STIFFNESS_HIGH,
                SpringForce.DAMPING_RATIO_NO_BOUNCY
            ).animateToFinalPosition(-(getNoCoverageHeight() / 50f))

            saveContainer.spring(
                SpringAnimation.TRANSLATION_Y,
                SpringForce.STIFFNESS_HIGH,
                SpringForce.DAMPING_RATIO_NO_BOUNCY
            ).animateToFinalPosition(-(getNoCoverageHeight() / 50f))
        }
    }

    private fun getNoCoverageHeight(): Float {
        binding.apply {
            noCoverage.measure(
                View.MeasureSpec.UNSPECIFIED,
                View.MeasureSpec.UNSPECIFIED
            )
            return noCoverage.measuredHeight.toFloat()
        }
    }

    private fun valuationType(item: KeyGearItemQuery.KeyGearItem): ValuationType? {
        val valuation =
            item.fragments.keyGearItemFragment.fragments.keyGearItemValuationFragment.valuation

        return when {
            valuation?.asKeyGearItemValuationFixed != null -> ValuationType.FIXED
            valuation?.asKeyGearItemValuationMarketValue != null -> ValuationType.MARKET_PRICE
            else -> null
        }
    }

    private fun transitionToUploading() {
        binding.apply {
            loadingIndicator.show()
            ValueAnimator.ofInt(saveContainer.width, saveContainer.height).apply {
                interpolator = AccelerateDecelerateInterpolator()
                duration = SAVE_BUTTON_TRANSITION_DURATION
                addUpdateListener { va ->
                    saveContainer.updateLayoutParams<ConstraintLayout.LayoutParams> {
                        width = va.animatedValue as Int
                    }
                    save.alpha = 1 - va.animatedFraction
                    loadingIndicator.alpha = va.animatedFraction
                }
                start()
            }
        }
    }

    private fun setButtonState(hasPrice: Boolean, hasDate: Boolean) {
        binding.apply {
            if (hasPrice && hasDate) {
                save.isEnabled = true
                saveContainer.backgroundTintList =
                    ColorStateList.valueOf(colorAttr(R.attr.colorButton))
            } else {
                save.isEnabled = false
                saveContainer.backgroundTintList =
                    ContextCompat.getColorStateList(
                        this@KeyGearValuationActivity,
                        R.color.semi_light_gray
                    )
            }
        }
    }

    companion object {
        private const val ITEM_ID = "ITEM_ID"

        private const val SAVE_BUTTON_TRANSITION_DURATION = 200L

        fun newInstance(context: Context, id: String) =
            Intent(context, KeyGearValuationActivity::class.java).apply {
                putExtra(ITEM_ID, id)
            }
    }
}
