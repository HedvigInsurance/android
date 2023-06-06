package com.hedvig.app.feature.perils

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.updatePadding
import com.hedvig.android.core.common.android.parcelable
import com.hedvig.android.core.ui.view.viewDps
import com.hedvig.app.feature.insurance.ui.detail.coverage.PerilAdapter
import com.hedvig.app.feature.insurance.ui.detail.coverage.PerilModel
import com.hedvig.app.ui.view.ExpandableBottomSheet

class PerilBottomSheet : ExpandableBottomSheet() {

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    val peril = requireArguments().parcelable<Peril>(PERIL)
      ?: error("Programmer error: Missing arguments in ${this@PerilBottomSheet.javaClass.name}")

    binding.recycler.updatePadding(bottom = binding.recycler.paddingBottom + 56.viewDps)
    binding.recycler.adapter = PerilAdapter().also { adapter ->
      adapter.submitList(
        expandedList(
          peril.title,
          peril.description,
          peril.info,
          peril.covered,
          peril.exception,
        ),
      )
    }
  }

  private fun expandedList(
    title: String,
    description: String,
    info: String,
    covered: List<String>,
    exceptions: List<String>,
  ): List<PerilModel> {
    return buildList {
      add(PerilModel.Title(title))
      add(PerilModel.Description(description))
      if (covered.isNotEmpty()) {
        add(PerilModel.Header.CoveredHeader)
      }
      addAll(covered.map { PerilModel.PerilList.Covered(it) })
      if (exceptions.isNotEmpty()) {
        add(PerilModel.Header.ExceptionHeader)
      }
      addAll(exceptions.map { PerilModel.PerilList.Exception(it) })
      if (info.isNotBlank()) {
        add(PerilModel.Header.InfoHeader)
        add(PerilModel.Paragraph(info))
      }
    }
  }

  companion object {
    private const val PERIL = "PERIL"

    val TAG: String = PerilBottomSheet::class.java.name

    fun newInstance(peril: Peril) = PerilBottomSheet().apply {
      arguments = bundleOf(
        PERIL to peril,
      )
    }
  }
}
