package com.hedvig.app.feature.offer

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import androidx.core.widget.NestedScrollView
import com.hedvig.android.owldroid.fragment.CostFragment
import com.hedvig.android.owldroid.fragment.IncentiveFragment
import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.app.BASE_MARGIN_OCTUPLE
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.offer.binders.FactAreaBinder
import com.hedvig.app.feature.offer.binders.PerilBinder
import com.hedvig.app.feature.offer.binders.TermsBinder
import com.hedvig.app.feature.settings.SettingsActivity
import com.hedvig.app.service.LoginStatusService.Companion.IS_VIEWING_OFFER
import com.hedvig.app.util.boundedColorLerp
import com.hedvig.app.util.extensions.compatColor
import com.hedvig.app.util.extensions.getStringId
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.setStrikethrough
import com.hedvig.app.util.extensions.showAlert
import com.hedvig.app.util.extensions.startClosableChat
import com.hedvig.app.util.extensions.storeBoolean
import com.hedvig.app.util.extensions.view.fadeIn
import com.hedvig.app.util.extensions.view.hide
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.view.updatePadding
import com.hedvig.app.util.interpolateTextKey
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import kotlinx.android.synthetic.main.activity_offer.*
import kotlinx.android.synthetic.main.loading_spinner.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import org.threeten.bp.LocalDate
import java.math.BigDecimal

class OfferActivity : BaseActivity(R.layout.activity_offer) {

    private val offerViewModel: OfferViewModel by viewModel()
    private val tracker: OfferTracker by inject()

    private lateinit var factAreaBinder: FactAreaBinder
    private lateinit var termsBinder: TermsBinder
    private lateinit var perilBinder: PerilBinder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        factAreaBinder = FactAreaBinder(offerFactArea as LinearLayout)
        termsBinder = TermsBinder(offerTermsArea as LinearLayout, tracker)
        perilBinder = PerilBinder(offerPerilArea as LinearLayout, supportFragmentManager)

        offerViewModel.data.observe(lifecycleOwner = this) {
            it?.let { data ->
                perilBinder.bind(data)
                termsBinder.bind(data)

                container.show()
                loadingSpinner.remove()
                setupButtons()

                if (data.contracts.isNotEmpty()) {
                    storeBoolean(IS_VIEWING_OFFER, false)
                    startActivity(Intent(this, LoggedInActivity::class.java).apply {
                        putExtra(LoggedInActivity.EXTRA_IS_FROM_ONBOARDING, true)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    })
                } else {
                    val completeQuote = data.lastQuoteOfMember.asCompleteQuote
                    completeQuote?.let {
                        when {
                            completeQuote.quoteDetails.asSwedishApartmentQuoteDetails != null -> {
                                val apartmentData =
                                    data.lastQuoteOfMember.asCompleteQuote.quoteDetails.asSwedishApartmentQuoteDetails!!
                                bindToolBar(apartmentData.street)
                                bindPremiumBox(
                                    completeQuote.typeOfContract,
                                    completeQuote.insuranceCost.fragments.costFragment,
                                    completeQuote.startDate,
                                    data.redeemedCampaigns.firstOrNull()?.fragments?.incentiveFragment?.incentive
                                )
                            }
                            completeQuote.quoteDetails.asSwedishHouseQuoteDetails != null -> {
                                val houseData =
                                    data.lastQuoteOfMember.asCompleteQuote.quoteDetails.asSwedishHouseQuoteDetails!!
                                bindToolBar(houseData.street)
                                bindPremiumBox(
                                    completeQuote.typeOfContract,
                                    completeQuote.insuranceCost.fragments.costFragment,
                                    completeQuote.startDate,
                                    data.redeemedCampaigns.firstOrNull()?.fragments?.incentiveFragment?.incentive
                                )
                            }
                        }
                        factAreaBinder.bind(completeQuote)
                    }
                }

            }
        }

        settings.setHapticClickListener {
            startActivity(SettingsActivity.newInstance(this))
        }

