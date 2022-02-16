package com.hedvig.app.feature.embark.ui

import android.content.Context
import android.transition.TransitionManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.google.android.material.appbar.MaterialToolbar
import com.hedvig.app.databinding.ViewMaterialProgressToolbarBinding
import com.hedvig.app.util.ProgressPercentage

class MaterialProgressToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var binding: ViewMaterialProgressToolbarBinding =
        ViewMaterialProgressToolbarBinding.inflate(LayoutInflater.from(context), this)

    val toolbar: MaterialToolbar
        get() = binding.toolbar

    init {
        orientation = VERTICAL
    }

    fun setProgress(progressPercentage: ProgressPercentage) {
        binding.progress.post {
            TransitionManager.beginDelayedTransition(this)
            binding.progress.layoutParams = FrameLayout.LayoutParams(
                (this.width * progressPercentage.value).toInt(),
                binding.progress.layoutParams.height
            )
        }
    }
}
