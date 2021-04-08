package com.hedvig.app.feature.embark.ui

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.databinding.TooltipHeaderBinding
import com.hedvig.app.databinding.TooltipWithTitleBinding
import com.hedvig.app.databinding.TooltipWithoutTitleBinding
import com.hedvig.app.feature.embark.TooltipModel
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.invalid
import com.hedvig.app.util.extensions.viewBinding

class TooltipBottomSheetAdapter :
    ListAdapter<TooltipModel, TooltipBottomSheetAdapter.ViewHolder>(GenericDiffUtilItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.tooltip_header -> ViewHolder.Information(parent)
        R.layout.tooltip_with_title -> ViewHolder.TooltipWithTitle(parent)
        R.layout.tooltip_without_title -> ViewHolder.TooltipWithoutTitle(parent)
        else -> throw Error("Invalid viewType: $viewType")
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is TooltipModel.Header -> R.layout.tooltip_header
        is TooltipModel.Tooltip.TooltipWithTitle -> R.layout.tooltip_with_title
        is TooltipModel.Tooltip.TooltipWithOutTitle -> R.layout.tooltip_without_title
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        abstract fun bind(item: TooltipModel)

        class Information(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.tooltip_header)) {
            private val binding by viewBinding(TooltipHeaderBinding::bind)
            override fun bind(item: TooltipModel) {
                if (item !is TooltipModel.Header) {
                    return invalid(item)
                }
                item.text?.let { binding.root.text = it }
            }
        }

        class TooltipWithTitle(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.tooltip_with_title)) {
            private val binding by viewBinding(TooltipWithTitleBinding::bind)
            override fun bind(item: TooltipModel) {
                if (item !is TooltipModel.Tooltip.TooltipWithTitle) {
                    return invalid(item)
                }
                binding.apply {
                    title.text = item.title
                    description.text = item.description
                }
            }
        }

        class TooltipWithoutTitle(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.tooltip_without_title)) {
            private val binding by viewBinding(TooltipWithoutTitleBinding::bind)
            override fun bind(item: TooltipModel) {
                if (item !is TooltipModel.Tooltip.TooltipWithOutTitle) {
                    return invalid(item)
                }
                binding.root.text = item.description
            }
        }
    }
}
