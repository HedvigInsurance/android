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
    R.layout.upcoming_renewal_card -> ViewHolder.UpcomingRenewal(parent)
    else -> throw Error("Invalid view type")
  }

  override fun getItemViewType(position: Int) = when (getItem(position)) {
    is HomeModel.UpcomingRenewal -> R.layout.upcoming_renewal_card
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
