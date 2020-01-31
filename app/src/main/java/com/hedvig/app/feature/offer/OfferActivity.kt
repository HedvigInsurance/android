package com.hedvig.app.feature.offer

import android.animation.ValueAnimator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.NestedScrollView
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import com.hedvig.android.owldroid.fragment.IncentiveFragment
import com.hedvig.android.owldroid.fragment.PerilCategoryFragment
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.dashboard.ui.PerilBottomSheet
import com.hedvig.app.feature.dashboard.ui.PerilIcon
import com.hedvig.app.feature.dashboard.ui.PerilView
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.offer.binders.FactAreaBinder
import com.hedvig.app.feature.offer.binders.FeatureBubbleBinder
import com.hedvig.app.feature.offer.binders.TermsBinder
import com.hedvig.app.feature.settings.SettingsActivity
import com.hedvig.app.service.LoginStatusService.Companion.IS_VIEWING_OFFER
import com.hedvig.app.util.boundedColorLerp
import com.hedvig.app.util.boundedLerp
import com.hedvig.app.util.extensions.compatColor
import com.hedvig.app.util.extensions.displayMetrics
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.setStrikethrough
import com.hedvig.app.util.extensions.showAlert
import com.hedvig.app.util.extensions.startClosableChat
import com.hedvig.app.util.extensions.storeBoolean
import com.hedvig.app.util.extensions.view.hide
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.view.spring
import com.hedvig.app.util.extensions.view.updatePadding
import com.hedvig.app.util.interpolateTextKey
import com.hedvig.app.util.isSigned
import com.hedvig.app.util.isStudentInsurance
import com.hedvig.app.util.safeLet
import kotlinx.android.synthetic.main.activity_offer.*
import kotlinx.android.synthetic.main.feature_bubbles.*
import kotlinx.android.synthetic.main.loading_spinner.*
import kotlinx.android.synthetic.main.offer_peril_section.view.*
import kotlinx.android.synthetic.main.offer_section_switch.*
import kotlinx.android.synthetic.main.offer_section_terms.view.*
import kotlinx.android.synthetic.main.price_bubbles.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import kotlin.math.min

class OfferActivity : BaseActivity(R.layout.activity_offer) {

    private val offerViewModel: OfferViewModel by viewModel()
    private val tracker: OfferTracker by inject()

    private val doubleMargin: Int by lazy { resources.getDimensionPixelSize(R.dimen.base_margin_double) }
    private val perilTotalWidth: Int by lazy {
        resources.getDimensionPixelSize(R.dimen.peril_width) + (doubleMargin * 2)
    }
    private val rowWidth: Int by lazy {
        displayMetrics.widthPixels - (doubleMargin * 2)
    }

    private val animationHandler = Handler()
    private var hasTriggeredAnimations = false
    private var lastAnimationHasCompleted = false

    private val perilWidth by lazy {
        resources.getDimensionPixelSize(R.dimen.peril_width)
    }

    private lateinit var featureBubbleBinder: FeatureBubbleBinder
    private lateinit var factAreaBinder: FactAreaBinder
    private lateinit var termsBinder: TermsBinder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        offerChatButton.setHapticClickListener {
            tracker.openChat()
            offerViewModel.triggerOpenChat {
                startClosableChat(true)
            }
        }

        featureBubbleBinder = FeatureBubbleBinder(featureBubbles as ConstraintLayout, tracker, supportFragmentManager)
        factAreaBinder = FactAreaBinder(offerFactBox as LinearLayout)
        termsBinder = TermsBinder(termsSection as ConstraintLayout, tracker)

        bindStaticData()

