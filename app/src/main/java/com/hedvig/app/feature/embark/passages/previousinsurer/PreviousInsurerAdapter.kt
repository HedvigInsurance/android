package com.hedvig.app.feature.embark.passages.previousinsurer

import android.content.Context
import android.graphics.drawable.PictureDrawable
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestBuilder
import com.hedvig.app.R
import com.hedvig.app.databinding.ExpandableBottomSheetTitleBinding
import com.hedvig.app.databinding.PreviousInsurerItemBinding
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding

class PreviousInsurerAdapter(
    context: Context,
    previousInsurers: List<PreviousInsurerParameter.PreviousInsurer>,
    private val requestBuilder: RequestBuilder<PictureDrawable>,
    private val onInsurerClicked: (PreviousInsurerItem.Insurer) -> Unit,
) : ListAdapter<PreviousInsurerItem, PreviousInsurerAdapter.PreviousInsurerViewHolder>(GenericDiffUtilItemCallback()) {

    init {
        submitList(
            listOf(PreviousInsurerItem.Header) + previousInsurers.map { it.toListItem() } + PreviousInsurerItem.Insurer(
                context.getString(R.string.EXTERNAL_INSURANCE_PROVIDER_OTHER_OPTION),
                null,
                context.getString(R.string.EXTERNAL_INSURANCE_PROVIDER_OTHER_OPTION),
            )
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.expandable_bottom_sheet_title -> PreviousInsurerViewHolder.Header(parent)
        R.layout.previous_insurer_item -> PreviousInsurerViewHolder.InsurerViewHolder(
            parent,
            requestBuilder,
            onInsurerClicked
        )
        else -> throw Error("No view type found for: $viewType")
    }

    override fun onBindViewHolder(holder: PreviousInsurerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is PreviousInsurerItem.Header -> R.layout.expandable_bottom_sheet_title
        is PreviousInsurerItem.Insurer -> R.layout.previous_insurer_item
    }

    sealed class PreviousInsurerViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        abstract fun bind(item: PreviousInsurerItem)

        class InsurerViewHolder(
            parent: ViewGroup,
            val requestBuilder: RequestBuilder<PictureDrawable>,
            val onInsurerClicked: (PreviousInsurerItem.Insurer) -> Unit,
        ) : PreviousInsurerViewHolder(parent.inflate(R.layout.previous_insurer_item)) {

            private val binding by viewBinding(PreviousInsurerItemBinding::bind)

            override fun bind(item: PreviousInsurerItem) {
                (item as? PreviousInsurerItem.Insurer)?.let {
                    item.icon?.let { iconUrl ->
                        requestBuilder
                            .load(Uri.parse(binding.icon.context.getString(R.string.BASE_URL) + iconUrl))
                            .into(binding.icon)
                    }

                    binding.text.text = item.name
                    binding.root.setHapticClickListener {
                        onInsurerClicked(item)
                    }
                }
                    ?: throw IllegalArgumentException("Can only bind with PreviousInsurerItem.Insurer, not ${item.javaClass.name}")
            }
        }

        class Header(parent: ViewGroup) :
            PreviousInsurerViewHolder(parent.inflate(R.layout.expandable_bottom_sheet_title)) {

            private val binding by viewBinding(ExpandableBottomSheetTitleBinding::bind)

            init {
                binding.title.setText(R.string.onboarding_norway_current_insurer_bottom_sheet_title)
            }

            override fun bind(item: PreviousInsurerItem) = Unit
        }
    }
}

private fun PreviousInsurerParameter.PreviousInsurer.toListItem() = PreviousInsurerItem.Insurer(name, icon, id)
