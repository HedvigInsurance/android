package com.hedvig.app

import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.view.setHapticClickListener

class GenericDevelopmentAdapter(
    private val items: List<Item>
) : RecyclerView.Adapter<GenericDevelopmentAdapter.ViewHolder>() {
   
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)
    override fun getItemCount() = items.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    class ViewHolder(parent: ViewGroup) :
        RecyclerView.ViewHolder(parent.inflate(R.layout.development_row)) {
        private val root = itemView as TextView
        fun bind(data: Item) {
            root.text = data.title
            root.setHapticClickListener { data.open() }
        }
    }

    data class Item(
        val title: String,
        val open: () -> Unit
    )
}
