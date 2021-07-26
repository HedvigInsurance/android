package com.hedvig.app.feature.embark.passages.multiaction

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.databinding.ViewMultiActionAddBinding
import com.hedvig.app.databinding.ViewMultiActionComponentBinding
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.dp
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.invalid
import com.hedvig.app.util.extensions.view.increaseTouchableArea
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding

class MultiActionAdapter(
    private val onComponentClick: (Long) -> Unit,
    private val onComponentRemove: (Long) -> Unit,
    private val createNewComponent: () -> Unit,
) : ListAdapter<MultiActionItem, MultiActionAdapter.MultiActionViewHolder>(GenericDiffUtilItemCallback()) {

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is MultiActionItem.AddButton -> R.layout.view_multi_action_add
        is MultiActionItem.Component -> R.layout.view_multi_action_component
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.view_multi_action_component -> MultiActionViewHolder.ComponentViewHolder(parent)
        R.layout.view_multi_action_add -> MultiActionViewHolder.ButtonViewHolder(parent, createNewComponent)
        else -> throw Error("Invalid view type")
    }

    override fun onBindViewHolder(holder: MultiActionViewHolder, position: Int) = when (holder) {
        is MultiActionViewHolder.ComponentViewHolder -> {
            val item = (getItem(position) as MultiActionItem.Component)
            holder.bind(item, onComponentClick, onComponentRemove)
        }
        is MultiActionViewHolder.ButtonViewHolder -> {
            holder.bind(getItem(position))
        }
    }

    sealed class MultiActionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        class ComponentViewHolder(
            parent: ViewGroup,
        ) : MultiActionViewHolder(parent.inflate(R.layout.view_multi_action_component)) {
            private val binding by viewBinding(ViewMultiActionComponentBinding::bind)

            fun bind(
                item: MultiActionItem.Component,
                onComponentClick: (Long) -> Unit,
                onComponentRemove: (Long) -> Unit
            ) {
                binding.title.text = item.selectedDropDowns.joinToString { it.text }

                val switchLabels = item.switches
                    .filter { it.value }
                    .takeIf { it.isNotEmpty() }
                    ?.joinToString(prefix = " ・ ") { it.label } ?: ""

                val inputLabels = item.inputs.joinToString()

                binding.subtitle.text = inputLabels + switchLabels

                binding.removeButton.increaseTouchableArea(32.dp)
                binding.removeButton.setOnClickListener {
                    onComponentRemove(item.id)
                }
                itemView.setOnClickListener {
                    onComponentClick(item.id)
                }
            }
        }

        class ButtonViewHolder(
            parent: ViewGroup,
            onClick: () -> Unit
        ) : MultiActionViewHolder(parent.inflate(R.layout.view_multi_action_add)) {
            private val binding by viewBinding(ViewMultiActionAddBinding::bind)

            init {
                binding.root.setHapticClickListener { onClick() }
            }

            fun bind(data: MultiActionItem) = with(binding.title) {
                if (data !is MultiActionItem.AddButton) {
                    return invalid(data)
                }

                text = data.label
            }
        }
    }
}
