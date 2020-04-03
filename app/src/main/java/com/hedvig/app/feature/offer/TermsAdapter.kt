package com.hedvig.app.feature.offer

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.R
import com.hedvig.app.util.extensions.openUri
import com.hedvig.app.util.extensions.view.setHapticClickListener
import kotlinx.android.synthetic.main.offer_terms_area_button.view.*

class TermsAdapter(
    private val tracker: OfferTracker
) : RecyclerView.Adapter<TermsAdapter.ViewHolder>() {

    var items: List<OfferQuery.InsuranceTerm> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater
            .from(parent.context)
            .inflate(R.layout.offer_terms_area_button, parent, false)
    ) {
        fun bind(terms: OfferQuery.InsuranceTerm, tracker: OfferTracker) {
            val button = itemView.button
            button.text = terms.displayName + " ↗"
            button.setHapticClickListener {
                tracker.openOfferLink(terms.displayName)
                it.context.openUri(Uri.parse(terms.url))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], tracker)
    }
}