        offerViewModel.data.observe(lifecycleOwner = this) { data ->
            data?.let { d ->
                if (d.insurance.status.isSigned) {
                    storeBoolean(IS_VIEWING_OFFER, false)
                    startActivity(Intent(this, LoggedInActivity::class.java).apply {
                        putExtra(LoggedInActivity.EXTRA_IS_FROM_ONBOARDING, true)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    })
                }
                loadingSpinner.remove()
                container.show()
                bindToolbar(d)
                bindPriceBubbles(d)
                bindDiscountButton(d)
                factAreaBinder.bind(d.insurance)
                bindHomeSection(d)
                bindStuffSection(d)
                bindMeSection(d)
                termsBinder.bind(d.insurance)
                bindSwitchSection(d)
                featureBubbleBinder.bind(d)
                animateBubbles(d)
            }
        }
    }

    private fun bindToolbar(data: OfferQuery.Data) {
        offerToolbarAddress.text = data.insurance.address
    }

    private fun bindStaticData() {
        setSupportActionBar(offerToolbar)

        homeSection.paragraph.text = getString(R.string.OFFER_APARTMENT_PROTECTION_DESCRIPTION)
        homeSection.hero.setImageDrawable(getDrawable(R.drawable.offer_house))

        val quadrupleMargin = resources.getDimensionPixelSize(R.dimen.base_margin_quadruple)

        stuffSection.hero.setImageDrawable(getDrawable(R.drawable.offer_stuff))
        stuffSection.title.text = getString(R.string.OFFER_STUFF_PROTECTION_TITLE)
        stuffSection.updatePadding(top = quadrupleMargin)


        meSection.hero.setImageDrawable(getDrawable(R.drawable.offer_me))
        meSection.title.text = getString(R.string.OFFER_PERSONAL_PROTECTION_TITLE)
        meSection.paragraph.text = getString(R.string.OFFER_PERSONAL_PROTECTION_DESCRIPTION)
        meSection.updatePadding(top = quadrupleMargin)

        termsSection.privacyPolicy.setHapticClickListener {
            tracker.openTerms()
            startActivity(Intent(Intent.ACTION_VIEW, PRIVACY_POLICY_URL))
        }

        settings.setHapticClickListener {
            startActivity(SettingsActivity.newInstance(this))
        }

        grossPremium.setStrikethrough(true)

        setupButtons()
        setupScrollListeners()
    }

    private fun setupButtons() {
        signButton.setHapticClickListener {
            tracker.floatingSign()
            OfferSignDialog.newInstance().show(supportFragmentManager, OfferSignDialog.TAG)
        }
    }

    private fun setupScrollListeners() {
        offerScroll.setOnScrollChangeListener { _: NestedScrollView, _, scrollY, _, _ ->
            parallaxContainer.translationY = scrollY / 7.0f
            arrow.alpha = boundedLerp(1f, 0f, scrollY / 200f)
        }
    }

    private fun bindDiscountButton(data: OfferQuery.Data) {
        discountButton.text = if (hasActiveCampaign(data)) {
            getString(R.string.OFFER_REMOVE_DISCOUNT_BUTTON)
        } else {
            getString(R.string.OFFER_ADD_DISCOUNT_BUTTON)
        }

        discountButton.setHapticClickListener {
            if (hasActiveCampaign(data)) {
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
            } else {
                tracker.addDiscount()
                OfferRedeemCodeDialog.newInstance()
                    .show(supportFragmentManager, OfferRedeemCodeDialog.TAG)
            }
        }
    }

    private fun bindPriceBubbles(data: OfferQuery.Data) {
        grossPremium.hide()
        discountBubble.hide()
        discountTitle.remove()

        if (lastAnimationHasCompleted) {
            animatePremium(data.insurance.cost?.fragments?.costFragment?.monthlyNet?.amount?.toBigDecimal()?.toInt())
        } else {
            netPremium.setTextColor(compatColor(R.color.text_emphasized))
            netPremium.text =
                data.insurance.cost?.fragments?.costFragment?.monthlyNet?.amount?.toBigDecimal()
                    ?.toInt()?.toString()
        }

        if (data.redeemedCampaigns.size > 0) {
            when (val incentive = data.redeemedCampaigns[0].fragments.incentiveFragment.incentive) {
                is IncentiveFragment.AsMonthlyCostDeduction -> {
                    grossPremium.show()
                    grossPremium.text = interpolateTextKey(
                        getString(R.string.OFFER_GROSS_PREMIUM),
                        "GROSS_PREMIUM" to data.insurance.cost?.fragments?.costFragment?.monthlyGross?.amount?.toBigDecimal()?.toInt()
                    )

                    discountBubble.show()
                    discount.text = getString(R.string.OFFER_SCREEN_INVITED_BUBBLE)

                    if (!lastAnimationHasCompleted) {
                        netPremium.setTextColor(compatColor(R.color.pink))
                    }
                }
                is IncentiveFragment.AsFreeMonths -> {
                    discountBubble.show()
                    discountTitle.show()
                    discount.text = interpolateTextKey(
                        getString(R.string.OFFER_SCREEN_FREE_MONTHS_BUBBLE),
                        "free_month" to incentive.quantity
                    )
                }
                is IncentiveFragment.AsPercentageDiscountMonths -> {
                    grossPremium.show()
                    grossPremium.text = interpolateTextKey(
                        getString(R.string.OFFER_GROSS_PREMIUM),
                        "GROSS_PREMIUM" to data.insurance.cost?.fragments?.costFragment?.monthlyGross?.amount?.toBigDecimal()?.toInt()
                    )
                    discountBubble.show()
                    discountTitle.text =
                        getString(R.string.OFFER_SCREEN_PERCENTAGE_DISCOUNT_BUBBLE_TITLE)
                    discountTitle.show()
                    discount.text = if (incentive.pdmQuantity == 1) {
                        interpolateTextKey(
                            getString(R.string.OFFER_SCREEN_PERCENTAGE_DISCOUNT_BUBBLE_TITLE_SINGULAR),
                            "percentage" to incentive.percentageDiscount.toInt()
                        )
                    } else {
                        interpolateTextKey(
                            getString(R.string.OFFER_SCREEN_PERCENTAGE_DISCOUNT_BUBBLE_TITLE_PLURAL),
                            "months" to incentive.pdmQuantity,
                            "percentage" to incentive.percentageDiscount.toInt()
                        )
                    }
                    if (!lastAnimationHasCompleted) {
                        netPremium.setTextColor(compatColor(R.color.pink))
                    }
                }
            }
            if (lastAnimationHasCompleted) {
                animateDiscountBubble()
            }
        } else {
            discountBubble.scaleX = 0f
            discountBubble.scaleY = 0f
        }
    }

    private fun animatePremium(newNetPremium: Int?) {
        val prevNetPremium = netPremium.text.toString().toIntOrNull()
        safeLet(prevNetPremium, newNetPremium) { p, n ->
            if (p != n) {
                val colors = if (p > n) {
                    Pair(compatColor(R.color.text_emphasized), compatColor(R.color.pink))
                } else {
                    Pair(compatColor(R.color.pink), compatColor(R.color.text_emphasized))
                }
                ValueAnimator.ofInt(p, n).apply {
                    duration = 1000
                    interpolator = AccelerateDecelerateInterpolator()
                    addUpdateListener { v ->
                        netPremium.text = (v.animatedValue as? Int)?.toString()
                        netPremium.setTextColor(
                            boundedColorLerp(
                                colors.first,
                                colors.second,
                                v.animatedFraction
                            )
                        )
                    }
                    start()
                }
            }
        }
    }

    private fun animateBubbles(data: OfferQuery.Data) {
        if (hasTriggeredAnimations) {
            return
        }
        hasTriggeredAnimations = true
        animationHandler.postDelayed({
            performBubbleAnimation(netPremiumBubble)
        }, BASE_BUBBLE_ANIMATION_DELAY)
        if (hasActiveCampaign(data)) {
            animateDiscountBubble(BASE_BUBBLE_ANIMATION_DELAY)
        }
        animationHandler.postDelayed({
            performBubbleAnimation(amountInsuredBubble)
            performBubbleAnimation(startDateBubble)
        }, BASE_BUBBLE_ANIMATION_DELAY + 100)
        animationHandler.postDelayed({
            performBubbleAnimation(bindingPeriodBubble)
        }, BASE_BUBBLE_ANIMATION_DELAY + 150)
        animationHandler.postDelayed({
            performBubbleAnimation(brfOrTravelBubble)
            performBubbleAnimation(deductibleBubble) {
                lastAnimationHasCompleted = true
            }
        }, BASE_BUBBLE_ANIMATION_DELAY + 200)
        animationHandler.postDelayed({
            signButton
                .spring(
                    DynamicAnimation.TRANSLATION_Y,
                    damping = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
                )
                .animateToFinalPosition(0f)
        }, BASE_BUBBLE_ANIMATION_DELAY + 300)
    }

    private fun animateDiscountBubble(withDelay: Long = 0) {
        val action = {
            performBubbleAnimation(discountBubble)
        }
        if (withDelay > 0) {
            animationHandler.postDelayed({ action() }, withDelay)
        } else {
            action()
        }
    }

    private fun bindHomeSection(data: OfferQuery.Data) {
        homeSection.title.text = data.insurance.address
        data.insurance.arrangedPerilCategories.home?.fragments?.perilCategoryFragment?.let {
            addPerils(
                homeSection.perilsContainer,
                it
            )
        }
    }

    private fun bindStuffSection(data: OfferQuery.Data) {
        data.insurance.type?.let { insuranceType ->
            stuffSection.paragraph.text = interpolateTextKey(
                getString(R.string.OFFER_STUFF_PROTECTION_DESCRIPTION),
                "protectionAmount" to if (insuranceType.isStudentInsurance) {
                    getString(R.string.STUFF_PROTECTION_AMOUNT_STUDENT)
                } else {
                    getString(R.string.STUFF_PROTECTION_AMOUNT)
                }
            )
        }
        data.insurance.arrangedPerilCategories.stuff?.fragments?.perilCategoryFragment?.let {
            addPerils(
                stuffSection.perilsContainer,
                it
            )
        }
    }

    private fun bindMeSection(data: OfferQuery.Data) {
        data.insurance.arrangedPerilCategories.me?.fragments?.perilCategoryFragment?.let {
            addPerils(
                meSection.perilsContainer,
                it
            )
        }
    }

    private fun bindSwitchSection(data: OfferQuery.Data) {
        data.insurance.previousInsurer?.let { previousInsurer ->
            switchSection.show()

            if (previousInsurer.isSwitchable) {
                switchTitle.text = interpolateTextKey(
                    getString(R.string.OFFER_SWITCH_TITLE_APP),
                    "INSURER" to previousInsurer.displayName
                )
                switchParagraphTwo.text = getString(R.string.OFFER_SWITCH_COL_PARAGRAPH_ONE_APP)
            } else {
                switchTitle.text = getString(R.string.OFFER_SWITCH_TITLE_NON_SWITCHABLE_APP)
                switchParagraphTwo.text = getString(R.string.OFFER_NON_SWITCHABLE_PARAGRAPH_ONE_APP)
            }
        } ?: run {
            switchSection.remove()
        }
    }

    private fun addPerils(container: LinearLayout, category: PerilCategoryFragment) {
        container.removeAllViews()
        category.perils?.let { perils ->
            val maxPerilsPerRow = rowWidth / perilTotalWidth
            if (perils.size < maxPerilsPerRow) {
                container.orientation = LinearLayout.HORIZONTAL
                perils.forEach { peril ->
                    container.addView(makePeril(peril, category))
                }
            } else {
                container.orientation = LinearLayout.VERTICAL
                for (row in 0 until perils.size step maxPerilsPerRow) {
                    val rowView = LinearLayout(this)
                    val rowPerils = perils.subList(row, min(row + maxPerilsPerRow, perils.size))
                    rowPerils.forEach { peril ->
                        rowView.addView(makePeril(peril, category))
                    }
                    container.addView(rowView)
                }
            }
        }
    }

    override fun onPause() {
        animationHandler.removeCallbacksAndMessages(null)
        super.onPause()
    }

    private fun makePeril(peril: PerilCategoryFragment.Peril, category: PerilCategoryFragment) =
        PerilView.build(
            this,
            width = perilWidth,
            name = peril.title,
            iconId = peril.id,
            onClick = {
                safeLet(
                    category.title,
                    peril.id,
                    peril.title,
                    peril.description
                ) { name, id, title, description ->
                    PerilBottomSheet.newInstance(name, PerilIcon.from(id), title, description)
                        .show(supportFragmentManager, PerilBottomSheet.TAG)
                }
            }
        )

    companion object {
        private const val BASE_BUBBLE_ANIMATION_DELAY = 650L

        private val PRIVACY_POLICY_URL =
            Uri.parse("https://s3.eu-central-1.amazonaws.com/com-hedvig-web-content/Hedvig+-+integritetspolicy.pdf")

        private fun hasActiveCampaign(data: OfferQuery.Data) = data.redeemedCampaigns.size > 0

        private fun performBubbleAnimation(view: View, endAction: (() -> Unit)? = null) {
            view
                .spring(SpringAnimation.SCALE_X, stiffness = 1200f)
                .animateToFinalPosition(1f)
            val handle = view
                .spring(SpringAnimation.SCALE_Y, stiffness = 1200f)


            if (endAction != null) {
                handle.addEndListener { _, _, _, _ ->
                    endAction()
                }
            }
            handle.animateToFinalPosition(1f)
        }
    }
}
