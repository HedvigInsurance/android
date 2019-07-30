package com.hedvig.app.feature.offer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
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
import com.hedvig.app.util.boundedLerp
import com.hedvig.app.util.extensions.compatColor
import com.hedvig.app.util.extensions.displayMetrics
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.setStrikethrough
import com.hedvig.app.util.extensions.showAlert
import com.hedvig.app.util.extensions.startClosableChat
import com.hedvig.app.util.extensions.view.fadeIn
import com.hedvig.app.util.extensions.view.fadeOut
import com.hedvig.app.util.extensions.view.hide
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.view.spring
import com.hedvig.app.util.extensions.view.updateMargin
import com.hedvig.app.util.interpolateTextKey
import com.hedvig.app.util.isStudentInsurance
import com.hedvig.app.util.isApartmentOwner
import com.hedvig.app.util.safeLet
import kotlinx.android.synthetic.main.activity_offer.*
import kotlinx.android.synthetic.main.feature_bubbles.*
import kotlinx.android.synthetic.main.loading_spinner.*
import kotlinx.android.synthetic.main.offer_peril_section.view.*
import kotlinx.android.synthetic.main.offer_section_switch.*
import kotlinx.android.synthetic.main.offer_section_terms.view.*
import kotlinx.android.synthetic.main.price_bubbles.*
import org.koin.android.viewmodel.ext.android.viewModel
import kotlin.math.min

class OfferActivity : BaseActivity() {

    private val offerViewModel: OfferViewModel by viewModel()

    private val doubleMargin: Int by lazy { resources.getDimensionPixelSize(R.dimen.base_margin_double) }
    private val perilTotalWidth: Int by lazy { resources.getDimensionPixelSize(R.dimen.peril_width) + (doubleMargin * 2) }
    private val rowWidth: Int by lazy {
        displayMetrics.widthPixels - (doubleMargin * 2)
    }
    private val halfScreenHeight by lazy {
        displayMetrics.heightPixels / 2
    }
    private val signButtonOffScreenTranslation by lazy {
        resources.getDimension(R.dimen.offer_sign_button_off_screen_translation)
    }

    private var isShowingToolbarSign = true
    private var isShowingFloatingSign = false

