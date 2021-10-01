package com.hedvig.app.feature.insurance.ui

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.databinding.ContractPillBinding
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.viewBinding

class ContractPillAdapter(
    private val marketManager: MarketManager
) : ListAdapter<String, ContractPillAdapter.ViewHolder>(GenericDiffUtilItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent, marketManager)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(parent: ViewGroup, val marketManager: MarketManager) :
        RecyclerView.ViewHolder(parent.inflate(R.layout.contract_pill)) {
        private val binding by viewBinding(ContractPillBinding::bind)
        fun bind(item: String) {
            binding.root.text = item
        }
    }
}
