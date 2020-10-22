package com.hedvig.app.feature.insurance.ui.detail.yourinfo

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.databinding.ContractDetailYourInfoHeaderBinding
import com.hedvig.app.databinding.ContractDetailYourInfoRowBinding
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.viewBinding
import e

class YourInfoAdapter :
    ListAdapter<YourInfoModel, YourInfoAdapter.ViewHolder>(GenericDiffUtilItemCallback()) {

    override fun getItemViewType(position: Int) = when (currentList[position]) {
        is YourInfoModel.Header -> R.layout.contract_detail_your_info_header
        is YourInfoModel.Row -> R.layout.contract_detail_your_info_row
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.contract_detail_your_info_header -> ViewHolder.Header(parent)
        R.layout.contract_detail_your_info_row -> ViewHolder.Row(parent)
        else -> throw Error("Invalid view type")
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(data: YourInfoModel): Any?

        fun invalid(data: YourInfoModel) {
            e { "Invalid data passed to ${this.javaClass.name}::bind - type is ${data.javaClass.name}" }
        }

        class Header(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.contract_detail_your_info_header)) {
            private val binding by viewBinding(ContractDetailYourInfoHeaderBinding::bind)
            override fun bind(data: YourInfoModel) = with(binding) {
                if (data !is YourInfoModel.Header) {
                    return invalid(data)
                }

                root.setText(
                    when (data) {
                        YourInfoModel.Header.Details -> com.hedvig.app.R.string.CONTRACT_DETAIL_HOME_TITLE
                        YourInfoModel.Header.Coinsured -> com.hedvig.app.R.string.CONTRACT_DETAIL_COINSURED_TITLE
                    }
                )
            }
        }

        class Row(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.contract_detail_your_info_row)) {
            private val binding by viewBinding(ContractDetailYourInfoRowBinding::bind)
            override fun bind(data: YourInfoModel) = with(binding) {
                if (data !is YourInfoModel.Row) {
                    return invalid(data)
                }

                label.text = data.label
                content.text = data.content
            }
        }
    }
}
