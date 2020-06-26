package com.hedvig.app

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.util.extensions.inflate

class GenericDevelopmentAdapter(
    private val items: List<GenericDevelopmentAdapterItem>
) : RecyclerView.Adapter<GenericDevelopmentAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)
    override fun getItemCount() = items.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    class ViewHolder(parent: ViewGroup) :
        RecyclerView.ViewHolder(parent.inflate(R.layout.development_row)) {
        fun bind(data: GenericDevelopmentAdapterItem) = Unit
    }

    data class GenericDevelopmentAdapterItem(
        val title: String,
        val open: () -> Unit
    )
}
