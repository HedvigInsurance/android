package com.hedvig.app.feature.keygear.ui.itemdetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.util.extensions.view.useEdgeToEdge
import kotlinx.android.synthetic.main.activity_key_gear_item_detail.*

// TODO: Exit animation. Should be a shared element transition to the newly created item on the tab screen
class KeyGearItemDetailActivity : BaseActivity(R.layout.activity_key_gear_item_detail) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        root.useEdgeToEdge()

        photos.adapter = PhotosAdapter(
            listOf(
                Photo("https://images.unsplash.com/photo-1505156868547-9b49f4df4e04"),
                Photo("https://images.unsplash.com/photo-1505156868547-9b49f4df4e04"),
                Photo("https://images.unsplash.com/photo-1505156868547-9b49f4df4e04")
            )
        )
        pagerIndicator.pager = photos
    }

    companion object {
        fun newInstance(context: Context) = Intent(context, KeyGearItemDetailActivity::class.java)
    }
}
