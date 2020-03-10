package com.hedvig.app.feature.dashboard.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.android.owldroid.type.ContractStatus
import com.hedvig.app.R
import com.hedvig.app.util.extensions.compatDrawable
import com.hedvig.app.util.extensions.view.animateCollapse
import com.hedvig.app.util.extensions.view.animateExpand
import com.hedvig.app.util.extensions.view.setHapticClickListener
import kotlinx.android.synthetic.main.dashboard_contract_row.view.*

class ContractAdapter(
    private val fragmentManager: FragmentManager
) : RecyclerView.Adapter<ContractAdapter.ContractViewHolder>() {
    var items: List<Contract> = emptyList()
        set(value) {
            val diff = DiffUtil.calculateDiff(ContractDiffCallback(field, value))
            field = value
            diff.dispatchUpdatesTo(this)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ContractViewHolder(parent, fragmentManager)
    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ContractViewHolder, position: Int) {
        holder.bind(items[position])
    }

    class ContractViewHolder(parent: ViewGroup, fragmentManager: FragmentManager) : RecyclerView.ViewHolder(
        LayoutInflater
            .from(parent.context)
            .inflate(R.layout.dashboard_contract_row, parent, false)
    ) {
        private val status = itemView.contractStatus
        private val name = itemView.contractName
        private val informationCard = itemView.contractInformationCard
        private val perilCard = itemView.perilCard
        private val perilExpandCollapse = itemView.expandCollapse
        private val perils = itemView.perilsContainer

        init {
            perils.adapter = PerilsAdapter(fragmentManager)
        }

        fun bind(contract: Contract) {
            when (contract.status) {
                ContractStatus.ACTIVE -> {
                    status.setCompoundDrawablesRelativeWithIntrinsicBounds(status.context.compatDrawable(R.drawable.ic_filled_checkmark_small), null, null, null)
                    status.text = "Active TODO" // TODO: Translation
                }
                ContractStatus.PENDING -> {
                    status.setCompoundDrawables(status.context.compatDrawable(R.drawable.ic_clock), null, null, null)
                    status.text = "?? TODO" // TODO: Translation
                }
                ContractStatus.TERMINATED -> {
                    status.setCompoundDrawables(status.context.compatDrawable(R.drawable.ic_cross), null, null, null)
                    status.text = "?? TODO" // TODO: Translation
                }
                else -> {
                } // TODO
            }
            informationCard.setHapticClickListener {
                informationCard.context.startActivity(ContractDetailActivity.newInstance(informationCard.context, contract.id))
            }
            (perils.adapter as? PerilsAdapter)?.setData("TODO", contract.perils)
            perilCard.setHapticClickListener {
                val isExpanded = perils.height != 0
                if (isExpanded) {
                    perils.animateCollapse(withOpacity = true)
                    perilExpandCollapse
                        .animate()
                        .withLayer()
                        .setDuration(200)
                        .setInterpolator(DecelerateInterpolator())
                        .rotation(0f)
                        .start()
                } else {
                    perils.updateLayoutParams { height = 1 }
                    perils.animateExpand(withOpacity = true)
                    perilExpandCollapse
                        .animate()
                        .withLayer()
                        .setDuration(200)
                        .setInterpolator(DecelerateInterpolator())
                        .rotation(-180f)
                        .start()
                }
            }
        }
    }
}

class ContractDiffCallback(
    private val old: List<Contract>,
    private val new: List<Contract>
) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) = old[oldItemPosition].id == new[newItemPosition].id
    override fun getOldListSize() = old.size
    override fun getNewListSize() = new.size
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = old[oldItemPosition] == new[newItemPosition]
}
