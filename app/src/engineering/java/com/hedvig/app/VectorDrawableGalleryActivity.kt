package com.hedvig.app

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.hedvig.app.databinding.ActivityVectoryDrawableGalleryBinding
import com.hedvig.app.util.extensions.avdSetLooping
import com.hedvig.app.util.extensions.avdStart
import com.hedvig.app.util.extensions.viewBinding

class VectorDrawableGalleryActivity : AppCompatActivity(R.layout.activity_vectory_drawable_gallery) {
    private val binding by viewBinding(ActivityVectoryDrawableGalleryBinding::bind)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        with(binding) {
            findViewById<ImageView>(com.hedvig.app.R.id.typing).apply {
                avdSetLooping()
                avdStart()
            }

            splash.avdSetLooping()
            splash.avdStart()
        }
    }
}
