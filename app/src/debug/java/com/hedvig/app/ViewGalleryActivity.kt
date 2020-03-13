package com.hedvig.app

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

class ViewGalleryActivity : AppCompatActivity(R.layout.activity_view_gallery) {
    companion object {
        fun newInstance(context: Context) = Intent(context, ViewGalleryActivity::class.java)
    }
}

