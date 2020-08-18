package com.hedvig.app.feature.home.ui

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.databinding.HomeBigTextBinding
import com.hedvig.app.databinding.HomeBodyTextBinding
import com.hedvig.app.databinding.HomeCommonClaimBinding
import com.hedvig.app.databinding.HomeErrorBinding
import com.hedvig.app.databinding.HomeStartClaimContainedBinding
import com.hedvig.app.databinding.HomeStartClaimOutlinedBinding
import com.hedvig.app.feature.claims.ui.pledge.HonestyPledgeBottomSheet
import com.hedvig.app.util.GenericDiffUtilCallback
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding
import e
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class HomeAdapter(
    private val fragmentManager: FragmentManager,
    private val retry: () -> Unit
) : RecyclerView.Adapter<HomeAdapter.ViewHolder>() {
    var items: List<HomeModel> = emptyList()
        set(value) {
            val diff = DiffUtil.calculateDiff(
                GenericDiffUtilCallback(
                    field,
                    value
                )
            )
            field = value
            diff.dispatchUpdatesTo(this)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.home_big_text -> ViewHolder.BigText(parent)
        R.layout.home_body_text -> ViewHolder.BodyText(parent)
        R.layout.home_start_claim_outlined -> ViewHolder.StartClaimOutlined(parent)
        R.layout.home_start_claim_contained -> ViewHolder.StartClaimContained(parent)
        R.layout.home_common_claim_title -> ViewHolder.CommonClaimTitle(parent)
        R.layout.home_common_claim -> ViewHolder.CommonClaim(parent)
        R.layout.home_error -> ViewHolder.Error(parent)
        else -> throw Error("Invalid view type")
    }

    override fun getItemCount() = items.size
    override fun getItemViewType(position: Int) = when (items[position]) {
        is HomeModel.BigText -> R.layout.home_big_text
        is HomeModel.BodyText -> R.layout.home_body_text
        HomeModel.StartClaimOutlined -> R.layout.home_start_claim_outlined
        HomeModel.StartClaimContained -> R.layout.home_start_claim_contained
        HomeModel.CommonClaimTitle -> R.layout.home_common_claim_title
        is HomeModel.CommonClaim -> R.layout.home_common_claim
        HomeModel.Error -> R.layout.home_error
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], fragmentManager, retry)
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(
            data: HomeModel,
            fragmentManager: FragmentManager,
            retry: () -> Unit
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
                retry: () -> Unit
            ) = with(binding) {
                if (data !is HomeModel.BigText) {
                    return invalid(data)
                }

                when (data) {
                    is HomeModel.BigText.Pending -> {
                        root.text = root.resources.getString(
                            R.string.home_tab_pending_nonswitchable_welcome_title,
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
                retry: () -> Unit
            ): Any? = with(binding) {
                if (data !is HomeModel.BodyText) {
                    return invalid(data)
                }

                when (data) {
                    HomeModel.BodyText.Pending -> {
                        root.setText(R.string.home_tab_pending_nonswitchable_body)
                    }
                    HomeModel.BodyText.ActiveInFuture -> {
                        root.setText(R.string.home_tab_active_in_future_body)
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
                retry: () -> Unit
            ) = with(binding) {
                if (data != HomeModel.StartClaimOutlined) {
                    return invalid(data)
                }

                root.setHapticClickListener {
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
                retry: () -> Unit
            ) = with(binding) {
                if (data != HomeModel.StartClaimContained) {
                    return invalid(data)
                }

                root.setHapticClickListener {
                    HonestyPledgeBottomSheet().show(fragmentManager, HonestyPledgeBottomSheet.TAG)
                }
            }
        }

        class CommonClaimTitle(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.home_common_claim_title)) {
            override fun bind(
                data: HomeModel,
                fragmentManager: FragmentManager,
                retry: () -> Unit
            ) = Unit
        }

        class CommonClaim(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.home_common_claim)) {
            private val binding by viewBinding(HomeCommonClaimBinding::bind)
            override fun bind(
                data: HomeModel,
                fragmentManager: FragmentManager,
                retry: () -> Unit
            ) = with(binding) {
                if (data !is HomeModel.CommonClaim) {
                    return invalid(data)
                }

                when (data) {
                    // TODO, both: Add icon
                    is HomeModel.CommonClaim.Emergency -> {
                        label.text = data.title
                    }
                    is HomeModel.CommonClaim.TitleAndBulletPoints -> {
                        label.text = data.inner.title
                    }
                }
            }
        }

        class Error(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.home_error)) {
            private val binding by viewBinding(HomeErrorBinding::bind)
            override fun bind(
                data: HomeModel,
                fragmentManager: FragmentManager,
                retry: () -> Unit
            ) = with(binding) {
                this.retry.setHapticClickListener { retry() }
            }
        }
    }
}
