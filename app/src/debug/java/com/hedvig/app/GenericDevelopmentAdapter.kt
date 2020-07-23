package com.hedvig.app

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.view.setHapticClickListener

class GenericDevelopmentAdapter(
    private val items: List<Item>
) : RecyclerView.Adapter<GenericDevelopmentAdapter.ViewHolder>() {

    override fun getItemViewType(position: Int) = when (items[position]) {
        is Item.Header -> R.layout.development_header_row
        is Item.ClickableItem -> R.layout.development_row
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.development_header_row -> ViewHolder.HeaderViewHolder(parent)
        R.layout.development_row -> ViewHolder.ClickableViewHolder(parent)
        else -> throw Error("Invalid viewType")
    }

    override fun getItemCount() = items.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(data: Item)

        class HeaderViewHolder(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.development_header_row)) {
            private val root = itemView as TextView
            override fun bind(data: Item) {
                (data as? Item.Header)?.let {
                    root.text = data.title
                }
            }
        }

        class ClickableViewHolder(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.development_row)) {
            private val root = itemView as TextView

            override fun bind(data: Item) {
                (data as? Item.ClickableItem)?.let {
                    root.text = data.title
                    root.setHapticClickListener { data.open() }
                }
            }
        }
    }

    sealed class Item {
        data class Header(
            val title: String
        ) : Item()

        data class ClickableItem(
            val title: String,
            val open: () -> Unit
        ) : Item()
    }
}
