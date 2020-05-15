package com.hedvig.app.feature.embark.passages

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.fragment.app.Fragment
import com.hedvig.app.R
import com.hedvig.app.feature.embark.EmbarkViewModel
import com.hedvig.app.util.boundedProgress
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.spring
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
        actions.adapter = SelectActionAdapter { selectAction ->
            selectAction.keys.zip(selectAction.values).forEach { (key, value) ->
                model.putInStore(key, value)
            }
            response.text = selectAction.label
            response.show()
            val initialTranslation = response.translationY

            response
                .spring(DynamicAnimation.TRANSLATION_Y)
                .addUpdateListener { _, value, _ ->
                    response.alpha = boundedProgress(initialTranslation, 0f, value)
                }
                .addEndListener { _, _, _, _ ->
                    model.navigateToPassage(selectAction.link)
                }
                .animateToFinalPosition(0f)
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
    val label: String,
    val keys: List<String>,
    val values: List<String>
) : Parcelable
