package com.hedvig.app.feature.embark.passages

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.fragment.app.Fragment
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.R
import e
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.fragment_embark_text_action.*

class TextActionFragment : Fragment(R.layout.fragment_embark_text_action) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val data = requireArguments().getParcelable<TextActionData>(DATA)

        if (data == null) {
            e { "Programmer error: No DATA provided to ${this.javaClass.name}" }
            return
        }

        textActionInput.hint = data.hint
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
    val link: String,
    val hint: String
) : Parcelable {
    companion object {
        fun from(data: EmbarkStoryQuery.Data2) = TextActionData(
            data.link.name,
            data.placeholder
        )
    }
}
