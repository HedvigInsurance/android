package com.hedvig.app.feature.embark.passages

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.fragment.app.Fragment
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.R
import com.hedvig.app.feature.embark.EmbarkViewModel
import com.hedvig.app.util.extensions.onChange
import com.hedvig.app.util.extensions.view.setHapticClickListener
import e
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.fragment_embark_text_action.*
import org.koin.android.viewmodel.ext.android.sharedViewModel

class TextActionFragment : Fragment(R.layout.fragment_embark_text_action) {
    private val model: EmbarkViewModel by sharedViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val data = requireArguments().getParcelable<TextActionData>(DATA)

        if (data == null) {
            e { "Programmer error: No DATA provided to ${this.javaClass.name}" }
            return
        }

        messages.adapter = MessageAdapter().apply {
            items = data.messages
        }

        textActionInput.hint = data.hint
        textActionInput.onChange { text ->
            textActionSubmit.isEnabled = text.isNotEmpty()
        }

        textActionSubmit.text = data.submitLabel
        textActionSubmit.setHapticClickListener {
            val inputText = textActionInput.text.toString()
            model.putInStore("${data.passageName}Result", inputText)
            model.putInStore(data.key, inputText)
            val responseText = model.preProcessResponse(data.passageName) ?: inputText
            animateResponse(response, responseText) {
                model.navigateToPassage(data.link)
            }
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
    val link: String,
    val hint: String,
    val messages: List<String>,
    val submitLabel: String,
    val key: String,
    val passageName: String
) : Parcelable {
    companion object {
        fun from(messages: List<String>, data: EmbarkStoryQuery.Data2, passageName: String) =
            TextActionData(
                data.link.name,
                data.placeholder,
                messages,
                data.link.label,
                data.key,
                passageName
            )
    }
}
