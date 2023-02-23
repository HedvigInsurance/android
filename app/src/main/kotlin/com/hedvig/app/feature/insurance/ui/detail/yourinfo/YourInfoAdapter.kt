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
import com.hedvig.app.feature.home.ui.changeaddress.ChangeAddressActivity
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.invalid
import com.hedvig.app.util.extensions.startChat
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding

class YourInfoAdapter(
  private val fragmentManager: FragmentManager,
  private val openCancelInsuranceScreen: (insuranceId: String) -> Unit,
) : ListAdapter<YourInfoModel, YourInfoAdapter.ViewHolder>(GenericDiffUtilItemCallback()) {

  override fun getItemViewType(position: Int) = when (currentList[position]) {
    is YourInfoModel.ChangeAddressButton -> R.layout.change_address_button
    YourInfoModel.Change -> R.layout.your_info_change
    is YourInfoModel.PendingAddressChange -> R.layout.change_address_pending_change_card
    is YourInfoModel.CancelInsuranceButton -> R.layout.cancel_insurance_button
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
    R.layout.change_address_button -> ViewHolder.ChangeAddressButton(parent)
    R.layout.your_info_change -> ViewHolder.Change(parent)
    R.layout.change_address_pending_change_card -> ViewHolder.PendingAddressChange(parent)
    R.layout.cancel_insurance_button -> ViewHolder.CancelInsuranceButton(parent, openCancelInsuranceScreen)
    else -> throw Error("Invalid view type")
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(currentList[position], fragmentManager)
  }

  sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    abstract fun bind(data: YourInfoModel, fragmentManager: FragmentManager): Any?

    class Change(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.your_info_change)) {
      private val binding by viewBinding(YourInfoChangeBinding::bind)
      override fun bind(data: YourInfoModel, fragmentManager: FragmentManager) = with(binding) {
        openChatButton.setHapticClickListener {
          root.context.startChat()
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
          return invalid(data)
        }
        continueButton.text = root.context.getString(hedvig.resources.R.string.insurance_details_address_update_button)
        continueButton.setHapticClickListener {
          data.upcomingAgreement.table?.let {
            UpcomingChangeBottomSheet.newInstance(it).show(
              fragmentManager,
              UpcomingChangeBottomSheet.TAG,
            )
          }
        }
        paragraph.text = root.context.getString(
          hedvig.resources.R.string.insurance_details_adress_update_body_no_address,
          data.upcomingAgreement.activeFrom,
        )
      }
    }

    class CancelInsuranceButton(
      parent: ViewGroup,
      private val openCancelInsuranceScreen: (insuranceId: String) -> Unit,
    ) : ViewHolder(parent.inflate(R.layout.cancel_insurance_button)) {
      private val binding by viewBinding(ChangeAddressButtonBinding::bind)
      override fun bind(data: YourInfoModel, fragmentManager: FragmentManager) {
        require(data is YourInfoModel.CancelInsuranceButton)
        binding.root.setHapticClickListener {
          openCancelInsuranceScreen(data.insuranceId)
        }
      }
    }
  }
}
