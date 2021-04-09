package com.hedvig.app.feature.embark.passages.multiaction

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.databinding.ViewMultiActionComponentBinding
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.viewBinding

class MultiActionAdapter(
    private val onComponentClick: (Long) -> Unit,
    private val onComponentRemove: (Long) -> Unit
) : ListAdapter<MultiAction, MultiActionViewHolder>(GenericDiffUtilItemCallback()) {

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is MultiAction.AddButton -> R.layout.view_multi_action_add
        is MultiAction.Component -> R.layout.view_multi_action_component
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.view_multi_action_component -> ComponentViewHolder(parent)
        R.layout.view_multi_action_add -> ButtonViewHolder(parent)
        else -> throw Error("Invalid view type")
    }

    override fun onBindViewHolder(holder: MultiActionViewHolder, position: Int) = when (holder) {
        is ComponentViewHolder -> {
            val item = (getItem(position) as MultiAction.Component)
            holder.bind(item, onComponentClick, onComponentRemove)
        }
        is ButtonViewHolder -> {
            val item = (getItem(position) as MultiAction.AddButton)
            holder.itemView.setOnClickListener {
                item.onClick()
            }
        }
        else -> throw Error("Invalid view holder")
    }
}

abstract class MultiActionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

class ComponentViewHolder(parent: ViewGroup) : MultiActionViewHolder(parent.inflate(R.layout.view_multi_action_component)) {

    private val binding by viewBinding(ViewMultiActionComponentBinding::bind)

    fun bind(
        item: MultiAction.Component,
        onComponentClick: (Long) -> Unit,
        onComponentRemove: (Long) -> Unit
    ) {
        binding.title.text = item.selectedDropDown
        binding.subtitle.text = "${item.input} ${item.inputUnit}"
        binding.removeButton.setOnClickListener {
            onComponentRemove(item.id)
        }
        itemView.setOnClickListener {
            onComponentClick(item.id)
        }
    }
}

class ButtonViewHolder(parent: ViewGroup) : MultiActionViewHolder(parent.inflate(R.layout.view_multi_action_add))
