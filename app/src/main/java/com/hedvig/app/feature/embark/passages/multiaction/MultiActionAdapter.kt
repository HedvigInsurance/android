package com.hedvig.app.feature.embark.passages.multiaction

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.inflate

class MultiActionAdapter(private val clickListener: ClickListener) : ListAdapter<MultiAction, MultiActionViewHolder>(GenericDiffUtilItemCallback()) {

    interface ClickListener {
        fun onComponentClick(id: Long)
        fun onComponentRemove(id: Long)
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is MultiAction.AddButton -> R.layout.view_multi_action_add
        is MultiAction.Component -> R.layout.view_multi_action_component
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.view_multi_action_component -> ComponentViewHolder(parent.inflate(R.layout.view_multi_action_component))
        R.layout.view_multi_action_add -> ButtonViewHolder(parent.inflate(R.layout.view_multi_action_add))
        else -> throw Error("Invalid view type")
    }

    override fun onBindViewHolder(holder: MultiActionViewHolder, position: Int) = when (holder) {
        is ComponentViewHolder -> {
            val item = (getItem(position) as MultiAction.Component)
            holder.bind(item, clickListener)
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

class ComponentViewHolder(itemView: View) : MultiActionViewHolder(itemView) {

    private val removeButton = itemView.findViewById<FrameLayout>(R.id.removeButton)
    private val title = itemView.findViewById<TextView>(R.id.title)
    private val subtitle = itemView.findViewById<TextView>(R.id.subtitle)

    fun bind(item: MultiAction.Component, clickListener: MultiActionAdapter.ClickListener) {
        title.text = item.selectedDropDown
        subtitle.text = item.input
        removeButton.setOnClickListener {
            clickListener.onComponentRemove(item.id)
        }
        itemView.setOnClickListener {
            clickListener.onComponentClick(item.id)
        }
    }
}

class ButtonViewHolder(itemView: View) : MultiActionViewHolder(itemView)