        offerChatButton.setHapticClickListener {
            tracker.openChat()
            offerViewModel.triggerOpenChat {
                startClosableChat(true)
            }
        }
    }

    private fun bindToolBar(address: String) {
        offerToolbarAddress.text = address
        offerToolbar.fadeIn()
    }

    private fun bindPremiumBox(
        type: TypeOfContract,
        insuranceCost: CostFragment,
        startDate: LocalDate?,
        incentive: IncentiveFragment.Incentive?
    ) {

        premiumBoxTitle.setText(type.getStringId())
        premium.text = BigDecimal(insuranceCost.monthlyNet.amount).toInt().toString()
        if (BigDecimal(insuranceCost.monthlyDiscount.amount) > BigDecimal.ZERO) {
            grossPremium.setStrikethrough(true)
            grossPremium.text = BigDecimal(insuranceCost.monthlyGross.amount).toInt().toString()
        }

        startDateContainer.setHapticClickListener {
            tracker.chooseStartDate()
            ChangeDateBottomSheet.newInstance()
                .show(supportFragmentManager, ChangeDateBottomSheet.TAG)
        }
        startDate?.let {
            if (it != LocalDate.now()) {
                premiumBoxStartDate.text = it.toString()
            } else {
                premiumBoxStartDate.setText(R.string.START_DATE_TODAY)
            }
        } ?: premiumBoxStartDate.setText(R.string.START_DATE_TODAY)

        premiumBoxStartDate.setHapticClickListener {
            tracker.toolbarSign()
            OfferSignDialog.newInstance().show(supportFragmentManager, OfferSignDialog.TAG)
        }

        incentive?.let {
            discountButton.text = getString(R.string.OFFER_REMOVE_DISCOUNT_BUTTON)

            when {
                incentive.asFreeMonths != null -> {
                    premiumCampaignTitle.text = interpolateTextKey(
                        getString(R.string.OFFER_SCREEN_FREE_MONTHS_BUBBLE).replace('\n', ' '),
                        "free_month" to incentive.asFreeMonths.quantity
                    )
                    offerPremiumContainer.setBackgroundResource(R.drawable.background_premium_box_with_campaign)
                    premiumCampaignTitle.show()
                }
                incentive.asMonthlyCostDeduction != null -> {
                    premiumCampaignTitle.text = getString(R.string.OFFER_SCREEN_INVITED_BUBBLE)
                    offerPremiumContainer.setBackgroundResource(R.drawable.background_premium_box_with_campaign)
                    premiumCampaignTitle.show()
                }
                incentive.asPercentageDiscountMonths != null -> {
                    premiumCampaignTitle.text =
                        if (incentive.asPercentageDiscountMonths.pdmQuantity == 1) {
                            interpolateTextKey(
                                getString(R.string.OFFER_SCREEN_PERCENTAGE_DISCOUNT_BUBBLE_TITLE_SINGULAR),
                                "percentage" to incentive.asPercentageDiscountMonths.percentageDiscount.toInt()
                            )
                        } else {
                            interpolateTextKey(
                                getString(R.string.OFFER_SCREEN_PERCENTAGE_DISCOUNT_BUBBLE_TITLE_PLURAL),
                                "months" to incentive.asPercentageDiscountMonths.pdmQuantity,
                                "percentage" to incentive.asPercentageDiscountMonths.percentageDiscount.toInt()
                            )
                        }
                    grossPremium.text = interpolateTextKey(
                        getString(R.string.OFFER_GROSS_PREMIUM),
                        "GROSS_PREMIUM" to insuranceCost.monthlyGross.amount.toBigDecimal().toInt()
                    )
                    offerPremiumContainer.setBackgroundResource(R.drawable.background_premium_box_with_campaign)
                }
                incentive.asNoDiscount != null -> {
                    discountButton.remove()
                }
                else -> {
                }
            }
        } ?: run {
            discountButton.text = getString(R.string.OFFER_ADD_DISCOUNT_BUTTON)
            premiumCampaignTitle.hide()
            offerPremiumContainer.background = null
        }

        discountButton.setHapticClickListener {
            if (incentive == null) {
                tracker.addDiscount()
                OfferRedeemCodeDialog.newInstance()
                    .show(supportFragmentManager, OfferRedeemCodeDialog.TAG)
            } else {
                tracker.removeDiscount()
                showAlert(
                    R.string.OFFER_REMOVE_DISCOUNT_ALERT_TITLE,
                    R.string.OFFER_REMOVE_DISCOUNT_ALERT_DESCRIPTION,
                    R.string.OFFER_REMOVE_DISCOUNT_ALERT_REMOVE,
                    R.string.OFFER_REMOVE_DISCOUNT_ALERT_CANCEL,
                    {
                        offerViewModel.removeDiscount()
                    }
                )
            }
        }
        initializeToolbar()
    }

    private fun initializeToolbar() {
        offerToolbar.doOnApplyWindowInsets { view, insets, initialState ->
            view.updatePadding(top = initialState.paddings.top + insets.systemWindowInsetTop)
        }
        setSupportActionBar(offerToolbar)
        offerScroll.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, _: Int ->
            val positionInSpan =
                scrollY - (BASE_MARGIN_OCTUPLE - (offerToolbar.height.toFloat()))
            val percentage = positionInSpan / offerToolbar.height

            if (percentage < -1 || percentage > 2) {
                return@setOnScrollChangeListener
            }

            offerToolbar.setBackgroundColor(
                boundedColorLerp(
                    Color.TRANSPARENT,
                    compatColor(R.color.translucent_tool_bar),
                    percentage
                )
            )
        }
    }

    private fun setupButtons() {
        signButton.setHapticClickListener {
            tracker.floatingSign()
            OfferSignDialog.newInstance().show(supportFragmentManager, OfferSignDialog.TAG)
        }

        premiumBoxSignButton.setHapticClickListener {
            tracker.floatingSign()
            OfferSignDialog.newInstance().show(supportFragmentManager, OfferSignDialog.TAG)
        }
    }
}
