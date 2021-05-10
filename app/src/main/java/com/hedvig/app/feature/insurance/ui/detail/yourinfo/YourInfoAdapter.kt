package com.hedvig.app.feature.insurance.ui.detail.yourinfo

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.databinding.ChangeAddressButtonBinding
import com.hedvig.app.databinding.ChangeAddressPendingChangeCardBinding
import com.hedvig.app.databinding.YourInfoChangeBinding
import com.hedvig.app.databinding.YourInfoCoinsuredBinding
import com.hedvig.app.databinding.YourInfoHomeBinding
import com.hedvig.app.feature.chat.ui.ChatActivity
import com.hedvig.app.feature.home.ui.changeaddress.ChangeAddressActivity
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding
import e

class YourInfoAdapter(
    private val fragmentManager: FragmentManager
) : ListAdapter<YourInfoModel, YourInfoAdapter.ViewHolder>(GenericDiffUtilItemCallback()) {

    override fun getItemViewType(position: Int) = when (currentList[position]) {
        is YourInfoModel.Home -> R.layout.your_info_home
        is YourInfoModel.ChangeAddressButton -> R.layout.change_address_button
        YourInfoModel.Change -> R.layout.your_info_change
        is YourInfoModel.Coinsured -> R.layout.your_info_coinsured
        is YourInfoModel.PendingAddressChange -> R.layout.change_address_pending_change_card
        YourInfoModel.ContractError -> TODO()
        YourInfoModel.PendingChangeError -> TODO()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.your_info_home -> ViewHolder.Home(parent)
        R.layout.change_address_button -> ViewHolder.ChangeAddressButton(parent)
        R.layout.your_info_change -> ViewHolder.Change(parent)
        R.layout.your_info_coinsured -> ViewHolder.Coinsured(parent)
        R.layout.change_address_pending_change_card -> ViewHolder.PendingAddressChange(parent)
        else -> throw Error("Invalid view type")
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position], fragmentManager)
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(data: YourInfoModel, fragmentManager: FragmentManager): Any?

        fun invalid(data: YourInfoModel) {
            e { "Invalid data passed to ${this.javaClass.name}::bind - type is ${data.javaClass.name}" }
        }

        class Change(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.your_info_change)) {
            private val binding by viewBinding(YourInfoChangeBinding::bind)
            override fun bind(data: YourInfoModel, fragmentManager: FragmentManager) = with(binding) {
                openChatButton.setHapticClickListener {
                    root.context.startActivity(ChatActivity.newInstance(root.context, true))
                }
            }
        }

        class Home(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.your_info_home)) {
            private val binding by viewBinding(YourInfoHomeBinding::bind)
            override fun bind(data: YourInfoModel, fragmentManager: FragmentManager) = with(binding) {
                if (data !is YourInfoModel.Home) {
                    invalid(data)
                } else {
                    addressValue.text = data.street
                    postcodeValue.text = data.postalCode
                    typeValue.text = data.type?.let(root.context::getString)
                    sizeValue.text = data.size.toString()
                }
            }
        }

        class Coinsured(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.your_info_coinsured)) {
            private val binding by viewBinding(YourInfoCoinsuredBinding::bind)
            override fun bind(data: YourInfoModel, fragmentManager: FragmentManager) = with(binding) {
                if (data !is YourInfoModel.Coinsured) {
                    invalid(data)
                } else {
                    coinsuredAmount.text = when (data.amount) {
                        0 -> root.context.getString(R.string.CONTRACT_DETAIL_COINSURED_NUMBER_INPUT_ZERO_COINSURED)
                        1 -> root.context.getString(R.string.CONTRACT_DETAIL_COINSURED_NUMBER_INPUT_ONE_COINSURED)
                        else -> root.context.getString(R.string.CONTRACT_DETAIL_COINSURED_NUMBER_INPUT, data.amount)
                    }
                }
            }
        }

        class ChangeAddressButton(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.change_address_button)) {
            private val binding by viewBinding(ChangeAddressButtonBinding::bind)
            override fun bind(data: YourInfoModel, fragmentManager: FragmentManager) {
                binding.root.setHapticClickListener {
                    binding.root.context.startActivity(ChangeAddressActivity.newInstance(binding.root.context))
                }
            }
        }

        class PendingAddressChange(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.change_address_pending_change_card)) {
            private val binding by viewBinding(ChangeAddressPendingChangeCardBinding::bind)
            override fun bind(data: YourInfoModel, fragmentManager: FragmentManager) = with(binding) {
                if (data !is YourInfoModel.PendingAddressChange) {
                    invalid(data)
                } else {
                    continueButton.text = root.context.getString(R.string.home_tab_moving_action_sheet_open_offer_button)
                    continueButton.setHapticClickListener {
                        UpcomingChangeBottomSheet.newInstance(data.upcomingAgreement.table).show(
                            fragmentManager,
                            UpcomingChangeBottomSheet.TAG
                        )
                    }
                    paragraph.text = "Your insurance will be updated on ${data.upcomingAgreement.activeFrom} to your new home on ${data.upcomingAgreement.address}"
                }
            }
        }
    }
}
