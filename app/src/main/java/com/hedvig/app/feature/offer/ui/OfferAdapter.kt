package com.hedvig.app.feature.offer.ui

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnNextLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.app.BASE_MARGIN_DOUBLE
import com.hedvig.app.BASE_MARGIN_HALF
import com.hedvig.app.R
import com.hedvig.app.databinding.OfferFactAreaBinding
import com.hedvig.app.databinding.OfferHeaderBinding
import com.hedvig.app.databinding.OfferPerilAreaBinding
import com.hedvig.app.databinding.OfferSwitchBinding
import com.hedvig.app.databinding.OfferTermsAreaBinding
import com.hedvig.app.feature.offer.OfferRedeemCodeBottomSheet
import com.hedvig.app.feature.offer.OfferSignDialog
import com.hedvig.app.feature.offer.OfferTracker
import com.hedvig.app.feature.offer.TermsAdapter
import com.hedvig.app.feature.offer.ui.changestartdate.ChangeDateBottomSheet
import com.hedvig.app.feature.offer.ui.changestartdate.ChangeDateBottomSheetData
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.feature.table.generateTable
import com.hedvig.app.feature.table.intoTable
import com.hedvig.app.ui.decoration.GridSpacingItemDecoration
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.apollo.format
import com.hedvig.app.util.apollo.toMonetaryAmount
import com.hedvig.app.util.extensions.getStringId
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.invalid
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
    private val removeDiscount: () -> Unit,
) : ListAdapter<OfferModel, OfferAdapter.ViewHolder>(GenericDiffUtilItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.offer_header -> ViewHolder.Header(parent)
        R.layout.offer_fact_area -> ViewHolder.Facts(parent)
        R.layout.offer_peril_area -> ViewHolder.Perils(parent)
        R.layout.offer_terms_area -> ViewHolder.Terms(parent)
        R.layout.offer_switch -> ViewHolder.Switch(parent)
        else -> throw Error("Invalid viewType: $viewType")
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is OfferModel.Header -> R.layout.offer_header
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
            marketManager: MarketManager,
        )

        class Header(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.offer_header)) {
            private val binding by viewBinding(OfferHeaderBinding::bind)

            override fun bind(
                data: OfferModel,
                fragmentManager: FragmentManager,
                tracker: OfferTracker,
                removeDiscount: () -> Unit,
                marketManager: MarketManager,
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
                                .format(premium.context, marketManager.market)

                            premium.text = monetaryAmount
                            premiumPeriod.text = premiumPeriod.context.getString(R.string.OFFER_PRICE_PER_MONTH)

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
                                ?.displayValue
                                ?.let { discountText ->
                                    // TODO Add displayValues from all bundles when quering from QuoteBundle
                                    campaign.text = discountText
                                    campaign.show()
                                    originalPremium.apply {
                                        paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                                        // TODO Use monthlyGross from bundleCost
                                        text = monetaryAmount
                                    }

                                    premiumContainer.setBackgroundResource(
                                        R.drawable.background_premium_box_with_campaign
                                    )

                                    val errorColor = ContextCompat.getColor(discountButton.context, R.color.colorError)
                                    discountButton.setText(R.string.OFFER_REMOVE_DISCOUNT_BUTTON)
                                    discountButton.setTextColor(errorColor)
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
                                } ?: run {
                                discountButton.setText(R.string.OFFER_ADD_DISCOUNT_BUTTON)
                                val buttonTextColor =
                                    ContextCompat.getColor(discountButton.context, R.color.textColorLink)
                                discountButton.setTextColor(buttonTextColor)
                                premiumContainer.background = null
                                campaign.remove()
                                discountButton.setHapticClickListener {
                                    tracker.addDiscount()
                                    OfferRedeemCodeBottomSheet.newInstance()
                                        .show(
                                            fragmentManager,
                                            OfferRedeemCodeBottomSheet.TAG
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
                marketManager: MarketManager,
            ) {
                if (data !is OfferModel.Facts) {
                    return invalid(data)
                }
                data.inner.lastQuoteOfMember.asCompleteQuote?.detailsTable?.fragments?.tableFragment?.intoTable()
                    ?.let { table ->
                        generateTable(binding.expandableContent, table)
                        binding.expandableContentView.doOnNextLayout {
                            binding.expandableContentView.contentSizeChanged()
                        }
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
                marketManager: MarketManager,
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
                marketManager: MarketManager,
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
                marketManager: MarketManager,
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
