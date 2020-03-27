package com.hedvig.app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.feature.chat.ui.ChatActivity
import com.hedvig.app.util.extensions.view.setHapticClickListener
import kotlinx.android.synthetic.main.dashboard_upsell.view.*

class UpsellAdapter : RecyclerView.Adapter<UpsellAdapter.ViewHolder>() {
    var items: List<UpsellModel> = emptyList()
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
            .inflate(R.layout.dashboard_upsell, parent, false)
    ) {
        private val title = itemView.title
        private val description = itemView.description
        private val cta = itemView.cta

        init {
            cta.setHapticClickListener {
                cta.context.startActivity(ChatActivity.newInstance(cta.context))
            }
        }

        fun bind(model: UpsellModel) {
            title.text = title.resources.getString(model.title)
            description.text = description.resources.getString(model.description)
            cta.text = cta.resources.getString(model.ctaText)
        }
    }
}

data class UpsellModel(
    @get:StringRes val title: Int,
    @get:StringRes val description: Int,
    @get:StringRes val ctaText: Int
)

