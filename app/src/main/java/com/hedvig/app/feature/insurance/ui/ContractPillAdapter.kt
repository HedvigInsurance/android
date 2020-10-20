package com.hedvig.app.feature.insurance.ui

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.app.R
import com.hedvig.app.databinding.ContractPillBinding
import com.hedvig.app.util.GenericDiffUtilCallback
import com.hedvig.app.util.extensions.getStringIdForCard
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.viewBinding
import e

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
                        text.text =
                            text.context.getString(item.type.getStringIdForCard()).toUpperCase()
                    }
                    is ContractModel.NoOfCoInsured -> {
                        if (item.noOfCoInsured == 0) {
                            text.text = text.context.getString(R.string.insurance_tab_covers_you_tag).toUpperCase()
                        } else {
                            text.text = "${text.context.getString(R.string.insurance_tab_covers_you_tag).toUpperCase()}+${item.noOfCoInsured}"
                        }
                    }
                    is ContractModel.Student -> {
                        when (item.type) {
                            TypeOfContract.SE_APARTMENT_STUDENT_BRF,
                            TypeOfContract.SE_APARTMENT_STUDENT_RENT -> {
                                text.text =
                                    text.context.getString(R.string.insurance_tab_student_tag)
                                        .toUpperCase()
                            }
                            TypeOfContract.NO_HOME_CONTENT_YOUTH_OWN,
                            TypeOfContract.NO_HOME_CONTENT_YOUTH_RENT -> {
                                text.text = text.context.getString(R.string.insurance_tab_youth_tag)
                                    .toUpperCase()
                            }
                            else -> {
                                e { "Invalid data passed to ${ContractModel.Student::class.simpleName}" }
                            }
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
    data class Student(val type: TypeOfContract) : ContractModel()
}