    private val animationHandler = Handler()
    private var hasTriggeredAnimations = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_offer)

        offerChatButton.setHapticClickListener {
            offerViewModel.triggerOpenChat {
                startClosableChat(true)
            }
        }

        bindStaticData()

        offerViewModel.data.observe(lifecycleOwner = this) { data ->
            data?.let { d ->
                loadingSpinner.remove()
                container.show()
                bindToolbar(d)
                bindPriceBubbles(d)
                bindFeatureBubbles(d)
                bindDiscountButton(d)
                bindHomeSection(d)
                bindStuffSection(d)
                bindMeSection(d)
                bindTerms(d)
                bindSwitchSection(d)
                animateBubbles(d)
            }
        }
    }

    private fun bindToolbar(data: OfferQuery.Data) {
        offerToolbarAddress.text = data.insurance.address
    }

    private fun bindStaticData() {
        setSupportActionBar(offerToolbar)

        val deductibleText =
            "${getString(R.string.OFFER_BUBBLES_DEDUCTIBLE_TITLE)}\n${getString(R.string.OFFER_BUBBLES_DEDUCTIBLE_SUBTITLE)}"
        deductibleBubbleText.text = deductibleText

        val bindingPeriodText =
            "${getString(R.string.OFFER_BUBBLES_BINDING_PERIOD_TITLE)}\n${getString(R.string.OFFER_BUBBLES_BINDING_PERIOD_SUBTITLE)}"
        bindingPeriodBubbleText.text = bindingPeriodText


        homeSection.paragraph.text = getString(R.string.OFFER_APARTMENT_PROTECTION_DESCRIPTION)
        homeSection.hero.setImageDrawable(getDrawable(R.drawable.offer_house))

        stuffSection.hero.setImageDrawable(getDrawable(R.drawable.offer_stuff))
        stuffSection.title.text = getString(R.string.OFFER_STUFF_PROTECTION_TITLE)

        meSection.hero.setImageDrawable(getDrawable(R.drawable.offer_me))
        meSection.title.text = getString(R.string.OFFER_PERSONAL_PROTECTION_TITLE)
        meSection.paragraph.text = getString(R.string.OFFER_PERSONAL_PROTECTION_DESCRIPTION)

        termsSection.privacyPolicy.setHapticClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, PRIVACY_POLICY_URL))
        }

        grossPremium.setStrikethrough(true)

        setupButtons()
        setupScrollListeners()
    }

    private fun setupButtons() {
        signButton.setHapticClickListener {
            OfferSignDialog.newInstance().show(supportFragmentManager, OfferSignDialog.TAG)
        }
        offerToolbarSign.setHapticClickListener {
            OfferSignDialog.newInstance().show(supportFragmentManager, OfferSignDialog.TAG)
        }
    }

    private fun setupScrollListeners() {
        offerScroll.setOnScrollChangeListener { _: NestedScrollView, _, scrollY, _, _ ->
            if (scrollY > halfScreenHeight) {
                if (isShowingToolbarSign) {
                    isShowingToolbarSign = false
                    offerToolbarSign.fadeOut()
                }
                if (!isShowingFloatingSign) {
                    isShowingFloatingSign = true
                    signButton
                        .spring(
                            DynamicAnimation.TRANSLATION_Y,
                            damping = SpringForce.DAMPING_RATIO_LOW_BOUNCY
                        )
                        .animateToFinalPosition(0f)
                }
            } else {
                if (!isShowingToolbarSign) {
                    isShowingToolbarSign = true
                    offerToolbarSign.fadeIn()
                }
                if (isShowingFloatingSign) {
                    isShowingFloatingSign = false
                    signButton
                        .spring(
                            DynamicAnimation.TRANSLATION_Y,
                            damping = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
                        )
                        .animateToFinalPosition(signButtonOffScreenTranslation)
                }
            }

            parallaxContainer.translationY = scrollY / 1.25f
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
                OfferRedeemCodeDialog.newInstance().show(supportFragmentManager, OfferRedeemCodeDialog.TAG)
            }
        }
    }

    private fun bindPriceBubbles(data: OfferQuery.Data) {
        grossPremium.hide()
        discountBubble.hide()
        discountTitle.hide()

        netPremium.setTextColor(compatColor(R.color.off_black_dark))
        netPremium.text =
            data.insurance.cost?.fragments?.costFragment?.monthlyNet?.amount?.toBigDecimal()?.toInt()?.toString()

        if (data.redeemedCampaigns.size > 0) {
            when (data.redeemedCampaigns[0].fragments.incentiveFragment.incentive) {
                is IncentiveFragment.AsMonthlyCostDeduction -> {
                    grossPremium.show()
                    grossPremium.text = interpolateTextKey(
                        getString(R.string.OFFER_GROSS_PREMIUM),
                        "GROSS_PREMIUM" to data.insurance.cost?.fragments?.costFragment?.monthlyGross?.amount?.toBigDecimal()?.toInt()
                    )

                    discountBubble.show()
                    discount.text = getString(R.string.OFFER_SCREEN_INVITED_BUBBLE)
                    discount.updateMargin(top = 0)

                    netPremium.setTextColor(compatColor(R.color.pink))
                }
                is IncentiveFragment.AsFreeMonths -> {
                    discountTitle.show()
                    discount.text = interpolateTextKey(
                        getString(R.string.OFFER_SCREEN_FREE_MONTHS_BUBBLE),
                        "free_month" to (data.redeemedCampaigns[0].fragments.incentiveFragment.incentive as IncentiveFragment.AsFreeMonths).quantity
                    )
                    discount.updateMargin(top = resources.getDimensionPixelSize(R.dimen.base_margin_half))
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
            performBubbleAnimation(deductibleBubble)
        }, BASE_BUBBLE_ANIMATION_DELAY + 200)
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

    private fun bindFeatureBubbles(data: OfferQuery.Data) {
        val amountInsuredInterpolated = interpolateTextKey(
            getString(R.string.OFFER_BUBBLES_INSURED_SUBTITLE),
            "personsInHousehold" to data.insurance.personsInHousehold
        )
        val amountInsuredText = "${getString(R.string.OFFER_BUBBLES_INSURED_TITLE)}\n$amountInsuredInterpolated"
        amountInsuredBubbleText.text = amountInsuredText

        if (data.insurance.insuredAtOtherCompany == true) {
            val startDateText =
                "${getString(R.string.OFFER_BUBBLES_START_DATE_TITLE)}\n${getString(R.string.OFFER_BUBBLES_START_DATE_SUBTITLE_SWITCHER)}"
            startDateBubbleText.text = startDateText
        } else {
            val startDateText =
                "${getString(R.string.OFFER_BUBBLES_START_DATE_TITLE)}\n${getString(R.string.OFFER_BUBBLES_START_DATE_SUBTITLE_NEW)}"
            startDateBubbleText.text = startDateText
        }

        data.insurance.type?.let { t ->
            brfOrTravel.text = if (isApartmentOwner(t)) {
                getString(R.string.OFFER_BUBBLES_OWNED_ADDON_TITLE)
            } else {
                getString(R.string.OFFER_BUBBLES_TRAVEL_PROTECTION_TITLE)
            }
        }
    }

    private fun bindHomeSection(data: OfferQuery.Data) {
        homeSection.title.text = data.insurance.address
        data.insurance.perilCategories?.getOrNull(0)?.let { perils ->
            addPerils(homeSection.perilsContainer, perils.fragments.perilCategoryFragment)
        }
    }

    private fun bindStuffSection(data: OfferQuery.Data) {
        data.insurance.type?.let { insuranceType ->
            stuffSection.paragraph.text = interpolateTextKey(
                getString(R.string.OFFER_STUFF_PROTECTION_DESCRIPTION),
                "protectionAmount" to if (isStudentInsurance(insuranceType)) {
                    getString(R.string.STUFF_PROTECTION_AMOUNT_STUDENT)
                } else {
                    getString(R.string.STUFF_PROTECTION_AMOUNT)
                }
            )
        }
        data.insurance.perilCategories?.getOrNull(1)?.let { perils ->
            addPerils(stuffSection.perilsContainer, perils.fragments.perilCategoryFragment)
        }
    }

    private fun bindMeSection(data: OfferQuery.Data) {
        data.insurance.perilCategories?.getOrNull(2)?.let { perils ->
            addPerils(meSection.perilsContainer, perils.fragments.perilCategoryFragment)
        }
    }

    private fun bindTerms(data: OfferQuery.Data) {
        data.insurance.type?.let { insuranceType ->
            termsSection.maxCompensation.text = interpolateTextKey(
                getString(R.string.OFFER_TERMS_MAX_COMPENSATION),
                "MAX_COMPENSATION" to if (isStudentInsurance(insuranceType)) {
                    "200 000 kr"
                } else {
                    "1 000 000 kr"
                }
            )
            termsSection.deductible.text = interpolateTextKey(
                getString(R.string.OFFER_TERMS_DEDUCTIBLE),
                "DEDUCTIBLE" to "1 500 kr"
            )
        }
        data.insurance.presaleInformationUrl?.let { piu ->
            termsSection.presaleInformation.setHapticClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(piu)))
            }
        }
        data.insurance.policyUrl?.let { pu ->
            termsSection.terms.setHapticClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(pu)))
            }
        }
    }

    private fun bindSwitchSection(data: OfferQuery.Data) {
        if (data.insurance.insuredAtOtherCompany == true) {
            switchSection.show()

            val insurerDisplayName = when (data.insurance.currentInsurerName) {
                "LANSFORSAKRINGAR" -> {
                    "Länsförsäkringar"
                }
                "IF" -> {
                    "If"
                }
                "FOLKSAM" -> {
                    "Folksam"
                }
                "TRYGG_HANSA" -> {
                    "Trygg-Hansa"
                }
                "MODERNA" -> {
                    getString(R.string.MODERNA_FORSAKRING_APP)
                }
                "ICA" -> {
                    getString(R.string.ICA_FORSAKRING_APP)
                }
                "GJENSIDIGE" -> {
                    "Gjensidige"
                }
                "VARDIA" -> {
                    "Vardia"
                }
                "TRE_KRONOR" -> {
                    "Tre Kronor"
                }
                "OTHER" -> {
                    getString(R.string.OTHER_INSURER_OPTION_APP)
                }
                else -> {
                    getString(R.string.OTHER_INSURER_OPTION_APP)
                }
            }
            if (isSwitchableInsurer(data.insurance.currentInsurerName)) {
                switchTitle.text = interpolateTextKey(
                    getString(R.string.OFFER_SWITCH_TITLE_APP),
                    "INSURER" to insurerDisplayName
                )
                switchParagraphTwo.text = getString(R.string.OFFER_SWITCH_COL_PARAGRAPH_ONE_APP)
            } else {
                switchTitle.text = getString(R.string.OFFER_SWITCH_TITLE_NON_SWITCHABLE_APP)
                switchParagraphTwo.text = getString(R.string.OFFER_NON_SWITCHABLE_PARAGRAPH_ONE_APP)
            }
        } else {
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

    private fun makePeril(peril: PerilCategoryFragment.Peril, category: PerilCategoryFragment) = PerilView.build(
        this,
        name = peril.title,
        iconId = peril.id,
        onClick = {
            safeLet(category.title, peril.id, peril.title, peril.description) { name, id, title, description ->
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

        private fun performBubbleAnimation(view: View) {
            view
                .spring(SpringAnimation.SCALE_X, stiffness = 1200f)
                .animateToFinalPosition(1f)
            view
                .spring(SpringAnimation.SCALE_Y, stiffness = 1200f)
                .animateToFinalPosition(1f)
        }

        private fun isSwitchableInsurer(insurerName: String?) = when (insurerName) {
            "ICA", "FOLKSAM", "TRYGG_HANSA", "TRE_KRONOR" -> true
            else -> false
        }
    }
}
