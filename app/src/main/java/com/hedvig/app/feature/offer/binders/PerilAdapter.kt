package com.hedvig.app.feature.offer.binders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.BuildConfig
import com.hedvig.app.R
import com.hedvig.app.util.extensions.isDarkThemeActive
import kotlinx.android.synthetic.main.offer_peril_item.view.*

class PerilAdapter : RecyclerView.Adapter<PerilAdapter.ViewHolder>() {

    var list: List<OfferQuery.Peril> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(
                R.layout.offer_peril_item,
                parent,
                false
            )
    ) {
        private val icon = itemView.icon
        private val title = itemView.title

        fun bind(peril: OfferQuery.Peril) {
            val iconUrl = "${BuildConfig.BASE_URL}${if (icon.context.isDarkThemeActive) {
                peril.icon.variants.dark.svgUrl
            } else {
                peril.icon.variants.light.svgUrl
            }}"

            title.text = peril.title
            Glide.with(icon.context)
                .load(iconUrl)
                .into(icon)
        }
    }
}
