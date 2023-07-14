package com.hedvig.app.feature.table

import android.view.LayoutInflater
import android.view.ViewGroup
import com.hedvig.android.core.common.android.table.Table
import com.hedvig.android.core.ui.databinding.ListTextItemBinding
import com.hedvig.app.BASE_MARGIN
import com.hedvig.app.databinding.BottomSheetHeaderItemLayoutBinding
import com.hedvig.app.databinding.HeaderItemLayoutBinding
import com.hedvig.app.databinding.ListTextItemTwoLineBinding
import com.hedvig.app.util.extensions.view.updateMargin

fun generateTable(target: ViewGroup, table: Table) {
  val inflater = LayoutInflater.from(target.context)
  target.removeAllViews()

  val header = HeaderItemLayoutBinding.inflate(inflater, target, false)
  header.root.text = table.title
  header.root.updateMargin(bottom = BASE_MARGIN)
  target.addView(header.root)

  table.sections.forEach { section ->
    val sectionHeader = BottomSheetHeaderItemLayoutBinding.inflate(inflater, target, false)
    sectionHeader.root.text = section.title
    sectionHeader.root.updateMargin(top = BASE_MARGIN)
    target.addView(sectionHeader.root)

    section.tableRows.forEach { row ->
      if (row.subtitle != null) {
        val twoLine = ListTextItemTwoLineBinding.inflate(inflater, target, false)
        twoLine.title.text = row.title
        twoLine.subtitle.text = row.subtitle
        twoLine.value.text = row.value
        target.addView(twoLine.root)
      } else {
        val singleLine = ListTextItemBinding.inflate(inflater, target, false)
        singleLine.label.text = row.title
        singleLine.value.text = row.value
        target.addView(singleLine.root)
      }
    }
  }
}
