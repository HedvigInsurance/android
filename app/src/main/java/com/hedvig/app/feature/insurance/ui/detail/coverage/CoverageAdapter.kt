package com.hedvig.app.feature.insurance.ui.detail.coverage

import android.content.Context
import android.graphics.drawable.PictureDrawable
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestBuilder
import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.app.R
import com.hedvig.app.databinding.ContractCoverageDetailRowBinding
import com.hedvig.app.databinding.ContractDetailCoverageHeaderBinding
import com.hedvig.app.databinding.PerilDetailBinding
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.isDarkThemeActive
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding
import e

class CoverageAdapter(
    private val requestBuilder: RequestBuilder<PictureDrawable>,
    private val fragmentManager: FragmentManager
) :
    ListAdapter<CoverageModel, CoverageAdapter.ViewHolder>(GenericDiffUtilItemCallback()) {

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is CoverageModel.Header -> R.layout.contract_detail_coverage_header
        is CoverageModel.Peril -> R.layout.peril_detail
        is CoverageModel.InsurableLimit -> R.layout.contract_coverage_detail_row
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.contract_detail_coverage_header -> ViewHolder.Header(parent)
        R.layout.peril_detail -> ViewHolder.Peril(parent)
        R.layout.contract_coverage_detail_row -> ViewHolder.InsurableLimit(parent)
        else -> throw Error("Invalid viewType: $viewType")
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), requestBuilder, fragmentManager)
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(
            data: CoverageModel,
            requestBuilder: RequestBuilder<PictureDrawable>,
            fragmentManager: FragmentManager
        ): Any?

        fun invalid(data: CoverageModel) {
            e { "Invalid data passed to ${this.javaClass.name}::bind - type is ${data.javaClass.name}" }
        }

        class Header(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.contract_detail_coverage_header)) {
            private val binding by viewBinding(ContractDetailCoverageHeaderBinding::bind)
            override fun bind(
                data: CoverageModel,
                requestBuilder: RequestBuilder<PictureDrawable>,
                fragmentManager: FragmentManager
            ) = with(binding) {
                if (data !is CoverageModel.Header) {
                    return invalid(data)
                }

                with(binding.root) {
                    when (data) {
                        is CoverageModel.Header.Perils -> text =
                            context.getString(
                                R.string.CONTRACT_COVERAGE_CONTRACT_TYPE,
                                data.typeOfContract.displayNameDefinite(context)
                            )
                        CoverageModel.Header.InsurableLimits -> setText(
                            R.string.CONTRACT_COVERAGE_MORE_INFO
                        )
                    }
                }
            }

            companion object {
                private fun TypeOfContract.displayNameDefinite(context: Context) = when (this) {
                    TypeOfContract.SE_HOUSE,
                    TypeOfContract.SE_APARTMENT_BRF,
                    TypeOfContract.SE_APARTMENT_RENT,
                    TypeOfContract.SE_APARTMENT_STUDENT_BRF,
                    TypeOfContract.SE_APARTMENT_STUDENT_RENT,
                    TypeOfContract.NO_HOME_CONTENT_OWN,
                    TypeOfContract.NO_HOME_CONTENT_RENT,
                    TypeOfContract.NO_HOME_CONTENT_YOUTH_OWN,
                    TypeOfContract.NO_HOME_CONTENT_YOUTH_RENT,
                    TypeOfContract.DK_HOME_CONTENT_OWN,
                    TypeOfContract.DK_HOME_CONTENT_RENT,
                    TypeOfContract.DK_HOME_CONTENT_STUDENT_OWN,
                    TypeOfContract.DK_HOME_CONTENT_STUDENT_RENT,
                    -> context.getString(R.string.INSURANCE_TYPE_HOME_DEFINITE)
                    TypeOfContract.NO_TRAVEL,
                    TypeOfContract.NO_TRAVEL_YOUTH -> context.getString(R.string.INSURANCE_TYPE_TRAVEL_DEFINITE)
                    TypeOfContract.UNKNOWN__ -> ""
                }
            }
        }

        class Peril(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.peril_detail)) {
            private val binding by viewBinding(PerilDetailBinding::bind)
            override fun bind(
                data: CoverageModel,
                requestBuilder: RequestBuilder<PictureDrawable>,
                fragmentManager: FragmentManager
            ): Any? = with(binding) {
                if (data !is CoverageModel.Peril) {
                    return invalid(data)
                }


                label.text = data.inner.title
                val iconUrl = "${com.hedvig.app.BuildConfig.BASE_URL}${
                    if (icon.context.isDarkThemeActive) {
                        data.inner.icon.variants.dark.svgUrl
                    } else {
                        data.inner.icon.variants.light.svgUrl
                    }
                }"
                requestBuilder
                    .load(iconUrl)
                    .into(icon)

                root.setHapticClickListener {
                    PerilBottomSheet.newInstance(
                        root.context,
                        data.inner
                    )
                        .show(
                            fragmentManager,
                            PerilBottomSheet.TAG
                        )
                }
            }
        }

        class InsurableLimit(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.contract_coverage_detail_row)) {
            private val binding by viewBinding(ContractCoverageDetailRowBinding::bind)
            override fun bind(
                data: CoverageModel,
                requestBuilder: RequestBuilder<PictureDrawable>,
                fragmentManager: FragmentManager
            ) = with(binding) {
                if (data !is CoverageModel.InsurableLimit) {
                    return invalid(data)
                }
                label.text = data.inner.label
                content.text = data.inner.limit
                info.setHapticClickListener {
                    InsurableLimitsBottomSheet.newInstance(data.inner)
                        .show(fragmentManager, InsurableLimitsBottomSheet.TAG)
                }
            }
        }
    }
}
