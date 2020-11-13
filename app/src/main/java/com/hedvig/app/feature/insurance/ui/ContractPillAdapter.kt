package com.hedvig.app.feature.insurance.ui

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.app.R
import com.hedvig.app.databinding.ContractPillBinding
import com.hedvig.app.getLocale
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.viewBinding

class ContractPillAdapter :
    ListAdapter<ContractModel, ContractPillAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(parent: ViewGroup) :
        RecyclerView.ViewHolder(parent.inflate(R.layout.contract_pill)) {
        private val binding by viewBinding(ContractPillBinding::bind)
        fun bind(item: ContractModel) {
            binding.apply {
                when (item) {
                    is ContractModel.Address -> {
                        item.currentAgreement.asDanishHomeContentAgreement?.let {
                            text.text = it.address.fragments.addressFragment.street.toUpperCase(
                                getLocale(text.context)
                            )
                        }
                        item.currentAgreement.asNorwegianHomeContentAgreement?.let {
                            text.text = it.address.fragments.addressFragment.street.toUpperCase(
                                getLocale(text.context)
                            )
                        }
                        item.currentAgreement.asSwedishApartmentAgreement?.let {
                            text.text = it.address.fragments.addressFragment.street.toUpperCase(
                                getLocale(text.context)
                            )
                        }
                        item.currentAgreement.asSwedishHouseAgreement?.let {
                            text.text = it.address.fragments.addressFragment.street.toUpperCase(
                                getLocale(text.context)
                            )
                        }
                        item.currentAgreement.asDanishHomeContentAgreement?.let {
                            text.text = it.address.fragments.addressFragment.street.toUpperCase(
                                getLocale(text.context)
                            )
                        }
                    }
                    is ContractModel.NoOfCoInsured -> {
                        if (item.noOfCoInsured == 0) {
                            text.text =
                                text.context.getString(R.string.insurance_tab_covers_you_tag)
                        } else {
                            text.text = text.context.getString(
                                R.string.insurance_tab_covers_you_plus_tag,
                                item.noOfCoInsured
                            )
                        }
                    }
                }
            }
        }
    }
}

class DiffCallback : DiffUtil.ItemCallback<ContractModel>() {
    override fun areItemsTheSame(oldItem: ContractModel, newItem: ContractModel) =
        oldItem === newItem

    override fun areContentsTheSame(oldItem: ContractModel, newItem: ContractModel) =
        oldItem == newItem
}

sealed class ContractModel {
    data class Address(val currentAgreement: InsuranceQuery.CurrentAgreement) : ContractModel()
    data class NoOfCoInsured(val noOfCoInsured: Int) : ContractModel()
}
