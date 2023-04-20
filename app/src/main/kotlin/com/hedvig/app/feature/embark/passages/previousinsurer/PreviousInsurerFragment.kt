package com.hedvig.app.feature.embark.passages.previousinsurer

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.view.doOnNextLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hedvig.android.core.common.android.parcelable
import com.hedvig.android.core.common.android.whenApiVersion
import com.hedvig.app.R
import com.hedvig.app.databinding.PreviousOrExternalInsurerFragmentBinding
import com.hedvig.app.feature.embark.EmbarkViewModel
import com.hedvig.app.feature.embark.passages.MessageAdapter
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.setupInsetsForIme
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import slimber.log.d

class PreviousInsurerFragment : Fragment(R.layout.previous_or_external_insurer_fragment) {

  private val binding by viewBinding(PreviousOrExternalInsurerFragmentBinding::bind)
  private val viewModel: EmbarkViewModel by activityViewModel()
  private var insurerId: String? = null

  private val insurerData by lazy {
    requireArguments().parcelable<PreviousInsurerParameter>(DATA)
      ?: error("No argument passed to ${this.javaClass.name}")
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    postponeEnterTransition()

    binding.apply {
      whenApiVersion(Build.VERSION_CODES.R) {
        currentInsurerContainer.setupInsetsForIme(
          root = root,
          currentInsurerContainer,
        )
      }

      messages.adapter = MessageAdapter(insurerData.messages)
      currentInsurerContainer.setHapticClickListener {
        onShowInsurers()
      }
      continueButton.setHapticClickListener {
        onContinue()
      }

      setFragmentResultListener(InsurerProviderBottomSheet.REQUEST_KEY) { requestKey: String, bundle: Bundle ->
        if (requestKey == InsurerProviderBottomSheet.REQUEST_KEY) {
          val insurerName = bundle.getString(InsurerProviderBottomSheet.INSURER_NAME_KEY)
          insurerId = bundle.getString(InsurerProviderBottomSheet.INSURER_ID_KEY)
          continueButton.isEnabled = insurerName != null
          if (insurerName?.isNotEmpty() == true) {
            currentInsurerLabel.text = insurerName
          }
        }
      }

      messages.doOnNextLayout {
        startPostponedEnterTransition()
      }
    }
  }

  private fun onContinue() {
    if (insurerId == getString(hedvig.resources.R.string.EXTERNAL_INSURANCE_PROVIDER_OTHER_OPTION)) {
      MaterialAlertDialogBuilder(requireContext())
        .setTitle(getString(hedvig.resources.R.string.EXTERNAL_INSURANCE_PROVIDER_ALERT_TITLE))
        .setMessage(getString(hedvig.resources.R.string.EXTERNAL_INSURANCE_PROVIDER_ALERT_MESSAGE))
        .setPositiveButton(getString(hedvig.resources.R.string.ALERT_OK)) { dialog, _ -> continueEmbark() }
        .show()
    } else {
      insurerId?.let {
        viewModel.putInStore(insurerData.storeKey, it)
        continueEmbark()
      } ?: d { "insurerId was null when continuing from PreviousInsurerFragment" }
    }
  }

  private fun continueEmbark() {
    viewModel.submitAction(insurerData.next)
  }

  private fun onShowInsurers() {
    val fragment = InsurerProviderBottomSheet.newInstance(insurerData.previousInsurers)
    fragment.show(parentFragmentManager, InsurerProviderBottomSheet.TAG)
  }

  companion object {
    private const val DATA = "DATA"

    fun newInstance(previousInsurerData: PreviousInsurerParameter) =
      PreviousInsurerFragment().apply {
        arguments = Bundle().apply {
          putParcelable(DATA, previousInsurerData)
        }
      }
  }
}
