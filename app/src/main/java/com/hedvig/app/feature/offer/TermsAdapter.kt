package com.hedvig.app.feature.offer

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.android.owldroid.type.InsuranceTermType
import com.hedvig.app.R
import com.hedvig.app.databinding.OfferTermsAreaButtonBinding
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.openUri
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding

class TermsAdapter(
    private val tracker: OfferTracker,
    private val marketManager: MarketManager
) : ListAdapter<OfferQuery.InsuranceTerm, TermsAdapter.ViewHolder>(GenericDiffUtilItemCallback()) {

    class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater
            .from(parent.context)
            .inflate(R.layout.offer_terms_area_button, parent, false)
    ) {
        private val binding by viewBinding(OfferTermsAreaButtonBinding::bind)
        fun bind(terms: OfferQuery.InsuranceTerm, tracker: OfferTracker, marketManager: MarketManager) {
            val button = binding.button
            button.text = terms.displayName + " ↗"
            button.setHapticClickListener {
                tracker.openOfferLink(terms.displayName)
                // TODO Quick fix for getting new terms and conditions
                val uri = Uri.parse(if (marketManager.market == Market.SE && terms.type == InsuranceTermType.TERMS_AND_CONDITIONS) {
                    "https://www.hedvig.com/se/villkor"
                } else {
                    terms.url
                })
                it.context.openUri(uri)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), tracker, marketManager)
    }
}
