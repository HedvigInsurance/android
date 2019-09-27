package com.hedvig.app

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.hedvig.app.util.extensions.avdSetLooping
import com.hedvig.app.util.extensions.avdStart

class VectorDrawableGalleryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vectory_drawable_gallery)

        findViewById<ImageView>(R.id.typing).apply {
            avdSetLooping()
            avdStart()
        }
    }
}
