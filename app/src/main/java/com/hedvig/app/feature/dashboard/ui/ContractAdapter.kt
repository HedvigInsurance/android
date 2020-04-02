package com.hedvig.app.feature.dashboard.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.android.owldroid.graphql.DashboardQuery
import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.app.R
import com.hedvig.app.feature.dashboard.ui.contractcoverage.ContractCoverageActivity
import com.hedvig.app.feature.dashboard.ui.contractdetail.ContractDetailActivity
import com.hedvig.app.util.extensions.compatDrawable
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.interpolateTextKey
import e
import kotlinx.android.synthetic.main.dashboard_contract_row.view.*
import org.threeten.bp.format.DateTimeFormatter

class ContractAdapter(
    private val fragmentManager: FragmentManager
) : RecyclerView.Adapter<ContractAdapter.ContractViewHolder>() {
    var items: List<DashboardQuery.Contract> = emptyList()
        set(value) {
            val diff = DiffUtil.calculateDiff(ContractDiffCallback(field, value))
            field = value
            diff.dispatchUpdatesTo(this)
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ContractViewHolder(parent)
    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ContractViewHolder, position: Int) {
        holder.bind(items[position], fragmentManager)
    }

    class ContractViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
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
                    status.text =
                        status.resources.getString(R.string.DASHBOARD_INSURANCE_STATUS_INACTIVE_NO_STARTDATE)
                }
                contractStatus.asActiveInFutureStatus?.let { activeInFuture ->
                    status.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        status.context.compatDrawable(
                            R.drawable.ic_inactive
                        ), null, null, null
                    )
                    status.text = interpolateTextKey(
                        status.resources.getString(R.string.DASHBOARD_INSURANCE_STATUS_INACTIVE_STARTDATE),
                        "ACTIVATION_DATE" to dateTimeFormatter.format(activeInFuture.futureInception)
                    )
                }
                contractStatus.asActiveInFutureAndTerminatedInFutureStatus?.let { activeAndTerminated ->
                    status.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        status.context.compatDrawable(
                            R.drawable.ic_inactive
                        ), null, null, null
                    )
                    status.text = interpolateTextKey(
                        status.resources.getString(R.string.DASHBOARD_INSURANCE_STATUS_INACTIVE_STARTDATE_TERMINATED_IN_FUTURE),
                        "ACTIVATION_DATE" to activeAndTerminated.futureInception,
                        "TERMINATION_DATE" to dateTimeFormatter.format(activeAndTerminated.futureTermination)
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
                    status.text = interpolateTextKey(
                        status.resources.getString(R.string.DASHBOARD_INSURANCE_STATUS_ACTIVE_TERMINATIONDATE),
                        "TERMINATION_DATE" to dateTimeFormatter.format(terminatedInFuture.futureTermination)
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
                    interpolateTextKey(
                        contractInformationDescription.resources.getString(R.string.DASHBOARD_MY_INFO_COINSURED),
                        "COINSURED_PEOPLE" to contract.currentAgreement.numberCoInsured
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

class ContractDiffCallback(
    private val old: List<DashboardQuery.Contract>,
    private val new: List<DashboardQuery.Contract>
) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        old[oldItemPosition].id == new[newItemPosition].id

    override fun getOldListSize() = old.size
    override fun getNewListSize() = new.size
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        old[oldItemPosition] == new[newItemPosition]
}
