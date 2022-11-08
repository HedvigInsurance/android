package com.hedvig.app.feature.home.ui

import android.net.Uri
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.genericinfo.GenericErrorScreen
import com.hedvig.android.market.MarketManager
import com.hedvig.app.R
import com.hedvig.app.databinding.ChangeAddressPendingChangeCardBinding
import com.hedvig.app.databinding.HeaderItemLayoutBinding
import com.hedvig.app.databinding.HomeBigTextBinding
import com.hedvig.app.databinding.HomeBodyTextBinding
import com.hedvig.app.databinding.HomeChangeAddressButtonBinding
import com.hedvig.app.databinding.HomeCommonClaimBinding
import com.hedvig.app.databinding.HomePsaBinding
import com.hedvig.app.databinding.HomeStartClaimContainedBinding
import com.hedvig.app.databinding.HomeStartClaimOutlinedBinding
import com.hedvig.app.databinding.HowClaimsWorkButtonBinding
import com.hedvig.app.databinding.UpcomingRenewalCardBinding
import com.hedvig.app.feature.claimdetail.ClaimDetailActivity
import com.hedvig.app.feature.claims.ui.commonclaim.CommonClaimActivity
import com.hedvig.app.feature.claims.ui.commonclaim.EmergencyActivity
import com.hedvig.app.feature.dismissiblepager.DismissiblePagerModel
import com.hedvig.app.feature.home.model.HomeModel
import com.hedvig.app.feature.home.ui.changeaddress.ChangeAddressActivity
import com.hedvig.app.feature.home.ui.claimstatus.composables.ClaimStatusCards
import com.hedvig.app.feature.home.ui.connectpayincard.ConnectPayinCard
import com.hedvig.app.ui.coil.load
import com.hedvig.app.util.apollo.ThemedIconUrls
import com.hedvig.app.util.extensions.canOpenUri
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.invalid
import com.hedvig.app.util.extensions.openUri
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding
import com.hedvig.hanalytics.PaymentType
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit

