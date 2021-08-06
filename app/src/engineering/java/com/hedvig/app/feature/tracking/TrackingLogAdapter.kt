package com.hedvig.app.feature.tracking

import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.databinding.TwoLineListItemBinding
import com.hedvig.app.feature.tracking.TrackDetailFragment.Companion.show
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding
import java.time.format.DateTimeFormatter

class TrackingLogAdapter(
    private val fragmentManager: FragmentManager,
) : ListAdapter<TrackEvent, TrackingLogAdapter.ViewHolder>(
    GenericDiffUtilItemCallback()
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent, fragmentManager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        parent: ViewGroup,
        private val fragmentManager: FragmentManager,
    ) : RecyclerView.ViewHolder(parent.inflate(R.layout.two_line_list_item)) {
        private val binding by viewBinding(TwoLineListItemBinding::bind)

        fun bind(event: TrackEvent) = with(binding) {
            lineOne.text = event.name
            lineTwo.text = lineTwo.context.getString(
                R.string.event_list_item_line_two,
                event.timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                if (event.propertiesJsonString != null) {
                    "Yes"
                } else {
                    "No"
                }
            )
            root.setHapticClickListener {
                TrackDetailFragment
                    .newInstance(event)
                    .show(fragmentManager)
            }
        }
    }
}

