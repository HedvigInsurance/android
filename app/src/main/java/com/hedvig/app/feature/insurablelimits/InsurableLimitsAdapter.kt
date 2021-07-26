package com.hedvig.app.feature.insurablelimits

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.BASE_MARGIN_DOUBLE
import com.hedvig.app.BASE_MARGIN_SEPTUPLE
import com.hedvig.app.R
import com.hedvig.app.databinding.ContractCoverageDetailRowBinding
import com.hedvig.app.databinding.TextHeadline6Binding
import com.hedvig.app.feature.insurance.ui.detail.coverage.InsurableLimitsBottomSheet
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.invalid
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.updateMargin
import com.hedvig.app.util.extensions.viewBinding

class InsurableLimitsAdapter(
    private val fragmentManager: FragmentManager,
) : ListAdapter<InsurableLimitItem, InsurableLimitsAdapter.ViewHolder>(
    GenericDiffUtilItemCallback()
) {
    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is InsurableLimitItem.Header -> R.layout.text_headline6
        is InsurableLimitItem.InsurableLimit -> R.layout.contract_coverage_detail_row
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.text_headline6 -> ViewHolder.Header(parent)
        R.layout.contract_coverage_detail_row -> ViewHolder.InsurableLimit(parent, fragmentManager)
        else -> throw Error("Invalid viewType: $viewType")
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(data: InsurableLimitItem)

        class Header(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.text_headline6)) {
            private val binding by viewBinding(TextHeadline6Binding::bind)

            init {
                binding.root.updateMargin(
                    start = BASE_MARGIN_DOUBLE,
                    top = BASE_MARGIN_SEPTUPLE,
                    end = BASE_MARGIN_DOUBLE,
                    bottom = BASE_MARGIN_DOUBLE
                )
            }

            override fun bind(data: InsurableLimitItem) {
                if (data !is InsurableLimitItem.Header) {
                    return invalid(data)
                }
                binding.root.setText(
                    when (data) {
                        InsurableLimitItem.Header.Details -> R.string.moving_summary_scroll_Details
                        InsurableLimitItem.Header.MoreInfo -> R.string.CONTRACT_COVERAGE_MORE_INFO
                    }
                )
            }
        }

        class InsurableLimit(
            parent: ViewGroup,
            private val fragmentManager: FragmentManager,
        ) : ViewHolder(parent.inflate(R.layout.contract_coverage_detail_row)) {
            private val binding by viewBinding(ContractCoverageDetailRowBinding::bind)
            override fun bind(data: InsurableLimitItem) = with(binding) {
                if (data !is InsurableLimitItem.InsurableLimit) {
                    return invalid(data)
                }
                label.text = data.label
                content.text = data.limit
                info.setHapticClickListener {
                    InsurableLimitsBottomSheet
                        .newInstance(data.label, data.description)
                        .show(fragmentManager, InsurableLimitsBottomSheet.TAG)
                }
            }
        }
    }
}
