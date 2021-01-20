package com.hedvig.app.feature.embark.passages.previousinsurer

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.fragment.app.Fragment
import com.hedvig.app.R
import com.hedvig.app.databinding.FragmentPreviousInsurerBinding
import com.hedvig.app.feature.embark.passages.MessageAdapter
import com.hedvig.app.feature.embark.ui.PreviousInsurerBottomSheet
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding
import e
import kotlinx.android.parcel.Parcelize

class PreviousInsurerFragment : Fragment(R.layout.fragment_previous_insurer) {
    private val binding by viewBinding(FragmentPreviousInsurerBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val data = requireArguments().getParcelable<PreviousInsurerData>(DATA)
        if (data == null) {
            e { "Programmer error: No DATA provided to ${this.javaClass.name}" }
            return
        }

        binding.apply {
            messages.adapter = MessageAdapter().apply {
                submitList(data.messages)
            }
            currentInsurerContainer.setHapticClickListener {
                PreviousInsurerBottomSheet.newInstance(data.previousInsurers).show(parentFragmentManager, PreviousInsurerBottomSheet.TAG)
            }
        }
    }

    companion object {
        private const val DATA = "DATA"
        fun newInstance(previousInsurerData: PreviousInsurerData) =
            PreviousInsurerFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(DATA, previousInsurerData)
                }
            }
    }
}

@Parcelize
data class PreviousInsurerData(
    val messages: List<String>,
    val previousInsurers: List<PreviousInsurer>
) : Parcelable {
    @Parcelize
    data class PreviousInsurer(
        val name: String,
        val icon: String
    ) : Parcelable
}
