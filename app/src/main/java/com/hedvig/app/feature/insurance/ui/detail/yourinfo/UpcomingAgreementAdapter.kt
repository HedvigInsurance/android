package com.hedvig.app.feature.insurance.ui.detail.yourinfo

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.databinding.BottomSheetHeaderItemLayoutBinding
import com.hedvig.app.databinding.HeaderCenteredItemLayoutBinding
import com.hedvig.app.databinding.ListTextItemBinding
import com.hedvig.app.databinding.ListTextItemTwoLineBinding
import com.hedvig.app.feature.home.ui.changeaddress.GetUpcomingAgreementUseCase.UpcomingAgreementResult
import com.hedvig.app.feature.insurance.ui.detail.yourinfo.UpcomingAgreementAdapter.UpcomingAgreementItem.*
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.viewBinding

class UpcomingAgreementAdapter(
    upcomingAgreement: UpcomingAgreementResult.UpcomingAgreement,
    context: Context
) : ListAdapter<UpcomingAgreementAdapter.UpcomingAgreementItem, UpcomingAgreementAdapter.UpcomingAgreementViewHolder>(GenericDiffUtilItemCallback()) {

    init {
        val list = mutableListOf<UpcomingAgreementItem>()

        CenteredHeader("STRING RES MISSING").let(list::add)

        ListItem(context.getString(R.string.housing_info_list_postal_code_label), upcomingAgreement.address.street).let(list::add)
        ListItem("STRING RES MISSING", upcomingAgreement.address.postalCode).let(list::add)
        ListItem(context.getString(R.string.housing_info_list_insured_people_label), context.getString(R.string.DASHBOARD_MY_INFO_COINSURED, upcomingAgreement.nrOfCoInsured)).let(list::add)
        ListItem(context.getString(R.string.housing_info_list_living_space_label), upcomingAgreement.squareMeters.toString()).let(list::add)

        upcomingAgreement.ancillaryArea
            ?.let { ListItem(context.getString(R.string.housing_info_list_ancillary_area_label), context.getString(R.string.HOUSE_INFO_BOYTA_SQUAREMETERS, it)) }
            ?.let(list::add)

        upcomingAgreement.yearBuilt
            ?.let { ListItem(context.getString(R.string.housing_info_list_construction_year_label), it.toString()) }
            ?.let(list::add)

        upcomingAgreement.numberOfBaths
            ?.let { ListItem(context.getString(R.string.housing_info_list_baths_label), it.toString()) }
            ?.let(list::add)

        upcomingAgreement.partlySubleted
            ?.let {
                val valueString = if (it) {
                    context.getString(R.string.HOUSE_INFO_SUBLETED_TRUE)
                } else {
                    context.getString(R.string.HOUSE_INFO_SUBLETED_FALSE)
                }
                ListItem(context.getString(R.string.housing_info_list_sublet_label), valueString)
            }
            ?.let(list::add)

        Header(context.getString(R.string.housing_info_list_extra_buildings_subheadline)).let(list::add)

        upcomingAgreement.extraBuildings.filterNotNull().forEach {
            BuildingItem(it.name, it.area, it.hasWaterConnected).let(list::add)
        }

        submitList(list)
    }

    override fun getItemViewType(position: Int) = when (currentList[position]) {
        is ListItem -> R.layout.list_text_item
        is BuildingItem -> R.layout.list_text_item_two_line
        is Header -> R.layout.bottom_sheet_header_item_layout
        is CenteredHeader -> R.layout.header_centered_item_layout
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
            if (item !is ListItem) throw IllegalArgumentException("Wrong item ($item) in ListViewHolder")

            binding.label.text = item.label
            binding.value.text = item.value
        }
    }

    class TwoLineListItemViewHolder(parent: ViewGroup) : UpcomingAgreementViewHolder(parent.inflate(R.layout.list_text_item_two_line)) {

        private val binding by viewBinding(ListTextItemTwoLineBinding::bind)

        override fun bind(item: UpcomingAgreementItem) {
            if (item !is BuildingItem) throw IllegalArgumentException("Wrong item ($item) in TwoLineListItemViewHolder")

            val areaString = binding.root.context.getString(R.string.HOUSE_INFO_BOYTA_SQUAREMETERS, item.area)
            val waterConnectedString = if (item.waterConnected) ", water connected" else ""

            binding.label.text = item.name
            binding.value.text = "$areaString$waterConnectedString"
        }
    }

    class HeaderViewHolder(parent: ViewGroup) : UpcomingAgreementViewHolder(parent.inflate(R.layout.bottom_sheet_header_item_layout)) {

        private val binding by viewBinding(BottomSheetHeaderItemLayoutBinding::bind)

        override fun bind(item: UpcomingAgreementItem) {
            if (item !is Header) throw IllegalArgumentException("Wrong item ($item) in HeaderViewHolder")

            binding.headerItem.text = item.text
        }
    }

    class CenteredHeaderViewHolder(parent: ViewGroup) : UpcomingAgreementViewHolder(parent.inflate(R.layout.header_centered_item_layout)) {

        private val binding by viewBinding(HeaderCenteredItemLayoutBinding::bind)

        override fun bind(item: UpcomingAgreementItem) {
            if (item !is CenteredHeader) throw IllegalArgumentException("Wrong item ($item) in CenteredHeaderViewHolder")

            binding.headerItem.text = item.text
        }
    }

    sealed class UpcomingAgreementItem {

        data class CenteredHeader(val text: String) : UpcomingAgreementItem()
        data class Header(val text: String) : UpcomingAgreementItem()

        data class ListItem(
            val label: String,
            val value: String
        ) : UpcomingAgreementItem()

        data class BuildingItem(
            val name: String,
            val area: Int,
            val waterConnected: Boolean
        ) : UpcomingAgreementItem()

    }

}