class HomeAdapter(
  private val fragmentManager: FragmentManager,
  private val retry: () -> Unit,
  private val imageLoader: ImageLoader,
  private val marketManager: MarketManager,
  private val onClaimDetailCardClicked: (String) -> Unit,
  private val onClaimDetailCardShown: (String) -> Unit,
  private val onPaymentCardShown: () -> Unit,
  private val onPaymentCardClicked: (PaymentType) -> Unit,
  private val onStartClaimClicked: () -> Unit,
) : ListAdapter<HomeModel, HomeAdapter.ViewHolder>(HomeModelDiffUtilItemCallback) {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
    R.layout.home_psa -> ViewHolder.PSABox(parent)
    R.layout.home_big_text -> ViewHolder.BigText(parent)
    R.layout.home_body_text -> ViewHolder.BodyText(parent)
    ACTIVE_CLAIM -> ViewHolder.ClaimStatus(
      ComposeView(parent.context),
      onClaimDetailCardClicked,
      onClaimDetailCardShown,
    )
    SPACE -> ViewHolder.Space(ComposeView(parent.context))
    R.layout.home_start_claim_outlined -> ViewHolder.StartClaimOutlined(parent, onStartClaimClicked)
    R.layout.home_start_claim_contained -> ViewHolder.StartClaimContained(parent, onStartClaimClicked)
    CONNECT_PAYIN -> ViewHolder.InfoCard(ComposeView(parent.context), onPaymentCardShown, onPaymentCardClicked)
    R.layout.home_common_claim -> ViewHolder.CommonClaim(parent, imageLoader)
    ERROR -> ViewHolder.Error(ComposeView(parent.context), retry)
    R.layout.how_claims_work_button -> ViewHolder.HowClaimsWorkButton(parent)
    R.layout.upcoming_renewal_card -> ViewHolder.UpcomingRenewal(parent)
    R.layout.home_change_address_button -> ViewHolder.ChangeAddress(parent)
    R.layout.change_address_pending_change_card -> ViewHolder.PendingChange(parent)
    R.layout.header_item_layout -> ViewHolder.Header(parent)
    else -> throw Error("Invalid view type")
  }

  override fun getItemViewType(position: Int) = when (getItem(position)) {
    is HomeModel.BigText -> R.layout.home_big_text
    is HomeModel.BodyText -> R.layout.home_body_text
    is HomeModel.ClaimStatus -> ACTIVE_CLAIM
    is HomeModel.Space -> SPACE
    is HomeModel.StartClaimOutlined -> R.layout.home_start_claim_outlined
    is HomeModel.StartClaimContained -> R.layout.home_start_claim_contained
    is HomeModel.ConnectPayin -> CONNECT_PAYIN
    is HomeModel.CommonClaim -> R.layout.home_common_claim
    HomeModel.Error -> ERROR
    is HomeModel.PSA -> R.layout.home_psa
    is HomeModel.HowClaimsWork -> R.layout.how_claims_work_button
    is HomeModel.UpcomingRenewal -> R.layout.upcoming_renewal_card
    is HomeModel.Header -> R.layout.header_item_layout
    is HomeModel.ChangeAddress -> R.layout.home_change_address_button
    is HomeModel.PendingAddressChange -> R.layout.change_address_pending_change_card
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(
      getItem(position),
      fragmentManager,
      marketManager,
    )
  }

  override fun onViewRecycled(holder: ViewHolder) {
    val itemView = holder.itemView
    if (itemView is ComposeView) {
      itemView.disposeComposition()
    }
  }

  sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    abstract fun bind(
      data: HomeModel,
      fragmentManager: FragmentManager,
      marketManager: MarketManager,
    )

    class BigText(parent: ViewGroup) : ViewHolder(
      parent.inflate(
        R.layout.home_big_text,
      ),
    ) {
      private val binding by viewBinding(HomeBigTextBinding::bind)
      private val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
      override fun bind(
        data: HomeModel,
        fragmentManager: FragmentManager,
        marketManager: MarketManager,
      ) = with(binding) {
        if (data !is HomeModel.BigText) {
          return invalid(data)
        }

        val textRes = when (data) {
          is HomeModel.BigText.Pending -> root.resources.getString(
            hedvig.resources.R.string.home_tab_pending_unknown_title,
            data.name,
          )
          is HomeModel.BigText.ActiveInFuture -> root.resources.getString(
            hedvig.resources.R.string.home_tab_active_in_future_welcome_title,
            data.name,
            formatter.format(data.inception),
          )
          is HomeModel.BigText.Active -> root.resources.getString(
            hedvig.resources.R.string.home_tab_welcome_title,
            data.name,
          )
          is HomeModel.BigText.Terminated -> root.resources.getString(
            hedvig.resources.R.string.home_tab_terminated_welcome_title,
            data.name,
          )
          is HomeModel.BigText.Switching -> root.resources.getString(
            hedvig.resources.R.string.home_tab_pending_switchable_welcome_title,
            data.name,
          )
        }
        root.text = textRes
      }
    }

    class BodyText(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.home_body_text)) {
      private val binding by viewBinding(HomeBodyTextBinding::bind)

      override fun bind(
        data: HomeModel,
        fragmentManager: FragmentManager,
        marketManager: MarketManager,
      ) = with(binding) {
        if (data !is HomeModel.BodyText) {
          return invalid(data)
        }

        val textRes = when (data) {
          HomeModel.BodyText.Pending -> hedvig.resources.R.string.home_tab_pending_unknown_body
          HomeModel.BodyText.ActiveInFuture -> hedvig.resources.R.string.home_tab_active_in_future_body
          HomeModel.BodyText.Terminated -> hedvig.resources.R.string.home_tab_terminated_body
          HomeModel.BodyText.Switching -> hedvig.resources.R.string.home_tab_pending_switchable_body
        }
        root.setText(textRes)
      }
    }

    class ClaimStatus(
      val composeView: ComposeView,
      private val onClaimDetailCardClicked: (String) -> Unit,
      private val onClaimDetailCardShown: (String) -> Unit,
    ) : ViewHolder(composeView) {
      init {
        composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
      }

      private fun goToClaimDetailScreen(claimId: String) {
        onClaimDetailCardClicked(claimId)
        composeView.context.startActivity(
          ClaimDetailActivity.newInstance(composeView.context, claimId),
        )
      }

      override fun bind(
        data: HomeModel,
        fragmentManager: FragmentManager,
        marketManager: MarketManager,
      ) {
        if (data !is HomeModel.ClaimStatus) {
          return invalid(data)
        }

        composeView.setContent {
          HedvigTheme {
            ClaimStatusCards(
              goToDetailScreen = ::goToClaimDetailScreen,
              onClaimCardShown = onClaimDetailCardShown,
              claimStatusCardsUiState = data.claimStatusCardsUiState,
            )
          }
        }
      }
    }

    class Space(
      private val composeView: ComposeView,
    ) : ViewHolder(composeView) {
      init {
        composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
      }

      override fun bind(
        data: HomeModel,
        fragmentManager: FragmentManager,
        marketManager: MarketManager,
      ) {
        if (data !is HomeModel.Space) return invalid(data)
        composeView.setContent {
          Spacer(Modifier.height(data.height))
        }
      }
    }

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
      val composeView: ComposeView,
      private val onPaymentCardShown: () -> Unit,
      private val onPaymentCardClicked: (PaymentType) -> Unit,
    ) : ViewHolder(composeView) {

      init {
        composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
      }

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

    class CommonClaim(
      parent: ViewGroup,
      private val imageLoader: ImageLoader,
    ) : ViewHolder(parent.inflate(R.layout.home_common_claim)) {
      private val binding by viewBinding(HomeCommonClaimBinding::bind)
      override fun bind(
        data: HomeModel,
        fragmentManager: FragmentManager,
        marketManager: MarketManager,
      ) = with(binding) {
        if (data !is HomeModel.CommonClaim) {
          return invalid(data)
        }

        when (data) {
          is HomeModel.CommonClaim.Emergency -> {
            label.text = data.inner.title
            icon.load(requestUri(data.inner.iconUrls), imageLoader)
            root.setHapticClickListener {
              root.context.startActivity(
                EmergencyActivity.newInstance(
                  root.context,
                  data.inner,
                ),
              )
            }
          }
          is HomeModel.CommonClaim.TitleAndBulletPoints -> {
            label.text = data.inner.title
            icon.load(requestUri(data.inner.iconUrls), imageLoader)
            root.setHapticClickListener {
              root.context.startActivity(
                CommonClaimActivity.newInstance(
                  root.context,
                  data.inner,
                ),
              )
            }
          }
        }
      }

      private fun requestUri(icons: ThemedIconUrls) = Uri.parse(icons.iconByTheme(binding.root.context))
    }

    class HowClaimsWorkButton(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.how_claims_work_button)) {
      private val binding by viewBinding(HowClaimsWorkButtonBinding::bind)
      override fun bind(
        data: HomeModel,
        fragmentManager: FragmentManager,
        marketManager: MarketManager,
      ) = with(binding) {
        if (data !is HomeModel.HowClaimsWork) {
          return invalid(data)
        }
        val howClaimsWorkData = data.pages.mapIndexed { index, page ->
          DismissiblePagerModel.NoTitlePage(
            ThemedIconUrls.from(page.illustration.variants.fragments.iconVariantsFragment),
            page.body,
            button.context.getString(
              if (index == data.pages.size - 1) {
                hedvig.resources.R.string.claims_explainer_button_start_claim
              } else {
                hedvig.resources.R.string.claims_explainer_button_next
              },
            ),
          )
        }
        button.setHapticClickListener {
          HowClaimsWorkDialog.newInstance(howClaimsWorkData)
            .show(fragmentManager, HowClaimsWorkDialog.TAG)
        }
      }
    }

    class Error(
      val composeView: ComposeView,
      private val retry: () -> Unit,
    ) : ViewHolder(composeView) {

      init {
        composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
      }

      override fun bind(
        data: HomeModel,
        fragmentManager: FragmentManager,
        marketManager: MarketManager,
      ) {
        composeView.setContent {
          HedvigTheme {
            GenericErrorScreen(
              onRetryButtonClick = { retry() },
              Modifier
                .padding(16.dp)
                .padding(top = (80 - 16).dp),
            )
          }
        }
      }
    }

    class ChangeAddress(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.home_change_address_button)) {
      private val binding by viewBinding(HomeChangeAddressButtonBinding::bind)
      override fun bind(
        data: HomeModel,
        fragmentManager: FragmentManager,
        marketManager: MarketManager,
      ) = with(binding) {
        if (data !is HomeModel.ChangeAddress) {
          invalid(data)
        } else {
          title.setHapticClickListener {
            root.context.startActivity(ChangeAddressActivity.newInstance(root.context))
          }
        }
      }
    }

    class PendingChange(parent: ViewGroup) :
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
          root.context.startActivity(ChangeAddressActivity.newInstance(binding.root.context))
        }
      }
    }

    class Header(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.header_item_layout)) {
      private val binding by viewBinding(HeaderItemLayoutBinding::bind)

      override fun bind(
        data: HomeModel,
        fragmentManager: FragmentManager,
        marketManager: MarketManager,
      ) = with(binding) {
        if (data !is HomeModel.Header) {
          return invalid(data)
        }
        binding.headerItem.text = itemView.context.getString(data.stringRes)
      }
    }
  }

  companion object {
    const val ACTIVE_CLAIM = 1
    const val CONNECT_PAYIN = 2
    const val SPACE = 3
    const val ERROR = 4

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
