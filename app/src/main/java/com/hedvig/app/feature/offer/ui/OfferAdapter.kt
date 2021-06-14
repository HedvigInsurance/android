package com.hedvig.app.feature.offer.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.app.BASE_MARGIN_DOUBLE
import com.hedvig.app.BASE_MARGIN_HALF
import com.hedvig.app.R
import com.hedvig.app.databinding.AdditionalBuildingsRowBinding
import com.hedvig.app.databinding.OfferFactAreaBinding
import com.hedvig.app.databinding.OfferHeaderBinding
import com.hedvig.app.databinding.OfferPerilAreaBinding
import com.hedvig.app.databinding.OfferSwitchBinding
import com.hedvig.app.databinding.OfferTermsAreaBinding
import com.hedvig.app.feature.offer.OfferRedeemCodeDialog
import com.hedvig.app.feature.offer.OfferSignDialog
import com.hedvig.app.feature.offer.OfferTracker
import com.hedvig.app.feature.offer.TermsAdapter
import com.hedvig.app.feature.offer.ui.changestartdate.ChangeDateBottomSheet
import com.hedvig.app.feature.offer.ui.changestartdate.ChangeDateBottomSheetData
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.ui.decoration.GridSpacingItemDecoration
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.apollo.format
import com.hedvig.app.util.apollo.getCurrencyString
import com.hedvig.app.util.apollo.toMonetaryAmount
import com.hedvig.app.util.extensions.getStringId
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.setStrikethrough
import com.hedvig.app.util.extensions.showAlert
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.viewBinding
import com.hedvig.app.util.svg.buildRequestBuilder
import e
import java.time.LocalDate

