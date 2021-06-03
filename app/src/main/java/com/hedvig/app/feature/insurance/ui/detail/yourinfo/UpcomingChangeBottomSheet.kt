package com.hedvig.app.feature.insurance.ui.detail.yourinfo

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import com.hedvig.app.feature.home.ui.changeaddress.GetUpcomingAgreementUseCase.UpcomingAgreementResult.UpcomingAgreement
import com.hedvig.app.ui.view.ExpandableBottomSheet

class UpcomingChangeBottomSheet : ExpandableBottomSheet() {

    private val upcomingAgreement by lazy {
        requireArguments().getParcelable<UpcomingAgreement.Table>(UPCOMING_AGREEMENT)
            ?: throw IllegalArgumentException("No argument passed to ${this.javaClass.name}")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recycler.adapter = TableAdapter(upcomingAgreement)
    }

    companion object {

        val TAG = UpcomingChangeBottomSheet::class.java.name
        private const val UPCOMING_AGREEMENT = "UPCOMING_AGREEMENT"

        fun newInstance(upcomingAgreement: UpcomingAgreement.Table) =
            UpcomingChangeBottomSheet().apply {
                arguments = bundleOf(UPCOMING_AGREEMENT to upcomingAgreement)
            }
    }
}
