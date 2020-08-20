package com.hedvig.app.feature.dashboard.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.android.owldroid.graphql.DashboardQuery
import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.app.R
import com.hedvig.app.feature.chat.ui.ChatActivity
import com.hedvig.app.feature.dashboard.service.DashboardTracker
import com.hedvig.app.feature.dashboard.ui.contractcoverage.ContractCoverageActivity
import com.hedvig.app.feature.dashboard.ui.contractdetail.ContractDetailActivity
import com.hedvig.app.util.extensions.compatDrawable
import com.hedvig.app.util.extensions.view.setHapticClickListener
import e
import kotlinx.android.synthetic.main.dashboard_contract_row.view.*
import kotlinx.android.synthetic.main.dashboard_upsell.view.*
import java.time.format.DateTimeFormatter

class DashboardAdapter(
    private val fragmentManager: FragmentManager,
    private val tracker: DashboardTracker
) :
    RecyclerView.Adapter<DashboardAdapter.ViewHolder>() {
    var items: List<DashboardModel> = emptyList()
        set(value) {
            val diff = DiffUtil.calculateDiff(DashboardDiffUtilCallback(field, value))
            field = value
            diff.dispatchUpdatesTo(this)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.dashboard_contract_row -> ViewHolder.ContractViewHolder(parent)
        R.layout.dashboard_upsell -> ViewHolder.UpsellViewHolder(parent)
        R.layout.dashboard_header -> ViewHolder.TitleViewHolder(parent)
        else -> {
            throw Error("Unreachable")
        }
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder.ContractViewHolder -> {
                (items[position] as? DashboardModel.Contract)?.let {
                    holder.bind(
                        it.inner,
                        fragmentManager
                    )
                }
            }
            is ViewHolder.UpsellViewHolder -> {
                (items[position] as? DashboardModel.Upsell)?.let { holder.bind(it) }
            }
        }
    }

    override fun getItemViewType(position: Int) = when (items[position]) {
        is DashboardModel.Contract -> R.layout.dashboard_contract_row
        is DashboardModel.Upsell -> R.layout.dashboard_upsell
        is DashboardModel.Header -> R.layout.dashboard_header
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        class UpsellViewHolder(parent: ViewGroup) : ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.dashboard_upsell, parent, false)
        ) {
            private val title = itemView.title
            private val description = itemView.description
            private val cta = itemView.cta

            init {
                cta.setHapticClickListener {
                    val intent = ChatActivity.newInstance(cta.context, true)
                    val options =
                        ActivityOptionsCompat.makeCustomAnimation(
                            cta.context,
                            R.anim.activity_slide_up_in,
                            R.anim.stay_in_place
                        )

                    ActivityCompat.startActivity(cta.context, intent, options.toBundle())
                }
            }

            fun bind(model: DashboardModel.Upsell) {
                title.text = title.resources.getString(model.title)
                description.text = description.resources.getString(model.description)
                cta.text = cta.resources.getString(model.ctaText)
            }
        }

        /*
        class InfoBoxViewHolder(parent: ViewGroup) : ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.dashboard_info_card, parent, false)
        ) {
            private val title = itemView.title
            private val body = itemView.body
            private val action = itemView.action

            fun bind(data: DashboardModel.InfoBox, tracker: DashboardTracker) {
                when (data) {
                    is DashboardModel.InfoBox.ImportantInformation -> {
                        title.text = data.title
                        body.text = data.body
                        action.text = data.actionLabel
                        val maybeLinkUri = runCatching {
                            Uri.parse(data.actionLink)
                        }
                        action.setHapticClickListener {
                            maybeLinkUri.getOrNull()?.let { uri ->
                                if (action.context.canOpenUri(uri)) {
                                    action.context.openUri(uri)
                                }
                            }
                        }
                    }
                    is DashboardModel.InfoBox.Renewal -> {
                        title.text =
                            title.resources.getString(R.string.DASHBOARD_RENEWAL_PROMPTER_TITLE)
                        body.text = body.resources.getString(
                            R.string.DASHBOARD_RENEWAL_PROMPTER_BODY, ChronoUnit.DAYS.between(
                                LocalDate.now(),
                                data.renewalDate
                            )
                        )
                        action.text =
                            action.resources.getString(R.string.DASHBOARD_RENEWAL_PROMPTER_CTA)
                        val maybeLinkUri = runCatching {
                            Uri.parse(data.draftCertificateUrl)
                        }
                        action.setHapticClickListener {
                            tracker.showRenewal()
                            maybeLinkUri.getOrNull()?.let { uri ->
                                if (action.context.canOpenUri(uri)) {
                                    action.context.openUri(uri)
                                }
                            }
                        }
                    }
                    is DashboardModel.InfoBox.ConnectPayin -> {
                        title.text =
                            title.resources.getString(R.string.DASHBOARD_SETUP_DIRECT_DEBIT_TITLE)
                        body.text =
                            body.resources.getString(R.string.DASHBOARD_DIRECT_DEBIT_STATUS_NEED_SETUP_DESCRIPTION)
                        action.text =
                            body.resources.getString(R.string.DASHBOARD_DIRECT_DEBIT_STATUS_NEED_SETUP_BUTTON_LABEL)
                        action.setHapticClickListener {
                            tracker.connectPayment()
                            action.context.startActivity(ConnectPaymentActivity.newInstance(action.context))
                        }
                    }
                }
            }
        }
         */

        class ContractViewHolder(parent: ViewGroup) : ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.dashboard_contract_row, parent, false)
        ) {
            private val status = itemView.contractStatus
            private val name = itemView.contractName
            private val informationCard = itemView.contractInformationCard
            private val contractInformationIcon = itemView.contractInformationIcon
            private val contractInformationDescription = itemView.contractInformationDescription
            private val perilCard = itemView.coverageCard
            private val documentsCard = itemView.documentsCard

            fun bind(contract: DashboardQuery.Contract, fragmentManager: FragmentManager) {
                contract.status.fragments.contractStatusFragment.let { contractStatus ->
                    contractStatus.asPendingStatus?.let {
                        status.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            status.context.compatDrawable(
                                R.drawable.ic_inactive
                            ), null, null, null
                        )
                        status.text =
                            status.resources.getString(R.string.DASHBOARD_INSURANCE_STATUS_INACTIVE_NO_STARTDATE)
                    }
                    contractStatus.asActiveInFutureStatus?.let { activeInFuture ->
                        status.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            status.context.compatDrawable(
                                R.drawable.ic_inactive
                            ), null, null, null
                        )
                        status.text = status.resources.getString(
                            R.string.DASHBOARD_INSURANCE_STATUS_INACTIVE_STARTDATE,
                            dateTimeFormatter.format(activeInFuture.futureInception)
                        )
                    }
                    contractStatus.asActiveInFutureAndTerminatedInFutureStatus?.let { activeAndTerminated ->
                        status.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            status.context.compatDrawable(
                                R.drawable.ic_inactive
                            ), null, null, null
                        )
                        status.text = status.resources.getString(
                            R.string.DASHBOARD_INSURANCE_STATUS_INACTIVE_STARTDATE_TERMINATED_IN_FUTURE,
                            activeAndTerminated.futureInception,
                            dateTimeFormatter.format(activeAndTerminated.futureTermination)
                        )
                    }
                    contractStatus.asActiveStatus?.let {
                        status.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            status.context.compatDrawable(
                                R.drawable.ic_active
                            ), null, null, null
                        )
                        status.text =
                            status.resources.getString(R.string.DASHBOARD_INSURANCE_STATUS_ACTIVE)
                    }
                    contractStatus.asTerminatedInFutureStatus?.let { terminatedInFuture ->
                        status.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            status.context.compatDrawable(
                                R.drawable.ic_termination_in_future
                            ), null, null, null
                        )
                        status.text = status.resources.getString(
                            R.string.DASHBOARD_INSURANCE_STATUS_ACTIVE_TERMINATIONDATE,
                            dateTimeFormatter.format(terminatedInFuture.futureTermination)
                        )
                    }
                    contractStatus.asTerminatedTodayStatus?.let {
                        status.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            status.context.compatDrawable(
                                R.drawable.ic_termination_in_future
                            ), null, null, null
                        )
                        status.text =
                            status.resources.getString(R.string.DASHBOARD_INSURANCE_STATUS_TERMINATED_TODAY)
                    }
                    contractStatus.asTerminatedStatus?.let {
                        status.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            status.context.compatDrawable(
                                R.drawable.ic_terminated
                            ), null, null, null
                        )
                        status.text =
                            status.resources.getString(R.string.DASHBOARD_INSURANCE_STATUS_TERMINATED)
                    }
                }

                name.text = contract.displayName

                when (contract.typeOfContract) {
                    TypeOfContract.NO_TRAVEL_YOUTH,
                    TypeOfContract.NO_TRAVEL -> {
                        contractInformationIcon.setImageDrawable(
                            contractInformationIcon.context.compatDrawable(
                                R.drawable.ic_contract_type_travel
                            )
                        )
                    }
                    TypeOfContract.SE_HOUSE -> {
                        contractInformationIcon.setImageDrawable(
                            contractInformationIcon.context.compatDrawable(
                                R.drawable.ic_house
                            )
                        )
                    }
                    TypeOfContract.SE_APARTMENT_BRF,
                    TypeOfContract.SE_APARTMENT_RENT,
                    TypeOfContract.SE_APARTMENT_STUDENT_BRF,
                    TypeOfContract.SE_APARTMENT_STUDENT_RENT,
                    TypeOfContract.NO_HOME_CONTENT_OWN,
                    TypeOfContract.NO_HOME_CONTENT_RENT,
                    TypeOfContract.NO_HOME_CONTENT_YOUTH_OWN,
                    TypeOfContract.NO_HOME_CONTENT_YOUTH_RENT -> {
                        contractInformationIcon.setImageDrawable(
                            contractInformationIcon.context.compatDrawable(
                                R.drawable.ic_apartment
                            )
                        )
                    }
                    TypeOfContract.UNKNOWN__ -> {
                    }
                }
                contractInformationDescription.text =
                    if (contract.currentAgreement.numberCoInsured == 0) {
                        contractInformationDescription.resources.getString(R.string.DASHBOARD_MY_INFO_NO_COINSURED)
                    } else {
                        contractInformationDescription.resources.getString(
                            R.string.DASHBOARD_MY_INFO_COINSURED,
                            contract.currentAgreement.numberCoInsured
                        )
                    }

                informationCard.setHapticClickListener {
                    informationCard.context.startActivity(
                        ContractDetailActivity.newInstance(
                            informationCard.context,
                            contract.id
                        )
                    )
                }

                perilCard.setHapticClickListener {
                    perilCard.context.startActivity(
                        ContractCoverageActivity.newInstance(
                            perilCard.context,
                            contract.id
                        )
                    )
                }

                documentsCard.setHapticClickListener {
                    DocumentBottomSheet
                        .newInstance(
                            contract.currentAgreement.asAgreementCore?.certificateUrl,
                            contract.termsAndConditions.url
                        )
                        .show(fragmentManager, DocumentBottomSheet.TAG)
                }
            }
        }

        class TitleViewHolder(parent: ViewGroup) : ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.dashboard_header, parent, false)
        )
    }

    companion object {
        private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy MM dd")

        private val DashboardQuery.CurrentAgreement.numberCoInsured: Int
            get() {
                asNorwegianTravelAgreement?.numberCoInsured?.let { return it }
                asSwedishHouseAgreement?.numberCoInsured?.let { return it }
                asSwedishApartmentAgreement?.numberCoInsured?.let { return it }
                asNorwegianHomeContentAgreement?.numberCoInsured?.let { return it }
                e { "Unable to infer amount coinsured for agreement: $this" }
                return 0
            }
    }
}

sealed class DashboardModel {
    object Header : DashboardModel()

    /*
    sealed class InfoBox : DashboardModel() {
        data class ImportantInformation(
            val title: String,
            val body: String,
            val actionLabel: String,
            val actionLink: String
        ) : InfoBox()

        data class Renewal(
            val renewalDate: LocalDate,
            val draftCertificateUrl: String
        ) : InfoBox()

        object ConnectPayin : InfoBox()
    }
     */

    data class Contract(
        val inner: DashboardQuery.Contract
    ) : DashboardModel()

    data class Upsell(
        @get:StringRes val title: Int,
        @get:StringRes val description: Int,
        @get:StringRes val ctaText: Int
    ) : DashboardModel()
}

class DashboardDiffUtilCallback(
    private val old: List<DashboardModel>,
    private val new: List<DashboardModel>
) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        old[oldItemPosition] == new[newItemPosition]

    override fun getOldListSize() = old.size
    override fun getNewListSize() = new.size
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        areItemsTheSame(oldItemPosition, newItemPosition)
}
