package com.hedvig.app.feature.dashboard.ui.contractcoverage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.android.owldroid.graphql.DashboardQuery
import com.hedvig.app.R
import kotlinx.android.synthetic.main.coverage_insurable_limit.view.*

class InsurableLimitsAdapter : RecyclerView.Adapter<InsurableLimitsAdapter.ViewHolder>() {
    var items: List<DashboardQuery.InsurableLimit> = emptyList()
        set(value) {
            val diff = DiffUtil.calculateDiff(InsurableLimitsDiffCallback(field, value))
            field = value
            diff.dispatchUpdatesTo(this)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)
    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater
            .from(parent.context)
            .inflate(R.layout.coverage_insurable_limit, parent, false)
    ) {
        private val label = itemView.label
        private val limit = itemView.limit

        fun bind(data: DashboardQuery.InsurableLimit) {
            label.text = data.label
            limit.text = data.limit
        }
    }

    class InsurableLimitsDiffCallback(
        private val old: List<DashboardQuery.InsurableLimit>,
        private val new: List<DashboardQuery.InsurableLimit>
    ) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) = old[oldItemPosition] == new[newItemPosition]
        override fun getOldListSize() = old.size
        override fun getNewListSize() = new.size
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = areItemsTheSame(oldItemPosition, newItemPosition)
    }
}
