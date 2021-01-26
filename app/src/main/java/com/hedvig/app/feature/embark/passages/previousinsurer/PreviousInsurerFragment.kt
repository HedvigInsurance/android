package com.hedvig.app.feature.embark.passages.previousinsurer

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.fragment.app.Fragment
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.R
import com.hedvig.app.databinding.FragmentPreviousInsurerBinding
import com.hedvig.app.feature.embark.EmbarkViewModel
import com.hedvig.app.feature.embark.passages.MessageAdapter
import com.hedvig.app.feature.embark.ui.PreviousInsurerBottomSheet
import com.hedvig.app.feature.embark.ui.PreviousInsurerBottomSheet.Companion.EXTRA_INSURER_ID
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding
import kotlinx.android.parcel.Parcelize
import org.koin.android.viewmodel.ext.android.sharedViewModel

class PreviousInsurerFragment : Fragment(R.layout.fragment_previous_insurer) {

    private val binding by viewBinding(FragmentPreviousInsurerBinding::bind)
    private val model: EmbarkViewModel by sharedViewModel()

    private val insurerData by lazy {
        requireArguments()
            .getParcelable<PreviousInsurerData>(DATA)
            ?: throw IllegalArgumentException("No argument passed to ${this.javaClass.name}")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.apply {
            messages.adapter = MessageAdapter(insurerData.messages)
            currentInsurerContainer.setHapticClickListener {
                onShowInsurers()
            }
        }
    }

    private fun onShowInsurers() {
        val fragment = PreviousInsurerBottomSheet.newInstance(insurerData.previousInsurers)
        fragment.setTargetFragment(this, REQUEST_SELECT_INSURER)
        fragment.show(parentFragmentManager, PreviousInsurerBottomSheet.TAG)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_SELECT_INSURER -> {
                val insurerId = data?.getStringExtra(EXTRA_INSURER_ID)
                if (resultCode == Activity.RESULT_OK && insurerId != null) {
                    model.putInStore(insurerData.storeKey, insurerId)
                    model.navigateToPassage(insurerData.next)
                }
            }
        }
    }

    companion object {
        private const val DATA = "DATA"
        private const val REQUEST_SELECT_INSURER = 1

        fun newInstance(previousInsurerData: PreviousInsurerData) =
            PreviousInsurerFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(DATA, previousInsurerData)
                }
            }
    }
}

@Parcelize
data class PreviousInsurerData(
    val messages: List<String>,
    val next: String,
    val skip: String,
    val storeKey: String,
    val previousInsurers: List<PreviousInsurer>
) : Parcelable {
    @Parcelize
    data class PreviousInsurer(
        val name: String,
        val icon: String
    ) : Parcelable

    companion object {
        fun from(messages: List<String>,
                 previousInsuranceAction: EmbarkStoryQuery.AsEmbarkPreviousInsuranceProviderAction) =
            PreviousInsurerData(
                messages = messages,
                next = previousInsuranceAction.data.next.fragments.embarkLinkFragment.name,
                skip = previousInsuranceAction.data.skip.fragments.embarkLinkFragment.name,
                previousInsurers = previousInsuranceAction
                    .data
                    .insuranceProviders
                    .map {
                        PreviousInsurer(
                            it.name,
                            it.logo.variants.fragments.iconVariantsFragment.light.svgUrl
                        )
                    },
                storeKey = previousInsuranceAction.data.storeKey,
            )
    }
}
