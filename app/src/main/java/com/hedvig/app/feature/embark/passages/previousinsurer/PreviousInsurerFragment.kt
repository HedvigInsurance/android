package com.hedvig.app.feature.embark.passages.previousinsurer

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import com.hedvig.app.R
import com.hedvig.app.databinding.PreviousInsurerFragmentBinding
import com.hedvig.app.feature.embark.EmbarkViewModel
import com.hedvig.app.feature.embark.passages.MessageAdapter
import com.hedvig.app.feature.embark.ui.PreviousInsurerBottomSheet
import com.hedvig.app.feature.embark.ui.PreviousInsurerBottomSheet.Companion.EXTRA_INSURER_ID
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding
import org.koin.android.viewmodel.ext.android.sharedViewModel

class PreviousInsurerFragment : Fragment(R.layout.previous_insurer_fragment) {

    private val binding by viewBinding(PreviousInsurerFragmentBinding::bind)
    private val model: EmbarkViewModel by sharedViewModel()

    private val insurerData by lazy {
        requireArguments()
            .getParcelable<PreviousInsurerParameter>(DATA)
            ?: throw IllegalArgumentException("No argument passed to ${this.javaClass.name}")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.apply {
            messages.adapter = MessageAdapter(insurerData.messages)
            currentInsurerContainer.setHapticClickListener {
                onShowInsurers()
            }
            continueButton.setHapticClickListener {
                onShowInsurers()
            }
        }

        setFragmentResultListener(REQUEST_SELECT_INSURER) { requestKey, bundle ->
            val insurerId = bundle.getString(EXTRA_INSURER_ID)
            if (requestKey == REQUEST_SELECT_INSURER && insurerId != null) {
                model.putInStore(insurerData.storeKey, insurerId)
                model.navigateToPassage(insurerData.next)
            }
        }
    }

    private fun onShowInsurers() {
        val fragment = PreviousInsurerBottomSheet.newInstance(insurerData.previousInsurers)
        fragment.show(parentFragmentManager, PreviousInsurerBottomSheet.TAG)
    }

    companion object {
        const val REQUEST_SELECT_INSURER = "request_insurer"
        private const val DATA = "DATA"

        fun newInstance(previousInsurerData: PreviousInsurerParameter) =
            PreviousInsurerFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(DATA, previousInsurerData)
                }
            }
    }
}

