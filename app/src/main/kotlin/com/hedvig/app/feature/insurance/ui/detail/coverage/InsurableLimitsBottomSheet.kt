package com.hedvig.app.feature.insurance.ui.detail.coverage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hedvig.app.R
import com.hedvig.app.databinding.InsurableLimitBottomSheetBinding
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import giraffe.fragment.InsurableLimitsFragment

class InsurableLimitsBottomSheet : BottomSheetDialogFragment() {
  private val binding by viewBinding(InsurableLimitBottomSheetBinding::bind)

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View? = inflater.inflate(R.layout.insurable_limit_bottom_sheet, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    binding.apply {
      title.text = requireArguments().getString(TITLE)
      body.text = requireArguments().getString(BODY)
    }
  }

  companion object {
    private const val TITLE = "TITLE"
    private const val BODY = "BODY"

    val TAG: String = InsurableLimitsBottomSheet::class.java.name

    fun newInstance(insurableLimits: InsurableLimitsFragment) =
      InsurableLimitsBottomSheet().apply {
        arguments = bundleOf(
          TITLE to insurableLimits.label,
          BODY to insurableLimits.description,
        )
      }

    fun newInstance(
      label: String,
      description: String,
    ) = InsurableLimitsBottomSheet().apply {
      arguments = bundleOf(
        TITLE to label,
        BODY to description,
      )
    }
  }
}
