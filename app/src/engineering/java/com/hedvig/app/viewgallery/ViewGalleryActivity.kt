package com.hedvig.app.viewgallery

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityViewGalleryBinding
import com.hedvig.app.util.extensions.view.applyNavigationBarInsets
import com.hedvig.app.util.extensions.view.applyStatusBarInsets
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding

class ViewGalleryActivity : AppCompatActivity(R.layout.activity_view_gallery) {
    private val binding by viewBinding(ActivityViewGalleryBinding::bind)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        with(binding) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            toolbar.applyStatusBarInsets()
            scrollView.applyStatusBarInsets()
            scrollViewContent.applyNavigationBarInsets()

            toolbar.setNavigationOnClickListener {
                onBackPressed()
            }

            openBottomSheet.setHapticClickListener {
                ViewGalleryBottomSheet
                    .newInstance()
                    .show(supportFragmentManager)
            }
        }
    }

    companion object {
        fun newInstance(context: Context) = Intent(context, ViewGalleryActivity::class.java)
    }
}
