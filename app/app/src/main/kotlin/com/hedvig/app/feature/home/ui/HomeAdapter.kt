package com.hedvig.app.feature.home.ui

import android.net.Uri
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.market.MarketManager
import com.hedvig.app.R
import com.hedvig.app.databinding.ChangeAddressPendingChangeCardBinding
import com.hedvig.app.databinding.HomePsaBinding
import com.hedvig.app.databinding.HomeStartClaimContainedBinding
import com.hedvig.app.databinding.HomeStartClaimOutlinedBinding
import com.hedvig.app.databinding.UpcomingRenewalCardBinding
import com.hedvig.app.feature.home.model.HomeModel
import com.hedvig.app.feature.home.ui.connectpayincard.ConnectPayinCard
import com.hedvig.app.util.extensions.canOpenUri
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.invalid
import com.hedvig.app.util.extensions.openUri
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding
import com.hedvig.hanalytics.PaymentType
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class HomeAdapter(
  private val fragmentManager: FragmentManager,
  private val imageLoader: ImageLoader,
  private val marketManager: MarketManager,
  private val onClaimDetailCardClicked: (String) -> Unit,
  private val onClaimDetailCardShown: (String) -> Unit,
  private val onPaymentCardShown: () -> Unit,
  private val onPaymentCardClicked: (PaymentType) -> Unit,
  private val onStartClaimClicked: () -> Unit,
  private val onStartMovingFlow: () -> Unit,
) : ListAdapter<HomeModel, HomeAdapter.ViewHolder>(HomeModelDiffUtilItemCallback) {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
    R.layout.home_psa -> ViewHolder.PSABox(parent)
    R.layout.home_start_claim_outlined -> ViewHolder.StartClaimOutlined(parent, onStartClaimClicked)
    R.layout.home_start_claim_contained -> ViewHolder.StartClaimContained(parent, onStartClaimClicked)
    R.layout.upcoming_renewal_card -> ViewHolder.UpcomingRenewal(parent)
    R.layout.change_address_pending_change_card -> ViewHolder.PendingChange(parent, onStartMovingFlow)
    else -> throw Error("Invalid view type")
  }

  override fun getItemViewType(position: Int) = when (getItem(position)) {
    is HomeModel.StartClaimOutlined -> R.layout.home_start_claim_outlined
    is HomeModel.StartClaimContained -> R.layout.home_start_claim_contained
    is HomeModel.PSA -> R.layout.home_psa
    is HomeModel.UpcomingRenewal -> R.layout.upcoming_renewal_card
    is HomeModel.PendingAddressChange -> R.layout.change_address_pending_change_card
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(
      getItem(position),
      fragmentManager,
      marketManager,
    )
  }

  sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    abstract fun bind(
      data: HomeModel,
      fragmentManager: FragmentManager,
      marketManager: MarketManager,
    )

    class StartClaimOutlined(
      parent: ViewGroup,
      private val onStartClaimClicked: () -> Unit,
    ) : ViewHolder(parent.inflate(R.layout.home_start_claim_outlined)) {
      private val binding by viewBinding(HomeStartClaimOutlinedBinding::bind)

      override fun bind(
        data: HomeModel,
        fragmentManager: FragmentManager,
        marketManager: MarketManager,
      ) = with(binding) {
        if (data !is HomeModel.StartClaimOutlined) {
          return invalid(data)
        }

        binding.button.setText(data.textId)
        root.setHapticClickListener {
          onStartClaimClicked()
        }
      }
    }

    class StartClaimContained(
      parent: ViewGroup,
      private val onStartClaimClicked: () -> Unit,
    ) : ViewHolder(parent.inflate(R.layout.home_start_claim_contained)) {
      private val binding by viewBinding(HomeStartClaimContainedBinding::bind)
      override fun bind(
        data: HomeModel,
        fragmentManager: FragmentManager,
        marketManager: MarketManager,
      ) = with(binding) {
        if (data !is HomeModel.StartClaimContained) {
          return invalid(data)
        }

        binding.button.setText(data.textId)
        root.setHapticClickListener {
          onStartClaimClicked()
        }
      }
    }

    class UpcomingRenewal(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.upcoming_renewal_card)) {
      private val binding by viewBinding(UpcomingRenewalCardBinding::bind)
      override fun bind(
        data: HomeModel,
        fragmentManager: FragmentManager,
        marketManager: MarketManager,
      ) = with(binding) {
        if (data !is HomeModel.UpcomingRenewal) {
          return invalid(data)
        }
        val upcomingRenewal = data.upcomingRenewal
        title.text = title.context.getString(
          hedvig.resources.R.string.DASHBOARD_RENEWAL_PROMPTER_TITLE,
          data.contractDisplayName,
        )
        body.text = body.context.getString(
          hedvig.resources.R.string.DASHBOARD_RENEWAL_PROMPTER_BODY,
          daysLeft(upcomingRenewal.renewalDate),
        )

        val maybeLinkUri = runCatching {
          Uri.parse(upcomingRenewal.draftCertificateUrl)
        }
        action.setHapticClickListener {
          maybeLinkUri.getOrNull()?.let { uri ->
            if (action.context.canOpenUri(uri)) {
              action.context.openUri(uri)
            }
          }
        }
      }
    }

    class InfoCard(
      private val composeView: ComposeView,
      private val onPaymentCardShown: () -> Unit,
      private val onPaymentCardClicked: (PaymentType) -> Unit,
    ) : ViewHolder(composeView) {
      override fun bind(
        data: HomeModel,
        fragmentManager: FragmentManager,
        marketManager: MarketManager,
      ) {
        if (data !is HomeModel.ConnectPayin) {
          return invalid(data)
        }

        composeView.setContent {
          HedvigTheme {
            ConnectPayinCard(
              onActionClick = { onPaymentCardClicked(data.payinType) },
              onShown = onPaymentCardShown,
            )
          }
        }
      }
    }

    class PSABox(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.home_psa)) {
      private val binding by viewBinding(HomePsaBinding::bind)
      override fun bind(
        data: HomeModel,
        fragmentManager: FragmentManager,
        marketManager: MarketManager,
      ) = with(binding) {
        if (data !is HomeModel.PSA) {
          return invalid(data)
        }
        body.text = data.inner.message
        val uri = Uri.parse(data.inner.link)
        root.setHapticClickListener {
          if (arrow.context.canOpenUri(uri)) {
            arrow.context.openUri(uri)
          }
        }
      }
    }

    class PendingChange(parent: ViewGroup, val onStartMovingFlow: () -> Unit) :
      ViewHolder(parent.inflate(R.layout.change_address_pending_change_card)) {
      private val binding by viewBinding(ChangeAddressPendingChangeCardBinding::bind)
      override fun bind(
        data: HomeModel,
        fragmentManager: FragmentManager,
        marketManager: MarketManager,
      ) = with(binding) {
        if (data !is HomeModel.PendingAddressChange) {
          return invalid(data)
        }

        paragraph.text = root.context.getString(
          hedvig.resources.R.string.home_tab_moving_info_card_description,
          data.address,
        )
        continueButton.text = root.context.getString(hedvig.resources.R.string.home_tab_moving_info_card_button_text)
        continueButton.setHapticClickListener {
          onStartMovingFlow()
        }
      }
    }
  }

  companion object {
    fun daysLeft(date: LocalDate): Int = ChronoUnit.DAYS.between(LocalDate.now(), date).toInt()

    object HomeModelDiffUtilItemCallback : DiffUtil.ItemCallback<HomeModel>() {
      override fun areItemsTheSame(oldItem: HomeModel, newItem: HomeModel): Boolean {
        if (oldItem is HomeModel.ClaimStatus && newItem is HomeModel.ClaimStatus) {
          // Only a single ClaimStatus must appear in the list, therefore always true
          return true
        }
        return oldItem == newItem
      }

      override fun areContentsTheSame(oldItem: HomeModel, newItem: HomeModel) = oldItem == newItem
    }
  }
}
