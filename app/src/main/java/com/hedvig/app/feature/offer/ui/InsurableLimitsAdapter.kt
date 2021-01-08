package com.hedvig.app.feature.offer.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.android.owldroid.fragment.InsurableLimitsFragment
import com.hedvig.app.R
import com.hedvig.app.databinding.CoverageInsurableLimitBinding
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.viewBinding

class InsurableLimitsAdapter :
    ListAdapter<InsurableLimitsFragment, InsurableLimitsAdapter.ViewHolder>(
        GenericDiffUtilItemCallback()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater
            .from(parent.context)
            .inflate(R.layout.coverage_insurable_limit, parent, false)
    ) {
        private val binding by viewBinding(CoverageInsurableLimitBinding::bind)

        fun bind(data: InsurableLimitsFragment) {
            binding.apply {
                label.text = data.label
                limit.text = data.limit
            }
        }
    }
}
