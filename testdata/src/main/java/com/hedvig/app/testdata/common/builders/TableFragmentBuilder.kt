package com.hedvig.app.testdata.common.builders

import com.hedvig.android.owldroid.fragment.TableFragment

data class TableFragmentBuilder(
    private val title: String = "Title",
    private val sections: List<Pair<String, List<Triple<String, String?, String>>>> = emptyList(),
) {
    fun build() = TableFragment(
        title = title,
        sections = sections.map { (title, rows) ->
            TableFragment.Section(
                title = title,
                rows = rows.map { (title, subtitle, value) ->
                    TableFragment.Row(
                        title = title,
                        subtitle = subtitle,
                        value = value
                    )
                }
            )
        }
    )
}
