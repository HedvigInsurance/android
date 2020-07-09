package com.hedvig.app.feature.offer.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.app.BASE_MARGIN_DOUBLE
import com.hedvig.app.BASE_MARGIN_HALF
import com.hedvig.app.R
import com.hedvig.app.feature.dashboard.ui.contractcoverage.InsurableLimitsAdapter
import com.hedvig.app.feature.dashboard.ui.contractcoverage.PerilsAdapter
import com.hedvig.app.feature.offer.ChangeDateBottomSheet
import com.hedvig.app.feature.offer.OfferRedeemCodeDialog
import com.hedvig.app.feature.offer.OfferSignDialog
import com.hedvig.app.feature.offer.OfferTracker
import com.hedvig.app.feature.offer.TermsAdapter
import com.hedvig.app.ui.decoration.GridSpacingItemDecoration
import com.hedvig.app.util.apollo.format
import com.hedvig.app.util.apollo.toMonetaryAmount
import com.hedvig.app.util.extensions.getStringId
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.setStrikethrough
import com.hedvig.app.util.extensions.showAlert
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.svg.buildRequestBuilder
import e
import kotlinx.android.synthetic.main.additional_buildings_row.view.*
import kotlinx.android.synthetic.main.offer_fact_area.view.*
import kotlinx.android.synthetic.main.offer_header.view.*
import kotlinx.android.synthetic.main.offer_header.view.title
import kotlinx.android.synthetic.main.offer_peril_area.view.*
import kotlinx.android.synthetic.main.offer_terms_area.view.*
import java.time.LocalDate

