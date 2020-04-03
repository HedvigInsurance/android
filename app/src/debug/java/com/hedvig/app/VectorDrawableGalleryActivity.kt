package com.hedvig.app

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.hedvig.app.util.extensions.avdSetLooping
import com.hedvig.app.util.extensions.avdStart
import kotlinx.android.synthetic.debug.activity_vectory_drawable_gallery.*

class VectorDrawableGalleryActivity : AppCompatActivity(R.layout.activity_vectory_drawable_gallery) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        findViewById<ImageView>(R.id.typing).apply {
            avdSetLooping()
            avdStart()
        }

        splash.avdSetLooping()
        splash.avdStart()
    }
}
