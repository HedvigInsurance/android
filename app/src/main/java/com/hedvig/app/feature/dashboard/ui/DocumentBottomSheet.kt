package com.hedvig.app.feature.dashboard.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hedvig.app.R
import com.hedvig.app.feature.dashboard.service.DashboardTracker
import com.hedvig.app.util.extensions.canOpenUri
import com.hedvig.app.util.extensions.openUri
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import e
import kotlinx.android.synthetic.main.contract_document_sheet.*
import org.koin.android.ext.android.inject

class DocumentBottomSheet : BottomSheetDialogFragment() {
    private val tracker: DashboardTracker by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.contract_document_sheet, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val insuranceCertificateUrl = arguments?.getString(INSURANCE_CERTIFICATE_URL)
        val termsAndConditionsUrl = arguments?.getString(TERMS_AND_CONDITIONS_URL)

        if (termsAndConditionsUrl == null) {
            e { "Programmer error: TERMS_AND_CONDITIONS_URL not provided to ${this.javaClass.name}" }
            return
        }

        if (insuranceCertificateUrl != null) {
            val insuranceCertificateUri = Uri.parse(insuranceCertificateUrl)
            insuranceCertificate.setHapticClickListener {
                tracker.insuranceCertificate()
                if (requireContext().canOpenUri(insuranceCertificateUri)) {
                    requireContext().openUri(insuranceCertificateUri)
                }
            }
            insuranceCertificate.show()
        }
        val termsAndConditionsUri = Uri.parse(termsAndConditionsUrl)


        termsAndConditions.setHapticClickListener {
            tracker.termsAndConditions()
            if (requireContext().canOpenUri(termsAndConditionsUri)) {
                requireContext().openUri(termsAndConditionsUri)
            }
        }
    }

    companion object {
        private const val INSURANCE_CERTIFICATE_URL = "INSURANCE_CERTIFICATE_URL"
        private const val TERMS_AND_CONDITIONS_URL = "TERMS_AND_CONDITIONS_URL"

        const val TAG = "DocumentBottomSheet"

        fun newInstance(insuranceCertificateUrl: String?, termsAndConditionsUrl: String) =
            DocumentBottomSheet().apply {
                arguments = Bundle().apply {
                    insuranceCertificateUrl?.let { putString(INSURANCE_CERTIFICATE_URL, it) }
                    putString(TERMS_AND_CONDITIONS_URL, termsAndConditionsUrl)
                }
            }
    }
}
