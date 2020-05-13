package com.hedvig.app.feature.embark.passages

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.fragment.app.Fragment
import com.hedvig.app.R
import e
import kotlinx.android.parcel.Parcelize

class TextActionFragment : Fragment(R.layout.fragment_embark_text_action) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val data = requireArguments().getParcelable<TextActionData>(DATA)

        if (data == null) {
            e { "Programmer error: No DATA provided to ${this.javaClass.name}" }
            return
        }
    }

    companion object {
        private const val DATA = "DATA"
        fun newInstance(data: TextActionData) = TextActionFragment().apply {
            arguments = Bundle().apply {
                putParcelable(DATA, data)
            }
        }
    }
}

@Parcelize
data class TextActionData(
    val link: String
) : Parcelable
