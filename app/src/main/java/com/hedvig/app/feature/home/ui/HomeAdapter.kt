package com.hedvig.app.feature.home.ui

import android.graphics.drawable.PictureDrawable
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestBuilder
import com.hedvig.app.BuildConfig
import com.hedvig.app.R
import com.hedvig.app.databinding.GenericErrorBinding
import com.hedvig.app.databinding.HeaderItemLayoutBinding
import com.hedvig.app.databinding.HomeBigTextBinding
import com.hedvig.app.databinding.HomeBodyTextBinding
import com.hedvig.app.databinding.HomeChangeAddressBinding
import com.hedvig.app.databinding.HomeCommonClaimBinding
import com.hedvig.app.databinding.HomeInfoCardBinding
import com.hedvig.app.databinding.HomePsaBinding
import com.hedvig.app.databinding.HomeStartClaimContainedBinding
import com.hedvig.app.databinding.HomeStartClaimOutlinedBinding
import com.hedvig.app.databinding.HowClaimsWorkButtonBinding
import com.hedvig.app.databinding.UpcomingRenewalCardBinding
import com.hedvig.app.feature.claims.ui.commonclaim.CommonClaimActivity
import com.hedvig.app.feature.claims.ui.commonclaim.EmergencyActivity
import com.hedvig.app.feature.claims.ui.pledge.HonestyPledgeBottomSheet
import com.hedvig.app.feature.dismissiblepager.DismissiblePagerModel
import com.hedvig.app.feature.home.service.HomeTracker
import com.hedvig.app.feature.home.ui.HomeModel.HowClaimsWork
import com.hedvig.app.feature.home.ui.changeaddress.ChangeAddressActivity
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.apollo.ThemedIconUrls
import com.hedvig.app.util.extensions.canOpenUri
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.openUri
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding
import e
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit

