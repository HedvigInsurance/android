package com.hedvig.android.core.common.android.table

import android.os.Parcelable
import giraffe.fragment.TableFragment
import kotlinx.parcelize.Parcelize

@Parcelize
data class Table(
  val title: String,
  val sections: List<Section>,
) : Parcelable {
  @Parcelize
  data class Section(
    val title: String,
    val rows: List<Row>,
  ) : Parcelable

  @Parcelize
  data class Row(
    val title: String,
    val subtitle: String?,
    val value: String,
  ) : Parcelable
}

fun TableFragment.intoTable() = Table(
  title = title,
  sections = sections.map { section ->
    Table.Section(
      title = section.title,
      rows = section.rows.map { row ->
        Table.Row(
          title = row.title,
          value = row.value,
          subtitle = row.subtitle,
        )
      },
    )
  },
)
