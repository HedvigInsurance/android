package com.hedvig.app.feature.table

import com.hedvig.android.owldroid.fragment.TableFragment

fun TableFragment.intoTable() =
    Table(
        title = title,
        sections = sections.map { section ->
            Table.Section(
                title = section.title,
                rows = section.rows.map { row ->
                    Table.Row(
                        title = row.title,
                        value = row.value,
                        subtitle = row.subtitle
                    )
                }
            )
        }
    )
