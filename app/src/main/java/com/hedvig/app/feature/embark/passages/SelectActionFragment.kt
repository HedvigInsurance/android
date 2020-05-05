package com.hedvig.app.feature.embark.passages

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.fragment.app.Fragment
import com.hedvig.app.R
import com.hedvig.app.feature.embark.EmbarkViewModel
import e
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.fragment_embark_select_action.*
import org.koin.android.viewmodel.ext.android.sharedViewModel

class SelectActionFragment : Fragment(R.layout.fragment_embark_select_action) {
    private val model: EmbarkViewModel by sharedViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val data = requireArguments().getParcelable<SelectActionPassage>(DATA)

        if (data == null) {
            e { "Programmer error: No DATA provided to ${this.javaClass.name}" }
            return
        }

        messages.adapter = MessageAdapter().apply {
            items = data.messages
        }
        actions.adapter = SelectActionAdapter { link ->
            model.navigateToPassage(link)
        }.apply {
            items = data.actions
        }
    }

    companion object {
        private const val DATA = "DATA"
        fun newInstance(data: SelectActionPassage) =
            SelectActionFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(DATA, data)
                }
            }
    }
}

@Parcelize
data class SelectActionPassage(
    val messages: List<String>,
    val actions: List<SelectAction>
) : Parcelable

@Parcelize
data class SelectAction(
    val link: String,
    val label: String
) : Parcelable