class OfferAdapter(
    private val fragmentManager: FragmentManager,
    private val tracker: OfferTracker,
    private val removeDiscount: () -> Unit
) : RecyclerView.Adapter<OfferAdapter.ViewHolder>() {
    var items: List<OfferModel> = emptyList()
        set(value) {
            val diff = DiffUtil.calculateDiff(
                OfferDiffCallback(
                    field, value
                )
            )

            field = value
            diff.dispatchUpdatesTo(this)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.offer_header -> ViewHolder.Header(parent)
        R.layout.offer_info_area -> ViewHolder.Info(parent)
        R.layout.offer_fact_area -> ViewHolder.Facts(parent)
        R.layout.offer_peril_area -> ViewHolder.Perils(parent)
        R.layout.offer_terms_area -> ViewHolder.Terms(parent)
        R.layout.offer_footer -> ViewHolder.Footer(parent)
        else -> throw Error("Invalid viewType: $viewType")
    }

    override fun getItemViewType(position: Int) = when (items[position]) {
        is OfferModel.Header -> R.layout.offer_header
        OfferModel.Info -> R.layout.offer_info_area
        is OfferModel.Facts -> R.layout.offer_fact_area
        is OfferModel.Perils -> R.layout.offer_peril_area
        is OfferModel.Terms -> R.layout.offer_terms_area
        OfferModel.Footer -> R.layout.offer_footer
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], fragmentManager, tracker, removeDiscount)
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(
            data: OfferModel,
            fragmentManager: FragmentManager,
            tracker: OfferTracker,
            removeDiscount: () -> Unit
        )

        class Header(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.offer_header)) {
            private val title = itemView.title
            private val premium = itemView.premium
            private val premiumContainer = itemView.premiumContainer
            private val grossPremium = itemView.grossPremium
            private val startDateContainer = itemView.startDateContainer
            private val startDate = itemView.startDate
            private val discountButton = itemView.discountButton
            private val campaign = itemView.campaign
            private val sign = itemView.sign

            override fun bind(
                data: OfferModel,
                fragmentManager: FragmentManager,
                tracker: OfferTracker,
                removeDiscount: () -> Unit
            ) {
                if (data is OfferModel.Header) {
                    data.inner.lastQuoteOfMember.asCompleteQuote?.let { quote ->
                        title.text = title.resources.getString(quote.typeOfContract.getStringId())
                        premium.text =
                            quote.insuranceCost.fragments.costFragment.monthlyNet.fragments.monetaryAmountFragment.toMonetaryAmount()
                                .format(premium.context)
                        val gross =
                            quote.insuranceCost.fragments.costFragment.monthlyGross.fragments.monetaryAmountFragment.toMonetaryAmount()
                        if (gross.isZero) {
                            grossPremium.setStrikethrough(true)
                            grossPremium.text = gross.format(grossPremium.context)
                        }

                        startDateContainer.setHapticClickListener {
                            tracker.chooseStartDate()
                            ChangeDateBottomSheet.newInstance()
                                .show(
                                    fragmentManager,
                                    ChangeDateBottomSheet.TAG
                                )
                        }

                        quote.startDate?.let { sd ->
                            startDate.text = if (sd != LocalDate.now()) {
                                sd.toString()
                            } else {
                                startDate.resources.getString(R.string.START_DATE_TODAY)
                            }
                        } ?: startDate.setText(R.string.START_DATE_TODAY)

                        data.inner.redeemedCampaigns.firstOrNull()?.fragments?.incentiveFragment?.incentive?.let { incentive ->
                            discountButton.setText(R.string.OFFER_REMOVE_DISCOUNT_BUTTON)

                            incentive.asFreeMonths?.let { freeMonths ->
                                campaign.text = campaign.resources.getString(
                                    R.string.OFFER_SCREEN_FREE_MONTHS_DESCRIPTION,
                                    freeMonths.quantity
                                )
                                campaign.show()
                                premiumContainer.setBackgroundResource(R.drawable.background_premium_box_with_campaign)
                            }

                            incentive.asMonthlyCostDeduction?.let {
                                campaign.setText(R.string.OFFER_SCREEN_INVITED_BUBBLE)
                                campaign.show()
                                premiumContainer.setBackgroundResource(R.drawable.background_premium_box_with_campaign)
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
                                        pdm.pdmQuantity,
                                        pdm.percentageDiscount.toInt()
                                    )
                                }
                                campaign.show()
                                premiumContainer.setBackgroundResource(R.drawable.background_premium_box_with_campaign)
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
                                    })
                            }

                            // Remove campaign views if campaign type is unknown
                            if (
                                incentive.asFreeMonths == null
                                && incentive.asMonthlyCostDeduction == null
                                && incentive.asNoDiscount == null
                                && incentive.asPercentageDiscountMonths == null
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

                e { "Invariant detected: ${data.javaClass.name} passed to ${this.javaClass.name}::bind" }
            }
        }

        class Info(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.offer_info_area)) {
            override fun bind(
                data: OfferModel,
                fragmentManager: FragmentManager,
                tracker: OfferTracker,
                removeDiscount: () -> Unit
            ) = Unit
        }

        class Facts(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.offer_fact_area)) {
            private val expandandableContentView = itemView.expandableContentView
            private val livingSpace = itemView.livingSpace
            private val coinsured = itemView.coinsured
            private val offerExpirationDate = itemView.offerExpirationDate
            private val ancillarySpaceLabel = itemView.ancillarySpaceLabel
            private val ancillarySpace = itemView.ancillarySpace
            private val yearOfConstructionLabel = itemView.yearOfConstructionLabel
            private val yearOfConstruction = itemView.yearOfConstruction
            private val bathroomsLabel = itemView.bathroomsLabel
            private val bathrooms = itemView.bathrooms
            private val subletedLabel = itemView.subletedLabel
            private val subleted = itemView.subleted
            private val additionalBuildingsTitle = itemView.additionalBuildingsTitle
            private val additionalBuildingsContainer = itemView.additionalBuildingsContainer
            private val additionalBuildingsSeparator = itemView.additionalBuildingsSeparator

            init {
                expandandableContentView.initialize()
            }

            override fun bind(
                data: OfferModel,
                fragmentManager: FragmentManager,
                tracker: OfferTracker,
                removeDiscount: () -> Unit
            ) {
                if (data is OfferModel.Facts) {
                    data.inner.lastQuoteOfMember.asCompleteQuote?.quoteDetails?.asSwedishApartmentQuoteDetails?.let { swedishApartmentQuote ->
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

                        expandandableContentView.contentSizeChanged()
                    }

                    data.inner.lastQuoteOfMember.asCompleteQuote?.quoteDetails?.asSwedishHouseQuoteDetails?.let { swedishHouseQuote ->
                        bindCommon(swedishHouseQuote.livingSpace, swedishHouseQuote.householdSize)
                        ancillarySpaceLabel.show()
                        ancillarySpace.show()
                        ancillarySpace.text = ancillarySpace.resources.getString(
                            R.string.HOUSE_INFO_BIYTA_SQUAREMETERS,
                            swedishHouseQuote.ancillarySpace
                        )

                        yearOfConstructionLabel.show()
                        yearOfConstruction.show()
                        yearOfConstruction.text = swedishHouseQuote.yearOfConstruction.toString()

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

                        expandandableContentView.contentSizeChanged()
                    }
                    return
                }

                e { "Invariant detected: ${data.javaClass.name} passed to ${this.javaClass.name}::bind" }
            }

            private fun bindCommon(dataLivingSpace: Int, personsInHousehold: Int) {
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

            private fun bindExtraBuildings(extraBuildings: List<OfferQuery.ExtraBuilding>) {
                additionalBuildingsTitle.show()
                additionalBuildingsSeparator.show()

                extraBuildings.forEach { eb ->
                    val extraBuilding = eb.asExtraBuildingCore ?: return@forEach
                    val row = LayoutInflater
                        .from(additionalBuildingsContainer.context)
                        .inflate(
                            R.layout.additional_buildings_row,
                            additionalBuildingsContainer,
                            false
                        )
                    row.title.text = extraBuilding.displayName

                    var bodyText =
                        row.resources.getString(
                            R.string.HOUSE_INFO_BOYTA_SQUAREMETERS,
                            extraBuilding.area
                        )
                    if (extraBuilding.hasWaterConnected) {
                        bodyText += ", " + row.resources.getString(R.string.HOUSE_INFO_CONNECTED_WATER)
                    }
                    row.body.text = bodyText
                    additionalBuildingsContainer.addView(row)
                }

                additionalBuildingsContainer.show()
            }
        }

        class Perils(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.offer_peril_area)) {
            private val perils = itemView.perils
            private val perilInfo = itemView.perilInfo

            init {
                perils.addItemDecoration(GridSpacingItemDecoration(BASE_MARGIN_HALF))
            }

            override fun bind(
                data: OfferModel,
                fragmentManager: FragmentManager,
                tracker: OfferTracker,
                removeDiscount: () -> Unit
            ) {
                if (perils.adapter == null) {
                    perils.adapter =
                        PerilsAdapter(fragmentManager, perils.context.buildRequestBuilder())
                }

                if (data is OfferModel.Perils) {
                    val items = data.inner.lastQuoteOfMember.asCompleteQuote?.perils.orEmpty()
                        .map { it.fragments.perilFragment }
                    (perils.adapter as? PerilsAdapter)?.items = items

                    when (data.inner.lastQuoteOfMember.asCompleteQuote?.typeOfContract) {
                        TypeOfContract.SE_HOUSE -> {
                            perilInfo.setText(R.string.OFFER_SCREEN_COVERAGE_BODY_HOUSE)
                        }
                        TypeOfContract.SE_APARTMENT_BRF,
                        TypeOfContract.SE_APARTMENT_STUDENT_BRF,
                        TypeOfContract.NO_HOME_CONTENT_OWN,
                        TypeOfContract.NO_HOME_CONTENT_YOUTH_OWN
                        -> {
                            perilInfo.setText(R.string.OFFER_SCREEN_COVERAGE_BODY_BRF)
                        }
                        TypeOfContract.NO_HOME_CONTENT_RENT,
                        TypeOfContract.NO_HOME_CONTENT_YOUTH_RENT,
                        TypeOfContract.SE_APARTMENT_RENT,
                        TypeOfContract.SE_APARTMENT_STUDENT_RENT -> {
                            perilInfo.setText(R.string.OFFER_SCREEN_COVERAGE_BODY_RENTAL)
                        }
                        else -> {
                        }
                    }
                    return
                }

                e { "Invariant detected: ${data.javaClass.name} passed to ${this.javaClass.name}::bind" }
            }
        }

        class Terms(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.offer_terms_area)) {
            private val insurableLimits = itemView.insurableLimits
            private val termsDocuments = itemView.termsDocuments

            init {
                insurableLimits.adapter = InsurableLimitsAdapter()
                insurableLimits.addItemDecoration(GridSpacingItemDecoration(BASE_MARGIN_DOUBLE))
            }

            override fun bind(
                data: OfferModel,
                fragmentManager: FragmentManager,
                tracker: OfferTracker,
                removeDiscount: () -> Unit
            ) {
                if (termsDocuments.adapter == null) {
                    termsDocuments.adapter = TermsAdapter(tracker)
                }
                if (data is OfferModel.Terms) {
                    data.inner.lastQuoteOfMember.asCompleteQuote?.insurableLimits?.map { it.fragments.insurableLimitsFragment }
                        ?.let { (insurableLimits.adapter as? InsurableLimitsAdapter)?.items = it }
                    data.inner.lastQuoteOfMember.asCompleteQuote?.insuranceTerms?.let {
                        (termsDocuments.adapter as? TermsAdapter)?.items = it
                    }
                    return
                }

                e { "Invariant detected: ${data.javaClass.name} passed to ${this.javaClass.name}::bind" }
            }
        }

        class Footer(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.offer_footer)) {
            override fun bind(
                data: OfferModel,
                fragmentManager: FragmentManager,
                tracker: OfferTracker,
                removeDiscount: () -> Unit
            ) {
                itemView.setHapticClickListener {
                    tracker.floatingSign()
                    OfferSignDialog.newInstance().show(
                        fragmentManager,
                        OfferSignDialog.TAG
                    )
                }
            }
        }
    }
}

sealed class OfferModel {
    data class Header(
        val inner: OfferQuery.Data
    ) : OfferModel()

    object Info : OfferModel()

    data class Facts(
        val inner: OfferQuery.Data
    ) : OfferModel()

    data class Perils(
        val inner: OfferQuery.Data
    ) : OfferModel()

    data class Terms(
        val inner: OfferQuery.Data
    ) : OfferModel()

    object Footer : OfferModel()
}

class OfferDiffCallback(
    private val old: List<OfferModel>,
    private val new: List<OfferModel>
) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        old[oldItemPosition] == new[newItemPosition]

    override fun getOldListSize() = old.size
    override fun getNewListSize() = new.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        areItemsTheSame(oldItemPosition, newItemPosition)
}