class OfferAdapter(
    private val fragmentManager: FragmentManager,
    private val tracker: OfferTracker,
    private val marketManager: MarketManager,
    private val removeDiscount: () -> Unit
) : ListAdapter<OfferModel, OfferAdapter.ViewHolder>(GenericDiffUtilItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.offer_header -> ViewHolder.Header(parent)
        R.layout.offer_info_area -> ViewHolder.Info(parent)
        R.layout.offer_fact_area -> ViewHolder.Facts(parent)
        R.layout.offer_peril_area -> ViewHolder.Perils(parent)
        R.layout.offer_terms_area -> ViewHolder.Terms(parent)
        R.layout.offer_switch -> ViewHolder.Switch(parent)
        else -> throw Error("Invalid viewType: $viewType")
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is OfferModel.Header -> R.layout.offer_header
        OfferModel.Info -> R.layout.offer_info_area
        is OfferModel.Facts -> R.layout.offer_fact_area
        is OfferModel.Perils -> R.layout.offer_peril_area
        is OfferModel.Terms -> R.layout.offer_terms_area
        is OfferModel.Switcher -> R.layout.offer_switch
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), fragmentManager, tracker, removeDiscount, marketManager)
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(
            data: OfferModel,
            fragmentManager: FragmentManager,
            tracker: OfferTracker,
            removeDiscount: () -> Unit,
            marketManager: MarketManager
        )

        class Header(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.offer_header)) {
            private val binding by viewBinding(OfferHeaderBinding::bind)

            override fun bind(
                data: OfferModel,
                fragmentManager: FragmentManager,
                tracker: OfferTracker,
                removeDiscount: () -> Unit,
                marketManager: MarketManager
            ) {
                if (data is OfferModel.Header) {
                    binding.apply {
                        data.inner.lastQuoteOfMember.asCompleteQuote?.let { quote ->
                            title.setText(quote.typeOfContract.getStringId())
                            val monetaryAmount = quote
                                .insuranceCost
                                .fragments
                                .costFragment
                                .monthlyNet
                                .fragments
                                .monetaryAmountFragment
                                .toMonetaryAmount()

                            premium.text = monetaryAmount.number.toString()
                            val currencyString = monetaryAmount.getCurrencyString(premium.context, marketManager.market)
                            premiumPeriod.text = premiumPeriod.context.getString(R.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION, currencyString)

                            val gross = quote
                                .insuranceCost
                                .fragments
                                .costFragment
                                .monthlyGross
                                .fragments
                                .monetaryAmountFragment
                                .toMonetaryAmount()

                            if (gross.isZero) {
                                grossPremium.setStrikethrough(true)
                                grossPremium.text = gross.format(grossPremium.context, marketManager.market)
                            }

                            startDateContainer.setHapticClickListener {
                                tracker.chooseStartDate()
                                ChangeDateBottomSheet.newInstance(
                                    ChangeDateBottomSheetData(
                                        quote.id,
                                        quote.currentInsurer?.switchable == true
                                    )
                                )
                                    .show(
                                        fragmentManager,
                                        ChangeDateBottomSheet.TAG
                                    )
                            }

                            val sd = quote.startDate

                            if (sd != null) {
                                if (sd == LocalDate.now()) {
                                    startDate.setText(R.string.START_DATE_TODAY)
                                } else {
                                    startDate.text = sd.toString()
                                }
                            } else {
                                if (quote.currentInsurer?.switchable == true) {
                                    startDate.setText(R.string.ACTIVATE_INSURANCE_END_BTN)
                                } else {
                                    startDate.setText(R.string.START_DATE_TODAY)
                                }
                            }

                            data
                                .inner
                                .redeemedCampaigns
                                .firstOrNull()
                                ?.fragments
                                ?.incentiveFragment
                                ?.incentive
                                ?.let { incentive ->
                                    discountButton.setText(R.string.OFFER_REMOVE_DISCOUNT_BUTTON)

                                    incentive.asFreeMonths?.let { freeMonths ->
                                        campaign.text = campaign.resources.getString(
                                            R.string.OFFER_SCREEN_FREE_MONTHS_DESCRIPTION,
                                            freeMonths.quantity
                                        )
                                        campaign.show()
                                        premiumContainer.setBackgroundResource(
                                            R.drawable.background_premium_box_with_campaign
                                        )
                                    }

                                    incentive.asMonthlyCostDeduction?.let {
                                        campaign.setText(R.string.OFFER_SCREEN_INVITED_BUBBLE)
                                        campaign.show()
                                        premiumContainer.setBackgroundResource(
                                            R.drawable.background_premium_box_with_campaign
                                        )
                                    }

                                    incentive.asPercentageDiscountMonths?.let { pdm ->
                                        campaign.text = if (pdm.pdmQuantity == 1) {
                                            campaign.resources.getString(
                                                R.string.OFFER_SCREEN_PERCENTAGE_DISCOUNT_BUBBLE_TITLE_SINGULAR,
                                                pdm.percentageDiscount.toInt()
                                            )
                                        } else {
                                            campaign.resources.getString(
                                                R.string.OFFER_SCREEN_PERCENTAGE_DISCOUNT_BUBBLE_TITLE_PLURAL,
                                                pdm.percentageDiscount.toInt(),
                                                pdm.pdmQuantity
                                            )
                                        }
                                        campaign.show()
                                        premiumContainer.setBackgroundResource(
                                            R.drawable.background_premium_box_with_campaign
                                        )
                                    }

                                    discountButton.setHapticClickListener {
                                        tracker.removeDiscount()
                                        discountButton.context.showAlert(
                                            R.string.OFFER_REMOVE_DISCOUNT_ALERT_TITLE,
                                            R.string.OFFER_REMOVE_DISCOUNT_ALERT_DESCRIPTION,
                                            R.string.OFFER_REMOVE_DISCOUNT_ALERT_REMOVE,
                                            R.string.OFFER_REMOVE_DISCOUNT_ALERT_CANCEL,
                                            {
                                                removeDiscount()
                                            }
                                        )
                                    }

                                    // Remove campaign views if campaign type is unknown
                                    if (
                                        incentive.asFreeMonths == null &&
                                        incentive.asMonthlyCostDeduction == null &&
                                        incentive.asNoDiscount == null &&
                                        incentive.asPercentageDiscountMonths == null
                                    ) {
                                        premiumContainer.background = null
                                        campaign.remove()
                                    }
                                } ?: run {
                                discountButton.setText(R.string.OFFER_ADD_DISCOUNT_BUTTON)
                                premiumContainer.background = null
                                campaign.remove()
                                discountButton.setHapticClickListener {
                                    tracker.addDiscount()
                                    OfferRedeemCodeDialog.newInstance()
                                        .show(
                                            fragmentManager,
                                            OfferRedeemCodeDialog.TAG
                                        )
                                }
                            }
                        }

                        sign.setHapticClickListener {
                            tracker.floatingSign()
                            OfferSignDialog.newInstance().show(
                                fragmentManager,
                                OfferSignDialog.TAG
                            )
                        }
                        return
                    }
                }
            }
        }

        class Info(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.offer_info_area)) {
            override fun bind(
                data: OfferModel,
                fragmentManager: FragmentManager,
                tracker: OfferTracker,
                removeDiscount: () -> Unit,
                marketManager: MarketManager
            ) = Unit
        }

        class Facts(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.offer_fact_area)) {
            private val binding by viewBinding(OfferFactAreaBinding::bind)

            init {
                binding.expandableContentView.initialize()
            }

            override fun bind(
                data: OfferModel,
                fragmentManager: FragmentManager,
                tracker: OfferTracker,
                removeDiscount: () -> Unit,
                marketManager: MarketManager
            ) {
                if (data is OfferModel.Facts) {
                    binding.apply {
                        data
                            .inner
                            .lastQuoteOfMember
                            .asCompleteQuote
                            ?.quoteDetails
                            ?.asSwedishApartmentQuoteDetails
                            ?.let { swedishApartmentQuote ->
                                ancillarySpaceLabel.remove()
                                ancillarySpace.remove()
                                yearOfConstructionLabel.remove()
                                yearOfConstruction.remove()
                                bathroomsLabel.remove()
                                bathrooms.remove()
                                subletedLabel.remove()
                                subleted.remove()
                                additionalBuildingsTitle.remove()
                                additionalBuildingsContainer.remove()
                                additionalBuildingsSeparator.remove()

                                bindCommon(
                                    swedishApartmentQuote.livingSpace,
                                    swedishApartmentQuote.householdSize
                                )

                                expandableContentView.contentSizeChanged()
                            }

                        data
                            .inner
                            .lastQuoteOfMember
                            .asCompleteQuote
                            ?.quoteDetails
                            ?.asSwedishHouseQuoteDetails
                            ?.let { swedishHouseQuote ->
                                bindCommon(
                                    swedishHouseQuote.livingSpace,
                                    swedishHouseQuote.householdSize
                                )
                                ancillarySpaceLabel.show()
                                ancillarySpace.show()
                                ancillarySpace.text = ancillarySpace.resources.getString(
                                    R.string.HOUSE_INFO_BIYTA_SQUAREMETERS,
                                    swedishHouseQuote.ancillarySpace
                                )

                                yearOfConstructionLabel.show()
                                yearOfConstruction.show()
                                yearOfConstruction.text =
                                    swedishHouseQuote.yearOfConstruction.toString()

                                bathroomsLabel.show()
                                bathrooms.show()
                                bathrooms.text = swedishHouseQuote.numberOfBathrooms.toString()

                                subletedLabel.show()
                                subleted.show()
                                subleted.text = if (swedishHouseQuote.isSubleted) {
                                    subleted.resources.getString(R.string.HOUSE_INFO_SUBLETED_TRUE)
                                } else {
                                    subleted.resources.getString(R.string.HOUSE_INFO_SUBLETED_FALSE)
                                }

                                swedishHouseQuote.extraBuildings.let { extraBuildings ->
                                    if (extraBuildings.isEmpty()) {
                                        additionalBuildingsContainer.remove()
                                        additionalBuildingsTitle.remove()
                                        additionalBuildingsSeparator.remove()
                                    } else {
                                        additionalBuildingsTitle.show()
                                        additionalBuildingsContainer.show()
                                        bindExtraBuildings(extraBuildings)
                                    }
                                }

                                expandableContentView.contentSizeChanged()
                            }
                        return
                    }
                }

                e { "Invariant detected: ${data.javaClass.name} passed to ${this.javaClass.name}::bind" }
            }

            private fun bindCommon(dataLivingSpace: Int, personsInHousehold: Int) {
                binding.apply {
                    livingSpace.text =
                        livingSpace.resources.getString(
                            R.string.HOUSE_INFO_BOYTA_SQUAREMETERS,
                            dataLivingSpace
                        )
                    coinsured.text = personsInHousehold.toString()
                    offerExpirationDate.text = offerExpirationDate.resources.getString(
                        R.string.OFFER_INFO_OFFER_EXPIRES,
                        LocalDate.now().plusMonths(1).toString()
                    )
                }
            }

            private fun bindExtraBuildings(extraBuildings: List<OfferQuery.ExtraBuilding>) {
                binding.apply {
                    additionalBuildingsTitle.show()
                    additionalBuildingsSeparator.show()

                    extraBuildings.forEach { eb ->
                        val extraBuilding = eb.asExtraBuildingCore ?: return@forEach
                        val binding =
                            AdditionalBuildingsRowBinding.inflate(
                                LayoutInflater.from(additionalBuildingsContainer.context),
                                additionalBuildingsContainer,
                                false
                            )
                        binding.title.text = extraBuilding.displayName

                        var bodyText =
                            binding.root.resources.getString(
                                R.string.HOUSE_INFO_BOYTA_SQUAREMETERS,
                                extraBuilding.area
                            )
                        if (extraBuilding.hasWaterConnected) {
                            bodyText += ", " + binding.root.resources.getString(R.string.HOUSE_INFO_CONNECTED_WATER)
                        }
                        binding.body.text = bodyText
                        additionalBuildingsContainer.addView(binding.root)
                    }
                    additionalBuildingsContainer.show()
                }
            }
        }

        class Perils(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.offer_peril_area)) {
            private val binding by viewBinding(OfferPerilAreaBinding::bind)

            init {
                binding.perils.addItemDecoration(GridSpacingItemDecoration(BASE_MARGIN_HALF))
            }

            override fun bind(
                data: OfferModel,
                fragmentManager: FragmentManager,
                tracker: OfferTracker,
                removeDiscount: () -> Unit,
                marketManager: MarketManager
            ) {
                binding.apply {
                    if (perils.adapter == null) {
                        perils.adapter =
                            PerilsAdapter(fragmentManager, perils.context.buildRequestBuilder())
                    }

                    if (data is OfferModel.Perils) {
                        val items = data.inner.lastQuoteOfMember.asCompleteQuote?.perils.orEmpty()
                            .map { it.fragments.perilFragment }
                        (perils.adapter as? PerilsAdapter)?.submitList(items)

                        when (data.inner.lastQuoteOfMember.asCompleteQuote?.typeOfContract) {
                            TypeOfContract.SE_HOUSE -> {
                                perilInfo.setText(R.string.OFFER_SCREEN_COVERAGE_BODY_HOUSE)
                            }
                            TypeOfContract.SE_APARTMENT_BRF,
                            TypeOfContract.SE_APARTMENT_STUDENT_BRF,
                            TypeOfContract.NO_HOME_CONTENT_OWN,
                            TypeOfContract.NO_HOME_CONTENT_YOUTH_OWN,
                            -> {
                                perilInfo.setText(R.string.OFFER_SCREEN_COVERAGE_BODY_BRF)
                            }
                            TypeOfContract.NO_HOME_CONTENT_RENT,
                            TypeOfContract.NO_HOME_CONTENT_YOUTH_RENT,
                            TypeOfContract.SE_APARTMENT_RENT,
                            TypeOfContract.SE_APARTMENT_STUDENT_RENT,
                            -> {
                                perilInfo.setText(R.string.OFFER_SCREEN_COVERAGE_BODY_RENTAL)
                            }
                            else -> {
                            }
                        }
                        return
                    }
                }

                e { "Invariant detected: ${data.javaClass.name} passed to ${this.javaClass.name}::bind" }
            }
        }

        class Terms(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.offer_terms_area)) {
            private val binding by viewBinding(OfferTermsAreaBinding::bind)

            init {
                binding.apply {
                    insurableLimits.adapter = InsurableLimitsAdapter()
                    insurableLimits.addItemDecoration(GridSpacingItemDecoration(BASE_MARGIN_DOUBLE))
                }
            }

            override fun bind(
                data: OfferModel,
                fragmentManager: FragmentManager,
                tracker: OfferTracker,
                removeDiscount: () -> Unit,
                marketManager: MarketManager
            ) {
                binding.apply {
                    if (termsDocuments.adapter == null) {
                        termsDocuments.adapter = TermsAdapter(tracker, marketManager)
                    }
                    if (data is OfferModel.Terms) {
                        data
                            .inner
                            .lastQuoteOfMember
                            .asCompleteQuote
                            ?.insurableLimits
                            ?.map { it.fragments.insurableLimitsFragment }
                            ?.let {
                                (insurableLimits.adapter as? InsurableLimitsAdapter)?.submitList(
                                    it
                                )
                            }
                        data.inner.lastQuoteOfMember.asCompleteQuote?.insuranceTerms?.let {
                            (termsDocuments.adapter as? TermsAdapter)?.submitList(it)
                        }
                        return
                    }
                }

                e { "Invariant detected: ${data.javaClass.name} passed to ${this.javaClass.name}::bind" }
            }
        }

        class Switch(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.offer_switch)) {
            private val binding by viewBinding(OfferSwitchBinding::bind)
            override fun bind(
                data: OfferModel,
                fragmentManager: FragmentManager,
                tracker: OfferTracker,
                removeDiscount: () -> Unit,
                marketManager: MarketManager
            ) {
                if (data is OfferModel.Switcher) {
                    val insurer = data.displayName
                        ?: binding.switchTitle.resources.getString(R.string.OTHER_INSURER_OPTION_APP)
                    binding.switchTitle.text = binding.switchTitle.resources.getString(
                        R.string.OFFER_SWITCH_TITLE_APP,
                        insurer
                    )
                    return
                }

                e { "Invariant detected: ${data.javaClass.name} passed to ${this.javaClass.name}::bind" }
            }
        }
    }
}

sealed class OfferModel {
    data class Header(
        val inner: OfferQuery.Data,
    ) : OfferModel()

    object Info : OfferModel()

    data class Facts(
        val inner: OfferQuery.Data,
    ) : OfferModel()

    data class Perils(
        val inner: OfferQuery.Data,
    ) : OfferModel()

    data class Terms(
        val inner: OfferQuery.Data,
    ) : OfferModel()

    data class Switcher(
        val displayName: String?,
    ) : OfferModel()
}
