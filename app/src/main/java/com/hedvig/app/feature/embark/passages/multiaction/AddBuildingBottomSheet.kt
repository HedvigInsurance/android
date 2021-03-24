package com.hedvig.app.feature.embark.passages.multiaction

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hedvig.app.R
import com.hedvig.app.databinding.DialogAddBuildingBinding
import com.hedvig.app.util.extensions.viewBinding
import kotlinx.android.parcel.Parcelize


class AddBuildingBottomSheet : BottomSheetDialogFragment() {
    private val binding by viewBinding(DialogAddBuildingBinding::bind)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.dialog_add_building, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {

            val type = arrayOf("Garage", "Attefall", "Friggebod", "Guest house")

            val adapter = ArrayAdapter(
                requireContext(),
                R.layout.dropdown_menu_popup_item,
                type)

            filledExposedDropdown.setAdapter(adapter)
        }
    }

    companion object {
        private const val DATA = "DATA"

        const val TAG = "changeDateBottomSheet"

        fun newInstance(data: AddBuildingParams): AddBuildingBottomSheet {
            return AddBuildingBottomSheet()
                .apply {
                    arguments = bundleOf(
                        DATA to data
                    )
                }
        }
    }
}

@Parcelize
data class AddBuildingParams(
    val type: String?,
    val input: String?,
    val checked: Boolean
) : Parcelable
