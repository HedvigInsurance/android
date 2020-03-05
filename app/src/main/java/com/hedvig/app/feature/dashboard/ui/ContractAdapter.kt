package com.hedvig.app.feature.dashboard.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
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
        (holder.perilCategories.adapter as? PerilCategoriesAdapter)?.perilCategories = items[position].perilCategories
    }

    class ContractViewHolder(parent: ViewGroup, fragmentManager: FragmentManager) : RecyclerView.ViewHolder(
        LayoutInflater
            .from(parent.context)
            .inflate(R.layout.dashboard_contract_row, parent, false)
    ) {
        val perilCategories: RecyclerView = itemView.perilCategories

        init {
            perilCategories.adapter = PerilCategoriesAdapter(fragmentManager)
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
