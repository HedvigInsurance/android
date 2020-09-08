package com.hedvig.app.feature.insurance.ui

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.app.R
import com.hedvig.app.databinding.DashboardUpsellBinding
import com.hedvig.app.databinding.InsuranceContractRowBinding
import com.hedvig.app.databinding.InsuranceErrorBinding
import com.hedvig.app.feature.chat.ui.ChatActivity
import com.hedvig.app.feature.insurance.service.InsuranceTracker
import com.hedvig.app.feature.insurance.ui.contractcoverage.ContractCoverageActivity
import com.hedvig.app.feature.insurance.ui.contractdetail.ContractDetailActivity
import com.hedvig.app.util.GenericDiffUtilCallback
import com.hedvig.app.util.extensions.canOpenUri
import com.hedvig.app.util.extensions.compatDrawable
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.openUri
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.viewBinding
import e
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class InsuranceAdapter(
    private val fragmentManager: FragmentManager,
    private val tracker: InsuranceTracker,
    private val retry: () -> Unit
) :
    RecyclerView.Adapter<InsuranceAdapter.ViewHolder>() {
    var items: List<InsuranceModel> = emptyList()
        set(value) {
            val diff = DiffUtil.calculateDiff(GenericDiffUtilCallback(field, value))
            field = value
            diff.dispatchUpdatesTo(this)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.insurance_contract_row -> ViewHolder.ContractViewHolder(parent)
        R.layout.dashboard_upsell -> ViewHolder.UpsellViewHolder(parent)
        R.layout.insurance_header -> ViewHolder.TitleViewHolder(parent)
        R.layout.insurance_error -> ViewHolder.Error(parent)
        else -> {
            throw Error("Unreachable")
        }
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder.ContractViewHolder -> {
                (items[position] as? InsuranceModel.Contract)?.let {
                    holder.bind(
                        it.inner,
                        fragmentManager,
                        tracker
                    )
                }
            }
            is ViewHolder.UpsellViewHolder -> {
                (items[position] as? InsuranceModel.Upsell)?.let { holder.bind(it) }
            }
            is ViewHolder.Error -> {
                holder.bind(retry, tracker)
            }
        }
    }

    override fun getItemViewType(position: Int) = when (items[position]) {
        is InsuranceModel.Contract -> R.layout.insurance_contract_row
        is InsuranceModel.Upsell -> R.layout.dashboard_upsell
        is InsuranceModel.Header -> R.layout.insurance_header
        InsuranceModel.Error -> R.layout.insurance_error
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        class UpsellViewHolder(parent: ViewGroup) : ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.dashboard_upsell, parent, false)
        ) {
            private val binding by viewBinding(DashboardUpsellBinding::bind)

            init {
                binding.apply {
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
            }

            fun bind(model: InsuranceModel.Upsell) {
                binding.apply {
                    title.text = title.resources.getString(model.title)
                    description.text = description.resources.getString(model.description)
                    cta.text = cta.resources.getString(model.ctaText)
                }
            }
        }

        class ContractViewHolder(parent: ViewGroup) : ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.insurance_contract_row, parent, false)
        ) {
            private val binding by viewBinding(InsuranceContractRowBinding::bind)

            fun bind(
                contract: InsuranceQuery.Contract,
                fragmentManager: FragmentManager,
                tracker: InsuranceTracker
            ) {
                binding.apply {
                    if (contract.upcomingRenewal != null) {
                        renewalCard.show()
                        contract.upcomingRenewal?.let { renewal ->
                            title.text =
                                title.resources.getString(R.string.DASHBOARD_RENEWAL_PROMPTER_TITLE)
                            body.text = body.resources.getString(
                                R.string.DASHBOARD_RENEWAL_PROMPTER_BODY,
                                ChronoUnit.DAYS.between(
                                    LocalDate.now(),
                                    renewal.renewalDate

                                )
                            )
                            action.text =
                                action.resources.getString(R.string.DASHBOARD_RENEWAL_PROMPTER_CTA)
                            val maybeLinkUri = runCatching {
                                Uri.parse(renewal.draftCertificateUrl)
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
                    }
                    contract.status.fragments.contractStatusFragment.let { contractStatus ->
                        contractStatus.asPendingStatus?.let {
                            this.contractStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                this.contractStatus.context.compatDrawable(
                                    R.drawable.ic_inactive
                                ), null, null, null
                            )
                            this.contractStatus.text =
                                this.contractStatus.resources.getString(R.string.DASHBOARD_INSURANCE_STATUS_INACTIVE_NO_STARTDATE)
                        }
                        contractStatus.asActiveInFutureStatus?.let { activeInFuture ->
                            this.contractStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                this.contractStatus.context.compatDrawable(
                                    R.drawable.ic_inactive
                                ), null, null, null
                            )
                            this.contractStatus.text = this.contractStatus.resources.getString(
                                R.string.DASHBOARD_INSURANCE_STATUS_INACTIVE_STARTDATE,
                                dateTimeFormatter.format(activeInFuture.futureInception)
                            )
                        }
                        contractStatus.asActiveInFutureAndTerminatedInFutureStatus?.let { activeAndTerminated ->
                            this.contractStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                this.contractStatus.context.compatDrawable(
                                    R.drawable.ic_inactive
                                ), null, null, null
                            )
                            this.contractStatus.text = this.contractStatus.resources.getString(
                                R.string.DASHBOARD_INSURANCE_STATUS_INACTIVE_STARTDATE_TERMINATED_IN_FUTURE,
                                activeAndTerminated.futureInception,
                                dateTimeFormatter.format(activeAndTerminated.futureTermination)
                            )
                        }
                        contractStatus.asActiveStatus?.let {
                            this.contractStatus.text =
                                this.contractStatus.resources.getString(R.string.DASHBOARD_INSURANCE_STATUS_ACTIVE)
                        }
                        contractStatus.asTerminatedInFutureStatus?.let { terminatedInFuture ->
                            this.contractStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                this.contractStatus.context.compatDrawable(
                                    R.drawable.ic_termination_in_future
                                ), null, null, null
                            )
                            this.contractStatus.text = this.contractStatus.resources.getString(
                                R.string.DASHBOARD_INSURANCE_STATUS_ACTIVE_TERMINATIONDATE,
                                dateTimeFormatter.format(terminatedInFuture.futureTermination)
                            )
                        }
                        contractStatus.asTerminatedTodayStatus?.let {
                            this.contractStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                this.contractStatus.context.compatDrawable(
                                    R.drawable.ic_termination_in_future
                                ), null, null, null
                            )
                            this.contractStatus.text =
                                this.contractStatus.resources.getString(R.string.DASHBOARD_INSURANCE_STATUS_TERMINATED_TODAY)
                        }
                        contractStatus.asTerminatedStatus?.let {
                            this.contractStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                this.contractStatus.context.compatDrawable(
                                    R.drawable.ic_terminated
                                ), null, null, null
                            )
                            this.contractStatus.text =
                                this.contractStatus.resources.getString(R.string.DASHBOARD_INSURANCE_STATUS_TERMINATED)
                        }
                    }


                    contractName.text = contract.displayName

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

                    contractInformationCard.setHapticClickListener {
                        tracker.contractInformationCard()
                        contractInformationCard.context.startActivity(
                            ContractDetailActivity.newInstance(
                                contractInformationCard.context,
                                contract.id
                            )
                        )
                    }

                    coverageCard.setHapticClickListener {
                        tracker.coverageCard()
                        coverageCard.context.startActivity(
                            ContractCoverageActivity.newInstance(
                                coverageCard.context,
                                contract.id
                            )
                        )
                    }

                    documentsCard.setHapticClickListener {
                        tracker.documentsCard()
                        DocumentBottomSheet
                            .newInstance(
                                contract.currentAgreement.asAgreementCore?.certificateUrl,
                                contract.termsAndConditions.url
                            )
                            .show(fragmentManager, DocumentBottomSheet.TAG)
                    }
                }
            }
        }

        class TitleViewHolder(parent: ViewGroup) : ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.insurance_header, parent, false)
        )

        class Error(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.insurance_error)) {
            private val binding by viewBinding(InsuranceErrorBinding::bind)
            fun bind(retry: () -> Unit, tracker: InsuranceTracker): Any? = with(binding) {
                this.retry.setHapticClickListener {
                    tracker.retry()
                    retry()
                }
            }
        }
    }

    companion object {
        private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy MM dd")

        private val InsuranceQuery.CurrentAgreement.numberCoInsured: Int
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

sealed class InsuranceModel {
    object Header : InsuranceModel()

    data class Contract(
        val inner: InsuranceQuery.Contract
    ) : InsuranceModel()

    data class Upsell(
        @get:StringRes val title: Int,
        @get:StringRes val description: Int,
        @get:StringRes val ctaText: Int
    ) : InsuranceModel()

    object Error : InsuranceModel()
}
