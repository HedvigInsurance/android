package com.hedvig.app.feature.embark.passages

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import kotlinx.android.synthetic.main.embark_select_action_item.view.*

class SelectActionAdapter : RecyclerView.Adapter<SelectActionAdapter.ViewHolder>() {
    var items = listOf<SelectAction>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)
    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater
            .from(parent.context)
            .inflate(R.layout.embark_select_action_item, parent, false)
    ) {
        private val action = itemView.action

        fun bind(item: SelectAction) {
            action.text = item.label
        }
    }
}
