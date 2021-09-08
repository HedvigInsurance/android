package com.hedvig.app.feature.perils

import android.content.Context
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.load
import com.carousell.concatadapterextension.ItemDecorationOwner
import com.carousell.concatadapterextension.SpanSizeLookupOwner
import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.app.BASE_MARGIN_DOUBLE
import com.hedvig.app.BASE_MARGIN_HALF
import com.hedvig.app.R
import com.hedvig.app.databinding.ContractDetailCoverageHeaderBinding
import com.hedvig.app.databinding.PerilDetailBinding
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.invalid
import com.hedvig.app.util.extensions.isDarkThemeActive
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding

class PerilsAdapter(
    private val fragmentManager: FragmentManager,
    private val imageLoader: ImageLoader,
) : ListAdapter<PerilItem, PerilsAdapter.ViewHolder>(GenericDiffUtilItemCallback()),
    SpanSizeLookupOwner,
    ItemDecorationOwner {

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is PerilItem.Header -> R.layout.contract_detail_coverage_header
        is PerilItem.Peril -> R.layout.peril_detail
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.contract_detail_coverage_header -> ViewHolder.Header(parent)
        R.layout.peril_detail -> ViewHolder.Peril(parent, imageLoader)
        else -> throw Error("Invalid viewType: $viewType")
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), fragmentManager)
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(
            data: PerilItem,
            fragmentManager: FragmentManager,
        ): Any?

        class Header(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.contract_detail_coverage_header)) {
            private val binding by viewBinding(ContractDetailCoverageHeaderBinding::bind)
            override fun bind(
                data: PerilItem,
                fragmentManager: FragmentManager,
            ) = with(binding) {
                if (data !is PerilItem.Header) {
                    return invalid(data)
                }

                with(binding.root) {
                    text =
                        context.getString(
                            R.string.CONTRACT_COVERAGE_CONTRACT_TYPE,
                            data.typeOfContract.displayNameDefinite(context)
                        )
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
                    TypeOfContract.NO_TRAVEL_YOUTH,
                    TypeOfContract.DK_TRAVEL,
                    TypeOfContract.DK_TRAVEL_STUDENT,
                    -> context.getString(
                        R.string.INSURANCE_TYPE_TRAVEL_DEFINITE
                    )
                    TypeOfContract.DK_ACCIDENT,
                    TypeOfContract.DK_ACCIDENT_STUDENT,
                    -> context.getString(R.string.PLACEHOLDER_CONTRACT_DISPLAY_NAME_DK_HOME_CONTENTS)
                    else -> ""
                }
            }
        }

        class Peril(parent: ViewGroup, private val imageLoader: ImageLoader) :
            ViewHolder(parent.inflate(R.layout.peril_detail)) {
            private val binding by viewBinding(PerilDetailBinding::bind)
            override fun bind(
                data: PerilItem,
                fragmentManager: FragmentManager,
            ) = with(binding) {
                if (data !is PerilItem.Peril) {
                    return invalid(data)
                }

                label.text = data.inner.title
                val iconUrl = "${icon.context.getString(R.string.BASE_URL)}${
                if (icon.context.isDarkThemeActive) {
                    data.inner.darkUrl
                } else {
                    data.inner.lightUrl
                }
                }"
                icon.load(iconUrl, imageLoader) {
                    crossfade(true)
                }

                root.setHapticClickListener {
                    PerilBottomSheet
                        .newInstance(data.inner)
                        .show(
                            fragmentManager,
                            PerilBottomSheet.TAG
                        )
                }
            }
        }
    }

    override fun getSpanSizeLookup() = object : GridLayoutManager.SpanSizeLookup() {
        override fun getSpanSize(position: Int) = when (currentList[position]) {
            is PerilItem.Peril -> 1
            else -> 2
        }
    }

    override fun getItemDecorations(): List<RecyclerView.ItemDecoration> = listOf(
        object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                val position = parent.getChildViewHolder(view).bindingAdapterPosition
                if (currentList[position] is PerilItem.Peril) {
                    val spanIndex =
                        (view.layoutParams as? GridLayoutManager.LayoutParams)?.spanIndex ?: return

                    when (spanIndex) {
                        SPAN_LEFT -> {
                            outRect.right = BASE_MARGIN_HALF
                            outRect.left = BASE_MARGIN_DOUBLE
                        }
                        SPAN_RIGHT -> {
                            outRect.left = BASE_MARGIN_HALF
                            outRect.right = BASE_MARGIN_DOUBLE
                        }
                    }
                }
            }
        }
    )

    companion object {
        private const val SPAN_LEFT = 0
        private const val SPAN_RIGHT = 1
    }
}
