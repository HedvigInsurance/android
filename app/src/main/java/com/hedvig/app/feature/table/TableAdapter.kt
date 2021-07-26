package com.hedvig.app.feature.table

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.databinding.BottomSheetHeaderItemLayoutBinding
import com.hedvig.app.databinding.HeaderCenteredItemLayoutBinding
import com.hedvig.app.databinding.ListTextItemBinding
import com.hedvig.app.databinding.ListTextItemTwoLineBinding
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.viewBinding

class TableAdapter : ListAdapter<TableAdapter.UpcomingAgreementItem, TableAdapter.UpcomingAgreementViewHolder>(
    GenericDiffUtilItemCallback()
) {

    fun setTable(table: Table) {
        submitList(
            listOfNotNull(
                table.title.takeIf { it.isNotBlank() }?.let(UpcomingAgreementItem::CenteredHeader),
            ) + table.sections.flatMap { section ->
                listOfNotNull(
                    section.title.takeIf { it.isNotBlank() }?.let(UpcomingAgreementItem::Header),
                ) + section.rows.map { row ->
                    row.subtitle?.let { subtitle ->
                        UpcomingAgreementItem.BuildingItem(row.title, subtitle, row.value)
                    } ?: UpcomingAgreementItem.ListItem(row.title, row.value)
                }
            }
        )
    }

    override fun getItemViewType(position: Int) = when (currentList[position]) {
        is UpcomingAgreementItem.ListItem -> R.layout.list_text_item
        is UpcomingAgreementItem.BuildingItem -> R.layout.list_text_item_two_line
        is UpcomingAgreementItem.Header -> R.layout.bottom_sheet_header_item_layout
        is UpcomingAgreementItem.CenteredHeader -> R.layout.header_centered_item_layout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.list_text_item -> ListItemViewHolder(parent)
        R.layout.list_text_item_two_line -> TwoLineListItemViewHolder(parent)
        R.layout.bottom_sheet_header_item_layout -> HeaderViewHolder(parent)
        R.layout.header_centered_item_layout -> CenteredHeaderViewHolder(parent)
        else -> throw Error("Invalid view type")
    }

    override fun onBindViewHolder(holder: UpcomingAgreementViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    abstract class UpcomingAgreementViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(item: UpcomingAgreementItem)
    }

    class ListItemViewHolder(parent: ViewGroup) : UpcomingAgreementViewHolder(parent.inflate(R.layout.list_text_item)) {

        private val binding by viewBinding(ListTextItemBinding::bind)

        override fun bind(item: UpcomingAgreementItem) {
            if (item !is UpcomingAgreementItem.ListItem) {
                throw IllegalArgumentException("Wrong item ($item) in ListViewHolder")
            }

            binding.label.text = item.label
            binding.value.text = item.value
        }
    }

    class TwoLineListItemViewHolder(parent: ViewGroup) :
        UpcomingAgreementViewHolder(parent.inflate(R.layout.list_text_item_two_line)) {

        private val binding by viewBinding(ListTextItemTwoLineBinding::bind)

        override fun bind(item: UpcomingAgreementItem) {
            if (item !is UpcomingAgreementItem.BuildingItem) {
                throw IllegalArgumentException("Wrong item ($item) in TwoLineListItemViewHolder")
            }
            with(binding) {
                title.text = item.title
                subtitle.text = item.subtitle
                value.text = item.value
            }
        }
    }

    class HeaderViewHolder(parent: ViewGroup) :
        UpcomingAgreementViewHolder(parent.inflate(R.layout.bottom_sheet_header_item_layout)) {

        private val binding by viewBinding(BottomSheetHeaderItemLayoutBinding::bind)

        override fun bind(item: UpcomingAgreementItem) {
            if (item !is UpcomingAgreementItem.Header) {
                throw IllegalArgumentException("Wrong item ($item) in HeaderViewHolder")
            }

            binding.headerItem.text = item.text
        }
    }

    class CenteredHeaderViewHolder(parent: ViewGroup) :
        UpcomingAgreementViewHolder(parent.inflate(R.layout.header_centered_item_layout)) {

        private val binding by viewBinding(HeaderCenteredItemLayoutBinding::bind)

        override fun bind(item: UpcomingAgreementItem) {
            if (item !is UpcomingAgreementItem.CenteredHeader) {
                throw IllegalArgumentException("Wrong item ($item) in CenteredHeaderViewHolder")
            }

            binding.headerItem.text = item.text
        }
    }

    sealed class UpcomingAgreementItem {

        data class CenteredHeader(val text: String) : UpcomingAgreementItem()
        data class Header(val text: String) : UpcomingAgreementItem()

        data class ListItem(
            val label: String,
            val value: String,
        ) : UpcomingAgreementItem()

        data class BuildingItem(
            val title: String,
            val subtitle: String,
            val value: String,
        ) : UpcomingAgreementItem()
    }
}
