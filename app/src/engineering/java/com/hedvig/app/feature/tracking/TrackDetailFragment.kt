package com.hedvig.app.feature.tracking

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.hedvig.app.R
import com.hedvig.app.databinding.TrackingDetailFragmentBinding
import com.hedvig.app.util.extensions.setMarkdownText
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import java.time.format.DateTimeFormatter

class TrackDetailFragment : DialogFragment(R.layout.tracking_detail_fragment) {
    private val binding by viewBinding(TrackingDetailFragmentBinding::bind)

    override fun getTheme() = R.style.FullScreenDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val event = requireArguments().getParcelable<TrackEvent>(EVENT)
            ?: throw IllegalArgumentException("Missing EVENT in ${this.javaClass.name}")

        with(binding) {
            toolbar.setNavigationOnClickListener { dismiss() }
            name.text = event.name
            timestamp.text = event.timestamp.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            if (event.propertiesJsonString != null) {
                properties.setMarkdownText(
                    """```json
${event.propertiesJsonString}
```"""
                )
            } else {
                properties.setText(com.hedvig.app.R.string.event_detail_properties_none)
            }
        }
    }

    companion object {
        private const val TAG = "TrackDetailFragment"
        private const val EVENT = "EVENT"
        fun newInstance(event: TrackEvent) = TrackDetailFragment().apply {
            arguments = bundleOf(EVENT to event)
        }

        fun TrackDetailFragment.show(fragmentManager: FragmentManager) = show(fragmentManager, TAG)
    }
}
