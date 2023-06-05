package com.hedvig.app.feature.perils

import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import com.carousell.concatadapterextension.ItemDecorationOwner
import com.carousell.concatadapterextension.SpanSizeLookupOwner
import com.hedvig.android.core.common.android.GenericDiffUtilItemCallback
import com.hedvig.android.core.common.android.isDarkThemeActive
import com.hedvig.app.BASE_MARGIN_DOUBLE
import com.hedvig.app.BASE_MARGIN_HALF
import com.hedvig.app.R
import com.hedvig.app.databinding.ContractDetailCoverageHeaderBinding
import com.hedvig.app.databinding.PerilDetailBinding
import com.hedvig.app.ui.coil.load
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.invalid
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding
import com.hedvig.app.util.extensions.viewDps

class PerilsAdapter(
  private val fragmentManager: FragmentManager,
  private val imageLoader: ImageLoader,
) : ListAdapter<PerilItem, PerilsAdapter.ViewHolder>(GenericDiffUtilItemCallback()),
  SpanSizeLookupOwner,
  ItemDecorationOwner {

  override fun getItemViewType(position: Int): Int = when (getItem(position)) {
    is PerilItem.Header -> R.layout.contract_detail_coverage_header
    is PerilItem.Peril -> R.layout.peril_detail
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
    R.layout.contract_detail_coverage_header -> ViewHolder.Header(parent)
    R.layout.peril_detail -> ViewHolder.Peril(parent, fragmentManager, imageLoader)
    else -> throw Error("Invalid viewType: $viewType")
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(getItem(position))
  }

  sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    abstract fun bind(data: PerilItem)

    class Header(
      parent: ViewGroup,
    ) : ViewHolder(parent.inflate(R.layout.contract_detail_coverage_header)) {
      private val binding by viewBinding(ContractDetailCoverageHeaderBinding::bind)
      override fun bind(data: PerilItem) {
        if (data !is PerilItem.Header) {
          return invalid(data)
        }

        with(binding.root) {
          text = when (data) {
            is PerilItem.Header.CoversSuffix -> {
              context.getString(
                hedvig.resources.R.string.CONTRACT_COVERAGE_CONTRACT_TYPE,
                data.displayName,
              )
            }

            is PerilItem.Header.Simple -> {
              data.displayName
            }
          }
        }
      }
    }

    class Peril(
      parent: ViewGroup,
      private val fragmentManager: FragmentManager,
      private val imageLoader: ImageLoader,
    ) : ViewHolder(parent.inflate(R.layout.peril_detail)) {
      private val binding by viewBinding(PerilDetailBinding::bind)

      override fun bind(data: PerilItem) {
        if (data !is PerilItem.Peril) {
          return invalid(data)
        }

        binding.label.text = data.inner.title
        if (data.inner.colorCode != null) {
          val shape = ShapeDrawable(OvalShape())
          shape.setTint(Color.parseColor(data.inner.colorCode))
          shape.intrinsicHeight = 16.viewDps
          shape.intrinsicWidth = 16.viewDps
          binding.icon.setImageDrawable(shape)
        } else {
          val iconUrl = if (binding.icon.context.isDarkThemeActive) {
            data.inner.darkUrl
          } else {
            data.inner.lightUrl
          }
          binding.icon.load(iconUrl, imageLoader) {
            crossfade(true)
          }
        }

        binding.root.setHapticClickListener {
          PerilBottomSheet
            .newInstance(data.inner)
            .show(
              fragmentManager,
              PerilBottomSheet.TAG,
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
    },
  )

  companion object {
    private const val SPAN_LEFT = 0
    private const val SPAN_RIGHT = 1
  }
}
