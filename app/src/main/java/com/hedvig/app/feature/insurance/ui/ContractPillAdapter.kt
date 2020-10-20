package com.hedvig.app.feature.insurance.ui

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.app.R
import com.hedvig.app.databinding.ContractPillBinding
import com.hedvig.app.util.GenericDiffUtilCallback
import com.hedvig.app.util.extensions.getStringId
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.viewBinding

class ContractPillAdapter : RecyclerView.Adapter<ContractPillAdapter.ViewHolder>() {

    var items: List<ContractModel> = emptyList()
        set(value) {
            val diff = DiffUtil.calculateDiff(GenericDiffUtilCallback(field, value))
            field = value
            diff.dispatchUpdatesTo(this)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.count()

    class ViewHolder(parent: ViewGroup) :
        RecyclerView.ViewHolder(parent.inflate(R.layout.contract_pill)) {
        private val binding by viewBinding(ContractPillBinding::bind)
        fun bind(item: ContractModel) {
            binding.apply {
                when (item) {
                    is ContractModel.ContractType -> {
                        text.setText(item.type.getStringId())
                    }
                    is ContractModel.NoOfCoInsured -> {
                        if (item.noOfCoInsured == 0) {
                            text.setText("You")
                        } else {
                            text.setText("You+${item.noOfCoInsured}")
                        }
                    }
                }
            }
        }
    }
}

sealed class ContractModel {
    data class ContractType(val type: TypeOfContract) : ContractModel()
    data class NoOfCoInsured(val noOfCoInsured: Int) : ContractModel()
}
