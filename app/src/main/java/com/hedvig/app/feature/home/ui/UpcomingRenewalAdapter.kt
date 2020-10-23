package com.hedvig.app.feature.home.ui

import android.net.Uri
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.app.R
import com.hedvig.app.databinding.UpconingRenewalCardBinding
import com.hedvig.app.feature.home.service.HomeTracker
import com.hedvig.app.feature.insurance.service.InsuranceTracker
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.canOpenUri
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.openUri
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class UpcomingRenewalAdapter(val tracker: HomeTracker) :
    ListAdapter<HomeQuery.Contract, UpcomingRenewalAdapter.ViewHolder>(
        GenericDiffUtilItemCallback()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), tracker)
    }

    class ViewHolder(parent: ViewGroup) :
        RecyclerView.ViewHolder(parent.inflate(R.layout.upconing_renewal_card)) {
        private val binding by viewBinding(UpconingRenewalCardBinding::bind)
        fun bind(item: HomeQuery.Contract, tracker: HomeTracker) {
            binding.apply {
                item.upcomingRenewal?.let { upcomingRenewal ->
                    body.text = body.context.getString(
                        R.string.DASHBOARD_RENEWAL_PROMPTER_BODY,
                        daysLeft(upcomingRenewal.renewalDate)
                    )

                    val maybeLinkUri = runCatching {
                        Uri.parse(upcomingRenewal.draftCertificateUrl)
                    }
                    action.setHapticClickListener {
                        tracker.showRenewal()
                        maybeLinkUri.getOrNull()?.let { uri ->
                            if (action.context.canOpenUri(uri)) {
                                action.context.openUri(uri)
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        fun daysLeft(date: LocalDate) =
            ChronoUnit.DAYS.between(date, LocalDate.now()).toInt()
    }
}
