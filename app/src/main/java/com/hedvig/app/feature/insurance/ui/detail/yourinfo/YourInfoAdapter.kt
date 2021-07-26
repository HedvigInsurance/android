package com.hedvig.app.feature.insurance.ui.detail.yourinfo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.databinding.AdditionalBuildingsRowBinding
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
                    return invalid(data)
                }
                when (data) {
                    is YourInfoModel.Home.Apartment -> {
                        setHouseSectionVisibility(false)
                        bindCommon(
                            data.street,
                            data.postalCode,
                            data.type,
                            data.size,
                        )
                    }
                    is YourInfoModel.Home.House -> {
                        setHouseSectionVisibility(true)
                        bindCommon(
                            data.street,
                            data.postalCode,
                            data.type,
                            data.size,
                        )
                        bindHouse(
                            data.ancillaryArea,
                            data.yearOfConstruction,
                            data.numberOfBathrooms,
                            data.isPartlySubleted,
                            data.extraBuildings,
                        )
                    }
                }
            }

            private fun bindCommon(
                address: String,
                postalCode: String,
                @StringRes type: Int?,
                size: Int,
            ) = with(binding) {
                addressValue.text = address
                postcodeValue.text = postalCode
                typeValue.text = type?.let(root.context::getString)
                sizeValue.text = root.context.getString(
                    R.string.CONTRACT_DETAIL_HOME_SIZE_INPUT, size
                )
            }

            private fun bindHouse(
                ancillaryArea: Int,
                yearBuilt: Int,
                numberOfBaths: Int,
                isPartlySubleted: Boolean,
                extraBuildings: List<Triple<String, Int, Boolean>>,
            ) = with(binding) {
                ancillaryAreaValue.text = ancillaryAreaValue.context.getString(
                    R.string.HOUSE_INFO_BIYTA_SQUAREMETERS,
                    ancillaryArea,
                )
                yearBuiltValue.text = yearBuilt.toString()
                numberOfBathsValue.text = numberOfBaths.toString()
                partlySubletedValue.setText(
                    if (isPartlySubleted) {
                        R.string.HOUSE_INFO_SUBLETED_TRUE
                    } else {
                        R.string.HOUSE_INFO_SUBLETED_FALSE
                    }
                )
                extraBuildingsTitle.isVisible = extraBuildings.isNotEmpty()
                extraBuildingsContainer.isVisible = extraBuildings.isNotEmpty()
                extraBuildingsContainer.removeAllViews()

                val inflater = LayoutInflater.from(extraBuildingsContainer.context)

                extraBuildings.forEach { (displayValue, area, hasWaterConnected) ->
                    val extraBuilding = AdditionalBuildingsRowBinding.inflate(
                        inflater,
                        extraBuildingsContainer,
                        false
                    )

                    extraBuilding.title.text = displayValue
                    extraBuilding.body.text = buildString {
                        append(
                            extraBuilding.body.context.getString(
                                R.string.HOUSE_INFO_BOYTA_SQUAREMETERS,
                                area,
                            )
                        )

                        if (hasWaterConnected) {
                            append(", ")
                            append(extraBuilding.body.context.getString(R.string.HOUSE_INFO_CONNECTED_WATER))
                        }
                    }
                    extraBuildingsContainer.addView(extraBuilding.root)
                }
            }

            private fun setHouseSectionVisibility(visible: Boolean) = with(binding) {
                ancillaryAreaContainer.isVisible = visible
                yearBuiltContainer.isVisible = visible
                numberOfBathsContainer.isVisible = visible
                partlySubletedContainer.isVisible = visible
                extraBuildingsTitle.isVisible = visible
                extraBuildingsContainer.isVisible = visible
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

        class PendingAddressChange(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.change_address_pending_change_card)) {
            private val binding by viewBinding(ChangeAddressPendingChangeCardBinding::bind)
            override fun bind(data: YourInfoModel, fragmentManager: FragmentManager) = with(binding) {
                if (data !is YourInfoModel.PendingAddressChange) {
                    invalid(data)
                } else {
                    continueButton.text = root.context.getString(R.string.insurance_details_address_update_button)
                    continueButton.setHapticClickListener {
                        data.upcomingAgreement.table?.let {
                            UpcomingChangeBottomSheet.newInstance(it).show(
                                fragmentManager,
                                UpcomingChangeBottomSheet.TAG
                            )
                        }
                    }
                    paragraph.text = root.context.getString(
                        R.string.insurance_details_address_update_body,
                        data.upcomingAgreement.activeFrom,
                        data.upcomingAgreement.address
                    )
                }
            }
        }
    }
}
