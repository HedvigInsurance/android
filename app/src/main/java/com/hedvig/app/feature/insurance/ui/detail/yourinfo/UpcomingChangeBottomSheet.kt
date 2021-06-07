package com.hedvig.app.feature.insurance.ui.detail.yourinfo

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import com.hedvig.app.feature.table.Table
import com.hedvig.app.feature.table.TableAdapter
import com.hedvig.app.ui.view.ExpandableBottomSheet

class UpcomingChangeBottomSheet : ExpandableBottomSheet() {

    private val upcomingAgreement by lazy {
        requireArguments().getParcelable<Table>(UPCOMING_AGREEMENT)
            ?: throw IllegalArgumentException("No argument passed to ${this.javaClass.name}")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recycler.adapter = TableAdapter(upcomingAgreement)
    }

    companion object {

        val TAG = UpcomingChangeBottomSheet::class.java.name
        private const val UPCOMING_AGREEMENT = "UPCOMING_AGREEMENT"

        fun newInstance(upcomingAgreement: Table) =
            UpcomingChangeBottomSheet().apply {
                arguments = bundleOf(UPCOMING_AGREEMENT to upcomingAgreement)
            }
    }
}
