package com.hedvig.app.feature.dashboard.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.android.owldroid.graphql.DashboardQuery
import com.hedvig.android.owldroid.type.AgreementStatus
import com.hedvig.android.owldroid.type.ContractStatus
import com.hedvig.app.R
import com.hedvig.app.feature.dashboard.ui.contractcoverage.ContractCoverageActivity
import com.hedvig.app.feature.dashboard.ui.contractdetail.ContractDetailActivity
import com.hedvig.app.util.extensions.compatDrawable
import com.hedvig.app.util.extensions.view.setHapticClickListener
import kotlinx.android.synthetic.main.dashboard_contract_row.view.*
import org.threeten.bp.format.DateTimeFormatter

class ContractAdapter : RecyclerView.Adapter<ContractAdapter.ContractViewHolder>() {
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
        holder.bind(items[position])
    }

    class ContractViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater
            .from(parent.context)
            .inflate(R.layout.dashboard_contract_row, parent, false)
    ) {
        private val status = itemView.contractStatus
        private val name = itemView.contractName
        private val informationCard = itemView.contractInformationCard
        private val perilCard = itemView.perilCard

        fun bind(contract: DashboardQuery.Contract) {
            name.text = contract.displayName
            when (contract.status) {
                ContractStatus.ACTIVE -> {
                    status.setCompoundDrawablesRelativeWithIntrinsicBounds(status.context.compatDrawable(R.drawable.ic_filled_checkmark_small), null, null, null)
                    status.text = "Active TODO" // TODO: Translation
                }
                ContractStatus.PENDING -> {
                    status.setCompoundDrawables(status.context.compatDrawable(R.drawable.ic_clock), null, null, null)
                    if (contract.currentAgreement.asAgreementCore?.status == AgreementStatus.PENDING) {
                        status.text = "Inget startdatum valt" // TODO: Translation
                    } else {
                        contract.currentAgreement.asAgreementCore?.activeFrom?.let { activeFrom ->
                            status.text = "Aktiveres ${activeFrom.format(FORMATTER)}" // TODO: Translation
                        }
                    }
                }
                ContractStatus.TERMINATED -> {
                    status.setCompoundDrawables(status.context.compatDrawable(R.drawable.ic_cross), null, null, null)
                    if (contract.currentAgreement.asAgreementCore?.status == AgreementStatus.TERMINATED) {
                        status.text = "Avslutet" // TODO: Translation
                    } else {
                        contract.currentAgreement.asAgreementCore?.activeTo?.let { activeTo ->
                            status.text = "Avslutes ${activeTo.format(FORMATTER)}" // TODO: Translation
                        }
                    }
                }
                else -> {
                } // TODO
            }
            informationCard.setHapticClickListener {
                informationCard.context.startActivity(ContractDetailActivity.newInstance(informationCard.context, contract.id))
            }

            perilCard.setHapticClickListener {
                perilCard.context.startActivity(ContractCoverageActivity.newInstance(perilCard.context, contract.id))
            }
        }
    }

    companion object {
        private val FORMATTER = DateTimeFormatter.ofPattern("dd, LLL YYYY")
    }
}

class ContractDiffCallback(
    private val old: List<DashboardQuery.Contract>,
    private val new: List<DashboardQuery.Contract>
) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) = old[oldItemPosition].id == new[newItemPosition].id
    override fun getOldListSize() = old.size
    override fun getNewListSize() = new.size
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = old[oldItemPosition] == new[newItemPosition]
}
