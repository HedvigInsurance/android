package com.hedvig.app.feature.embark.passages.previousinsurer

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.databinding.PickerHeaderBinding
import com.hedvig.app.databinding.PickerItemLayoutBinding
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.viewBinding

class PreviousInsurerAdapter(
    previousInsurers: List<PreviousInsurerData.PreviousInsurer>,
    private val onInsurerClicked: (String) -> Unit
) : ListAdapter<PreviousInsurerItem, PreviousInsurerAdapter.PreviousInsurerViewHolder>(GenericDiffUtilItemCallback()) {

    init {
        submitList(listOf(PreviousInsurerItem.Header(text = "Insurers")) + previousInsurers.map { it.toListItem() })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.picker_header -> PreviousInsurerViewHolder.Header(parent.inflate(R.layout.picker_header))
        R.layout.picker_item_layout -> PreviousInsurerViewHolder.InsurerViewHolder(parent.inflate(R.layout.picker_item_layout))
        else -> throw Error("No view type found for: $viewType")
    }

    override fun onBindViewHolder(holder: PreviousInsurerViewHolder, position: Int) = when (holder) {
        is PreviousInsurerViewHolder.InsurerViewHolder -> holder.bind(getItem(position) as PreviousInsurerItem.Insurer, onInsurerClicked)
        is PreviousInsurerViewHolder.Header -> holder.bind(getItem(position) as PreviousInsurerItem.Header)
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is PreviousInsurerItem.Header -> R.layout.picker_header
        is PreviousInsurerItem.Insurer -> R.layout.picker_item_layout
    }

    sealed class PreviousInsurerViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        class InsurerViewHolder(view: View) : PreviousInsurerViewHolder(view) {
            private val binding by viewBinding(PickerItemLayoutBinding::bind)

            fun bind(item: PreviousInsurerItem.Insurer, onInsurerClicked: (String) -> Unit) {
                binding.text.text = item.name
                binding.root.setOnClickListener {
                    onInsurerClicked(item.name)
                }
            }
        }

        class Header(view: View) : PreviousInsurerViewHolder(view) {
            private val binding by viewBinding(PickerHeaderBinding::bind)

            fun bind(item: PreviousInsurerItem.Header) {
                binding.header.text = item.text
            }
        }
    }
}

private fun PreviousInsurerData.PreviousInsurer.toListItem() = PreviousInsurerItem.Insurer(name, icon)
