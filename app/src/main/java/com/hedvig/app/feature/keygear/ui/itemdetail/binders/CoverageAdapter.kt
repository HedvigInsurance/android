package com.hedvig.app.feature.keygear.ui.itemdetail.binders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.util.extensions.compatDrawable
import kotlinx.android.synthetic.main.key_gear_item_detail_coverage_row.view.*

class CoverageAdapter(
    private val isExceptions: Boolean
) : RecyclerView.Adapter<CoverageAdapter.ViewHolder>() {
    var items: List<String> = listOf()
        set(value) {
            val diff = DiffUtil.calculateDiff(Callback(field, value))
            field = value
            diff.dispatchUpdatesTo(this)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.key_gear_item_detail_coverage_row, parent, false)
    )

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.label.text = items[position]
        holder.label.setCompoundDrawablesWithIntrinsicBounds(
            holder.label.context.compatDrawable(
                if (isExceptions) {
                    R.drawable.ic_cross
                } else {
                    R.drawable.ic_filled_checkmark_small
                }
            ), null, null, null
        )
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val label: TextView = view.label
    }

    class Callback(
        private val old: List<String>,
        private val new: List<String>
    ) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            old[oldItemPosition] == new[newItemPosition]

        override fun getOldListSize() = old.size

        override fun getNewListSize() = new.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            areItemsTheSame(oldItemPosition, newItemPosition)
    }
}