class HomeAdapter(
    private val fragmentManager: FragmentManager,
    private val retry: () -> Unit,
    private val requestBuilder: RequestBuilder<PictureDrawable>,
    private val tracker: HomeTracker,
    private val marketManager: MarketManager,
) : ListAdapter<HomeModel, HomeAdapter.ViewHolder>(GenericDiffUtilItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.home_psa -> ViewHolder.PSABox(parent)
        R.layout.home_big_text -> ViewHolder.BigText(parent)
        R.layout.home_body_text -> ViewHolder.BodyText(parent)
        R.layout.home_start_claim_outlined -> ViewHolder.StartClaimOutlined(parent)
        R.layout.home_start_claim_contained -> ViewHolder.StartClaimContained(parent)
        R.layout.home_info_card -> ViewHolder.InfoCard(parent)
        R.layout.home_common_claim -> ViewHolder.CommonClaim(parent)
        R.layout.generic_error -> ViewHolder.Error(parent)
        R.layout.how_claims_work_button -> ViewHolder.HowClaimsWorkButton(parent)
        R.layout.upcoming_renewal_card -> ViewHolder.UpcomingRenewal(parent)
        R.layout.home_change_address -> ViewHolder.ChangeAddress(parent)
        R.layout.header_item_layout -> ViewHolder.Header(parent)
        else -> throw Error("Invalid view type")
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is HomeModel.BigText -> R.layout.home_big_text
        is HomeModel.BodyText -> R.layout.home_body_text
        HomeModel.StartClaimOutlined -> R.layout.home_start_claim_outlined
        HomeModel.StartClaimContained -> R.layout.home_start_claim_contained
        is HomeModel.ConnectPayin -> R.layout.home_info_card
        is HomeModel.CommonClaim -> R.layout.home_common_claim
        HomeModel.Error -> R.layout.generic_error
        is HomeModel.PSA -> R.layout.home_psa
        is HowClaimsWork -> R.layout.how_claims_work_button
        is HomeModel.UpcomingRenewal -> R.layout.upcoming_renewal_card
        is HomeModel.Header -> R.layout.header_item_layout
        is HomeModel.ChangeAddress -> R.layout.home_change_address
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(
            getItem(position),
            fragmentManager,
            retry,
            requestBuilder,
            tracker,
            marketManager
        )
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(
            data: HomeModel,
            fragmentManager: FragmentManager,
            retry: () -> Unit,
            requestBuilder: RequestBuilder<PictureDrawable>,
            tracker: HomeTracker,
            marketManager: MarketManager,
        ): Any?

        fun invalid(data: HomeModel) {
            e { "Invalid data passed to ${this.javaClass.name}::bind - type is ${data.javaClass.name}" }
        }

        class BigText(parent: ViewGroup) : ViewHolder(
            parent.inflate(
                R.layout.home_big_text
            )
        ) {
            private val binding by viewBinding(HomeBigTextBinding::bind)
            private val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
            override fun bind(
                data: HomeModel,
                fragmentManager: FragmentManager,
                retry: () -> Unit,
                requestBuilder: RequestBuilder<PictureDrawable>,
                tracker: HomeTracker,
                marketManager: MarketManager,
            ): Any? = with(binding) {
                if (data !is HomeModel.BigText) {
                    return invalid(data)
                }

                when (data) {
                    is HomeModel.BigText.Pending -> {
                        root.text = root.resources.getString(
                            R.string.home_tab_pending_unknown_title,
                            data.name
                        )
                    }
                    is HomeModel.BigText.ActiveInFuture -> {
                        root.text = root.resources.getString(
                            R.string.home_tab_active_in_future_welcome_title,
                            data.name,
                            formatter.format(data.inception)
                        )
                    }
                    is HomeModel.BigText.Active -> {
                        root.text =
                            root.resources.getString(R.string.home_tab_welcome_title, data.name)
                    }
                    is HomeModel.BigText.Terminated -> {
                        root.text =
                            root.resources.getString(
                                R.string.home_tab_terminated_welcome_title,
                                data.name
                            )
                    }
                }
            }
        }

        class BodyText(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.home_body_text)) {
            private val binding by viewBinding(HomeBodyTextBinding::bind)

            override fun bind(
                data: HomeModel,
                fragmentManager: FragmentManager,
                retry: () -> Unit,
                requestBuilder: RequestBuilder<PictureDrawable>,
                tracker: HomeTracker,
                marketManager: MarketManager,
            ): Any? = with(binding) {
                if (data !is HomeModel.BodyText) {
                    return invalid(data)
                }

                when (data) {
                    HomeModel.BodyText.Pending -> {
                        root.setText(R.string.home_tab_pending_unknown_body)
                    }
                    HomeModel.BodyText.ActiveInFuture -> {
                        root.setText(R.string.home_tab_active_in_future_body)
                    }
                    HomeModel.BodyText.Terminated -> {
                        root.setText(R.string.home_tab_terminated_body)
                    }
                }
            }
        }

        class StartClaimOutlined(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.home_start_claim_outlined)) {
            private val binding by viewBinding(HomeStartClaimOutlinedBinding::bind)
            override fun bind(
                data: HomeModel,
                fragmentManager: FragmentManager,
                retry: () -> Unit,
                requestBuilder: RequestBuilder<PictureDrawable>,
                tracker: HomeTracker,
                marketManager: MarketManager,
            ): Any? = with(binding) {
                if (data != HomeModel.StartClaimOutlined) {
                    return invalid(data)
                }

                root.setHapticClickListener {
                    tracker.startClaimOutlined()
                    HonestyPledgeBottomSheet().show(fragmentManager, HonestyPledgeBottomSheet.TAG)
                }
            }
        }

        class StartClaimContained(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.home_start_claim_contained)) {
            private val binding by viewBinding(HomeStartClaimContainedBinding::bind)
            override fun bind(
                data: HomeModel,
                fragmentManager: FragmentManager,
                retry: () -> Unit,
                requestBuilder: RequestBuilder<PictureDrawable>,
                tracker: HomeTracker,
                marketManager: MarketManager,
            ): Any? = with(binding) {
                if (data != HomeModel.StartClaimContained) {
                    return invalid(data)
                }

                root.setHapticClickListener {
                    tracker.startClaimContained()
                    HonestyPledgeBottomSheet().show(fragmentManager, HonestyPledgeBottomSheet.TAG)
                }
            }
        }

        class UpcomingRenewal(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.upcoming_renewal_card)) {
            private val binding by viewBinding(UpcomingRenewalCardBinding::bind)
            override fun bind(
                data: HomeModel,
                fragmentManager: FragmentManager,
                retry: () -> Unit,
                requestBuilder: RequestBuilder<PictureDrawable>,
                tracker: HomeTracker,
                marketManager: MarketManager,
            ): Any? = with(binding) {
                if (data !is HomeModel.UpcomingRenewal) {
                    return invalid(data)
                }
                val upcomingRenewal = data.upcomingRenewal
                body.text = body.context.getString(
                    R.string.DASHBOARD_RENEWAL_PROMPTER_BODY,
                    daysLeft(upcomingRenewal.renewalDate)
                )

                val maybeLinkUri = runCatching {
                    Uri.parse(upcomingRenewal.draftCertificateUrl)
                }
                action.setHapticClickListener {
                    tracker.showRenewal()
                    maybeLinkUri.getOrNull()?.let { uri ->
                        if (action.context.canOpenUri(uri)) {
                            action.context.openUri(uri)
                        }
                    }
                }
            }
        }

        class InfoCard(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.home_info_card)) {
            private val binding by viewBinding(HomeInfoCardBinding::bind)
            override fun bind(
                data: HomeModel,
                fragmentManager: FragmentManager,
                retry: () -> Unit,
                requestBuilder: RequestBuilder<PictureDrawable>,
                tracker: HomeTracker,
                marketManager: MarketManager,
            ) = with(binding) {
                if (data !is HomeModel.ConnectPayin) {
                    return invalid(data)
                }

                title.setText(R.string.info_card_missing_payment_title)
                body.setText(R.string.info_card_missing_payment_body)
                action.setText(R.string.info_card_missing_payment_button_text)
                action.setHapticClickListener {
                    tracker.addPaymentMethod()
                    marketManager.market?.connectPayin(action.context)
                        ?.let { action.context.startActivity(it) }
                }
            }
        }

        class PSABox(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.home_psa)) {
            private val binding by viewBinding(HomePsaBinding::bind)
            override fun bind(
                data: HomeModel,
                fragmentManager: FragmentManager,
                retry: () -> Unit,
                requestBuilder: RequestBuilder<PictureDrawable>,
                tracker: HomeTracker,
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

        class CommonClaim(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.home_common_claim)) {
            private val binding by viewBinding(HomeCommonClaimBinding::bind)
            override fun bind(
                data: HomeModel,
                fragmentManager: FragmentManager,
                retry: () -> Unit,
                requestBuilder: RequestBuilder<PictureDrawable>,
                tracker: HomeTracker,
                marketManager: MarketManager,
            ): Any? = with(binding) {
                if (data !is HomeModel.CommonClaim) {
                    return invalid(data)
                }

                when (data) {
                    is HomeModel.CommonClaim.Emergency -> {
                        label.text = data.inner.title
                        requestBuilder
                            .load(requestUri(data.inner.iconUrls))
                            .into(icon)
                        root.setHapticClickListener {
                            root.context.startActivity(
                                EmergencyActivity.newInstance(
                                    root.context,
                                    data.inner
                                )
                            )
                        }
                    }
                    is HomeModel.CommonClaim.TitleAndBulletPoints -> {
                        label.text = data.inner.title
                        requestBuilder
                            .load(requestUri(data.inner.iconUrls))
                            .into(icon)
                        root.setHapticClickListener {
                            root.context.startActivity(
                                CommonClaimActivity.newInstance(
                                    root.context,
                                    data.inner
                                )
                            )
                        }
                    }
                }
            }

            private fun requestUri(icons: ThemedIconUrls) = Uri.parse(
                "${BuildConfig.BASE_URL}${icons.iconByTheme(binding.root.context)}"
            )
        }

        class HowClaimsWorkButton(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.how_claims_work_button)) {
            private val binding by viewBinding(HowClaimsWorkButtonBinding::bind)
            override fun bind(
                data: HomeModel,
                fragmentManager: FragmentManager,
                retry: () -> Unit,
                requestBuilder: RequestBuilder<PictureDrawable>,
                tracker: HomeTracker,
                marketManager: MarketManager,
            ): Any? = with(binding) {
                if (data !is HowClaimsWork) {
                    return invalid(data)
                }
                val howClaimsWorkData = data.pages.mapIndexed { index, page ->
                    DismissiblePagerModel.NoTitlePage(
                        ThemedIconUrls.from(page.illustration.variants.fragments.iconVariantsFragment),
                        page.body,
                        button.context.getString(
                            if (index == data.pages.size - 1) {
                                R.string.claims_explainer_button_start_claim
                            } else {
                                R.string.claims_explainer_button_next
                            }
                        )
                    )
                }
                button.setHapticClickListener {
                    HowClaimsWorkDialog.newInstance(howClaimsWorkData)
                        .show(fragmentManager, HowClaimsWorkDialog.TAG)
                }
            }
        }

        class Error(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.generic_error)) {
            private val binding by viewBinding(GenericErrorBinding::bind)
            override fun bind(
                data: HomeModel,
                fragmentManager: FragmentManager,
                retry: () -> Unit,
                requestBuilder: RequestBuilder<PictureDrawable>,
                tracker: HomeTracker,
                marketManager: MarketManager,
            ): Any? = with(binding) {
                this.retry.setHapticClickListener {
                    tracker.retry()
                    retry()
                }
            }
        }

        class ChangeAddress(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.home_change_address)) {
            private val binding by viewBinding(HomeChangeAddressBinding::bind)
            override fun bind(
                data: HomeModel,
                fragmentManager: FragmentManager,
                retry: () -> Unit,
                requestBuilder: RequestBuilder<PictureDrawable>,
                tracker: HomeTracker,
                marketManager: MarketManager,
            ): Any? = with(binding) {
                changeAddressButton.setHapticClickListener {
                    root.context.startActivity(ChangeAddressActivity.newInstance(root.context))
                }
            }
        }

        class Header(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.header_item_layout)) {
            private val binding by viewBinding(HeaderItemLayoutBinding::bind)

            override fun bind(
                data: HomeModel,
                fragmentManager: FragmentManager,
                retry: () -> Unit,
                requestBuilder: RequestBuilder<PictureDrawable>,
                tracker: HomeTracker,
                marketManager: MarketManager,
            ): Any? = with(binding) {
                if (data !is HomeModel.Header) {
                    return invalid(data)
                }
                binding.headerItem.text = data.text
            }
        }
    }

    companion object {
        fun daysLeft(date: LocalDate) =
            ChronoUnit.DAYS.between(LocalDate.now(), date).toInt()
    }
}
